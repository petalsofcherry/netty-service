package com.ncuos.promote.dispatch;

import io.netty.handler.codec.http.HttpMethod;

import java.util.HashMap;
import java.util.Map;


public class Dispatcher<T> {
    private final Map<HttpMethod, HttpDispatcher<T>> httpRouters = new HashMap<>();

    private final WsDispatcher<T> wsRouters = new WsDispatcher<>();

    private final HttpDispatcher<T> anyHttpMethodRouter = new HttpDispatcher<>();


    private Dispatcher<T> addHttpRoute(HttpMethod method, String path, T target) {
        getHttpDispatcher(method).addRoute(path, target);
        return this;
    }

    private Dispatcher<T> addWsRoute(String path, T target) {
        wsRouters.addRoute(path, target);
        return this;
    }

    private HttpDispatcher<T> getHttpDispatcher(HttpMethod method) {
        if (method == null) {
            return anyHttpMethodRouter;
        }

        HttpDispatcher<T> r = httpRouters.get(method);
        if (r == null) {
            r = new HttpDispatcher<>();
            httpRouters.put(method, r);
        }
        return r;
    }

    public DispatchResult<T> httpDispatch(HttpMethod method, String uri) {
        HttpDispatcher<T> router = httpRouters.get(method);
        if (router == null) {
            router = anyHttpMethodRouter;
        }

        DispatchResult<T> ret = router.dispatch(uri);
        if (ret != null) {
            return new DispatchResult<>(uri, ret.target());
        }

        if (router != anyHttpMethodRouter) {
            ret = anyHttpMethodRouter.dispatch(uri);
            if (ret != null) {
                return new DispatchResult<>(uri, ret.target());
            }
        }

        return null;
    }

    public DispatchResult<T> wsDispatch(String uri) {
        DispatchResult<T> ret = wsRouters.dispatch(uri);

        if (ret != null) {
            return new DispatchResult<>(uri, ret.target());
        }
        return null;
    }

    public Dispatcher<T> GET(String path, T target) {
        return addHttpRoute(HttpMethod.GET, path, target);
    }

    public Dispatcher<T> POST(String path, T target) {
        return addHttpRoute(HttpMethod.POST, path, target);
    }

    public Dispatcher<T> DELETE(String path, T target) {
        return addHttpRoute(HttpMethod.DELETE, path, target);
    }

    public Dispatcher<T> WS(String path, T target) {
        return addWsRoute(path, target);
    }
}
