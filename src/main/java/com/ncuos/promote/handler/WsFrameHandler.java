package com.ncuos.promote.handler;

import com.ncuos.promote.dispatch.Dispatcher;
import com.ncuos.promote.service.group.*;
import com.ncuos.promote.dispatch.Service;
import com.ncuos.promote.session.Session;
import com.ncuos.promote.session.SessionManager;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WsFrameHandler extends SimpleChannelInboundHandler<WebSocketFrame> {
    private final Dispatcher<Class<? extends Service>> dispatcher = new Dispatcher<Class<? extends Service>>()
            .WS("/group/init", Init.class)
            .WS("/group/new", New.class)
            .WS("/group/groupTimeout", GroupTimeout.class)
            .WS("/group/set", Set.class)
            .WS("/group/finish", Finish.class)
            .WS("/group/puzzleTimeout", PuzzleTimeout.class)
            .WS("/group/group_changed", GroupChanged.class)
            .WS("/group/group_finished", GroupFinished.class);

    private WebSocketServerHandshaker handshaker;

	@Override
	protected void messageReceived(ChannelHandlerContext ctx, WebSocketFrame msg) throws Exception {
		handleWebSocketFrame(ctx, msg);
	}

	private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {

		// Check for closing frame
		if (frame instanceof CloseWebSocketFrame) {
			handshaker.close(ctx.channel(),
					(CloseWebSocketFrame) frame.retain());
			return;
		}
		if (frame instanceof PingWebSocketFrame) {
			ctx.channel().write(
					new PongWebSocketFrame(frame.content().retain()));
			return;
		}
//		if (frame instanceof PongWebSocketFrame) {
//			ctx.channel().write(
//					new PongWebSocketFrame(frame.content().retain()));
//			return;
//		}
		if (!(frame instanceof TextWebSocketFrame)) {
			log.warn("Frame not a TextWebSocketFrame {}. Ignoring",frame);
			return;
		}

		// Send the uppercase string back.
		String request = ((TextWebSocketFrame) frame).text();
		log.trace("Received {} from {}", ctx.channel(), request);
		ctx.fireChannelRead(1);
	}

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //可以将channel.id().asLongText()或channel.id().asShortText()作为Session的ID
        Session session = Session.buildSession(ctx.channel());
        //Session存入Redis
        SessionManager.pushSession2Redis(session);
        log.info("终端连接:{}", session);
    }

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
		//If it is a websocket, handle it
		if(evt instanceof WebSocketServerHandshaker) {
			this.handshaker = (WebSocketServerHandshaker) evt;
		} else {
			//Else pass it further pipeline
			ctx.fireUserEventTriggered(evt);
		}
	}


}
