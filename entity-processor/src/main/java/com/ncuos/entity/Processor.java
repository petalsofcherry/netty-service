package com.ncuos.entity;

import com.sun.source.tree.Tree;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

@SupportedAnnotationTypes("com.ncuos.entity.HttpMethodMapper")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class Processor extends AbstractProcessor {
    private Messager messager;
    private JavacTrees trees;
    private TreeMaker treeMaker;
    private JavacElements utils;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        JavacProcessingEnvironment javacProcessingEnv = (JavacProcessingEnvironment) processingEnv;
        this.messager = processingEnv.getMessager();
        this.trees = JavacTrees.instance(processingEnv);
        Context context = (javacProcessingEnv).getContext();
        this.treeMaker = TreeMaker.instance(context);
        this.utils = javacProcessingEnv.getElementUtils();
    }

    @Override
    public synchronized boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(HttpMethodMapper.class);

        elements.forEach(element -> {
            if (element.getKind() == ElementKind.CLASS) {
                JCTree jcTree = trees.getTree(element);
                jcTree.accept(new TreeTranslator() {
                    @Override
                    public void visitClassDef(JCTree.JCClassDecl jcClassDecl) {
                        messager.printMessage(Diagnostic.Kind.NOTE, jcClassDecl.getSimpleName() + " has been processed");

                        JCTree.JCVariableDecl mapperVariableDecl = makeHttpMethodMapperVariableDecl();
                        jcClassDecl.defs = jcClassDecl.defs.prepend(mapperVariableDecl);

                        List<JCTree.JCMethodDecl> jcMethodDeclList = List.nil();

                        for (JCTree tree : jcClassDecl.defs) {
                            if (tree.getKind().equals(Tree.Kind.METHOD)) {
                                JCTree.JCMethodDecl jcMethodDecl = (JCTree.JCMethodDecl) tree;
                                jcMethodDeclList = jcMethodDeclList.append(jcMethodDecl);
                            }
                        }

                        AtomicBoolean mapperPutted = new AtomicBoolean(false);
                        jcMethodDeclList.forEach(jcMethodDecl -> {
                            if (jcMethodDecl.getName().toString().equals(jcClassDecl.getSimpleName().toString())) {
                                appendMethodBody(jcMethodDecl);
                                mapperPutted.set(true);
                            }
                        });

                        if (!mapperPutted.get()) {
                            JCTree.JCMethodDecl jcMethodDecl = makeNoArgConstructor(jcClassDecl);
                            appendMethodBody(jcMethodDecl);
                            mapperPutted.set(true);
                        }

                        jcClassDecl.defs = jcClassDecl.defs.prepend(makeHttpMethodMapperMethodDecl(mapperVariableDecl));

                        super.visitClassDef(jcClassDecl);
                    }
                });
            }
        });
        return true;
    }

    private JCTree.JCVariableDecl makeHttpMethodMapperVariableDecl() {
        JCTree.JCTypeApply functionWithType = treeMaker.TypeApply(
                makeSelectExpr("java.util.function.BiFunction"),
                List.of(
                        makeSelectExpr("io.netty.channel.ChannelHandlerContext"),
                        makeSelectExpr("Object"),
                        makeSelectExpr("com.google.gson.Gson")
                )
        );

        List<JCTree.JCExpression> genericArguments = List.of(makeSelectExpr("io.netty.handler.codec.http.HttpMethod"),
                functionWithType);
        JCTree.JCTypeApply vartype = treeMaker.TypeApply(makeSelectExpr("java.util.Map"), genericArguments);

        JCTree.JCIdent classNew = ident("java.util.HashMap");

        JCTree.JCNewClass newClass = treeMaker.NewClass(null, genericArguments, classNew,
                List.nil(), null);

        return treeMaker.VarDef(
                treeMaker.Modifiers(Flags.PRIVATE+Flags.STATIC+Flags.FINAL),
                utils.getName("HTTP_METHOD_MAPPER"),
                vartype,
                newClass
                );
    }

    private JCTree.JCMethodDecl makeHttpMethodMapperMethodDecl(JCTree.JCVariableDecl jcVariableDecl) {
        ListBuffer<JCTree.JCStatement> statements = new ListBuffer<>();
        statements.append(treeMaker.Return(treeMaker.Select(ident("this"), jcVariableDecl.getName())));
        JCTree.JCBlock body = treeMaker.Block(0, statements.toList());
        return treeMaker.MethodDef(
                treeMaker.Modifiers(Flags.PUBLIC),
                utils.getName("getHttpMethodMapper"),
                jcVariableDecl.vartype,
                List.nil(),
                List.nil(),
                List.nil(),
                body,
                null
        );
    }

    private JCTree.JCMethodDecl makeNoArgConstructor(JCTree.JCClassDecl jcClassDecl) {
        return treeMaker.MethodDef(
                treeMaker.Modifiers(Flags.PUBLIC),
                jcClassDecl.getSimpleName(),
                treeMaker.TypeIdent(TypeTag.VOID),
                List.nil(),
                List.nil(),
                List.nil(),
                treeMaker.Block(0, List.nil()),
                null
        );
    }

    private void appendMethodBody(JCTree.JCMethodDecl jcMethodDecl) {
        JCTree.JCBlock body = jcMethodDecl.body;
        List<JCTree.JCStatement> statements = body.stats;

        MethodGetter.getterList.forEach(getter-> prependStatements(statements, getter.get()));

        body.stats = statements;
    }

    private void prependStatements(List<JCTree.JCStatement> statements, String methodName) {
        statements.prepend(treeMaker.Exec(
                treeMaker.Apply(
                        List.nil(),
                        makeSelectExpr("HTTP_METHOD_MAPPER.put"),
                        List.of(
                                makeSelectExpr("io.netty.handler.codec.http.HttpMethod."
                                        + methodName.toUpperCase()),
                                makeMethodLambda(methodName)
                        )
                )
        ));
    }

    private JCTree.JCLambda makeMethodLambda(String methodName) {
        JCTree.JCVariableDecl param1 = treeMaker.VarDef(
                treeMaker.Modifiers(Flags.PARAMETER),
                utils.getName("channelHandlerContext"),
                makeSelectExpr("io.netty.channel.ChannelHandlerContext"),
                null
        );

        JCTree.JCVariableDecl param2 = treeMaker.VarDef(
                treeMaker.Modifiers(Flags.PARAMETER),
                utils.getName("msg"),
                makeSelectExpr("Object"),
                null
        );

        ListBuffer<JCTree.JCStatement> statements = new ListBuffer<>();
        statements.append(
                treeMaker.Return(
                        treeMaker.Apply(
                                List.nil(),
                                treeMaker.Select(ident("this"), utils.getName(methodName)),
                                List.of(param1.getNameExpression(), param2.getNameExpression())
                        )
                )
        );
        JCTree.JCBlock body = treeMaker.Block(0, statements.toList());

        return treeMaker.Lambda(
                List.of(param1, param2),
                body
        );
    }

    private JCTree.JCExpression makeSelectExpr(String select) {
        String[] parts = select.split("\\.");
        JCTree.JCExpression expression = ident(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            expression = treeMaker.Select(expression, utils.getName(parts[i]));
        }
        return expression;
    }

    private JCTree.JCIdent ident(String name) {
        return treeMaker.Ident(utils.getName(name));
    }

    private static class MethodGetter {
        private static String get = "get";
        private static String post = "post";
        private static String delete  = "delete";
        static List<Supplier<String>> getterList = List.of(
                MethodGetter::getGet,
                MethodGetter::getPost,
                MethodGetter::getDelete
        );

        static String getGet() {
            return get;
        }

        static String getPost() {
            return post;
        }

        static String getDelete() {
            return delete;
        }
    }
}