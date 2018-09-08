package com.ncuos.promote.session;

public class SessionManager{

    //将Session放入redis
    public static void pushSession2Redis(Session  session){
        //这里就不用我说了吧
        //不知道你自己用的redis客户端是什么
        //调用客户端(比如Jedis)的API把session放到你自己的数据结构不就行了
    }

    //从redis获取指定session
    public Session findById(String sessionId){
        return null;
    }
}
