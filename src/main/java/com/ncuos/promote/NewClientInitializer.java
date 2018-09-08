package com.ncuos.promote;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Sharable
public class NewClientInitializer extends ChannelInitializer<SocketChannel> {
	private static final Logger log  = LoggerFactory.getLogger(NewClientInitializer.class);
	private DefaultEventExecutorGroup singleThreadExGroup;
	
	public NewClientInitializer() {
		singleThreadExGroup = new DefaultEventExecutorGroup(1);
	}
	
	@Override
    public void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new HttpObjectAggregator(65536));
//        pipeline.addLast(new HttpRequestHandler(indexContent));
//        pipeline.addLast(new WSFrameHandler());
//        pipeline.addLast(singleThreadExGroup, updateCommitter);
    }

}