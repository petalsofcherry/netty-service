package com.ncuos.promote.service;

import com.google.gson.Gson;
import com.ncuos.entity.HttpMethodMapper;
import com.ncuos.promote.dispatch.Service;
import io.netty.channel.ChannelHandlerContext;

@HttpMethodMapper
public class Login implements Service {
    public Gson post(ChannelHandlerContext channelHandlerContext, Object msg) {
        return new Gson();
    }
}
