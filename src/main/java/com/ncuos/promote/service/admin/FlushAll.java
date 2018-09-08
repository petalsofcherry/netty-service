package com.ncuos.promote.service.admin;

import com.google.gson.Gson;
import com.ncuos.entity.HttpMethodMapper;
import com.ncuos.promote.dispatch.Service;
import io.netty.channel.ChannelHandlerContext;

@HttpMethodMapper
public class FlushAll implements Service {
    public Gson delete(ChannelHandlerContext channelHandlerContext, Object msg) {
        return new Gson();
    }
}
