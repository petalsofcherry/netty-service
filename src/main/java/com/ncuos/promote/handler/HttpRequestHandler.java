package com.ncuos.promote.handler;

import com.ncuos.promote.dispatch.DispatchResult;
import com.ncuos.promote.dispatch.Dispatcher;
import com.ncuos.promote.service.Login;
import com.ncuos.promote.dispatch.Service;
import com.ncuos.promote.service.group.*;
import com.ncuos.promote.service.admin.*;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;


@Slf4j
public class HttpRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private static final String WS_PATH = "/gameFeed";
    private final Dispatcher<Class<? extends Service>> dispatcher = new Dispatcher<Class<? extends Service>>()
            .POST("/login", Login.class)
            .GET("/group/init", Init.class)
            .GET("/group/view", View.class)
            .POST("/group/join", Join.class)
            .GET("/group/new", New.class)
            .GET("/group/groupTimeout", GroupTimeout.class)
            .GET("/group/board", Board.class)
            .GET("/group/set", Set.class)
            .GET("/group/finish", Finish.class)
            .GET("/group/puzzleTimeout", PuzzleTimeout.class)
            .GET("/group/groups", Groups.class)
            .GET("/group/group_changed", GroupChanged.class)
            .GET("/group/group_finished", GroupFinished.class)
            .DELETE("/admin/flush_all", FlushAll.class)
            .POST("/admin/start_game",  StartGame.class);

	@Override
    public void messageReceived(ChannelHandlerContext ctx, FullHttpRequest msg) throws InstantiationException, IllegalAccessException {
        if (msg != null) {
            handleHttpRequest(ctx, msg);
        }
    }

    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req)
			throws IllegalAccessException, InstantiationException {
        DispatchResult<Class<? extends Service>> dispatchResult = dispatcher.httpDispatch(req.method(), req.uri());
        Service service = dispatchResult.target().newInstance();

        if (!WS_PATH.equals(req.uri())) {
            service.getHttpMethodMapper().get(req.method()).apply(ctx, req);
        } else {
            WebSocketServerHandshakerFactory wsFactory =
            		new WebSocketServerHandshakerFactory(req.uri(), null, true);
            WebSocketServerHandshaker handshaker = wsFactory.newHandshaker(req);

            if (handshaker == null) {
                WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
            } else {
                ChannelFuture complete = handshaker.handshake(ctx.channel(), req);
                //attachPeriodicPinger(complete);
            }
            ctx.fireUserEventTriggered(handshaker);
        }
    }

    private void attachPeriodicPinger(ChannelFuture future) {
    	future.addListener(new ChannelFutureListener() {
			
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				if(future.isSuccess()) {
					Channel ch = future.channel();
					log.info("Creating a scheduled task to ping connection {} ",ch);
					ch.eventLoop().scheduleWithFixedDelay(() -> {
						ch.writeAndFlush(new TextWebSocketFrame("Server time is " +	System.currentTimeMillis()));
					}, 1000, 1000, TimeUnit.MILLISECONDS);
				}
			}
		});
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    	log.error("Error. Closing channel",cause);
        ctx.close();
    }
}
