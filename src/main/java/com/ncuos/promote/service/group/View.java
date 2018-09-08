package com.ncuos.promote.service.group;

import com.google.gson.Gson;
import com.ncuos.entity.HttpMethodMapper;
import com.ncuos.promote.dispatch.Service;
import io.netty.channel.ChannelHandlerContext;

@HttpMethodMapper
public class View implements Service {
    public Gson get(ChannelHandlerContext channelHandlerContext, Object msg) {
        return new Gson();
    }
}
