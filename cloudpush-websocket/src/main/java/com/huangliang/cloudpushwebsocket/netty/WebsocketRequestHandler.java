package com.huangliang.cloudpushwebsocket.netty;

import com.huangliang.cloudpushwebsocket.service.HttpResponseService;
import com.huangliang.cloudpushwebsocket.service.websocket.WebsocketServiceStrategy;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.HttpRequestHandler;

@ChannelHandler.Sharable
@Component
@Slf4j
public class WebsocketRequestHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    @Autowired
	private RedisTemplate redisTemplate;

	@Autowired
	private HttpRequestHandler httpRequestHandler;
	@Autowired
	private HttpResponseService httpResponseService;
	@Autowired
	private WebsocketServiceStrategy websocketServiceFactory;

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame){
		try {
			/**
			 *  策略模式
			 * 判断是否关闭链路的指令
			 * 判断是否ping消息
			 * 判断是否二进制消息
			 * 判断是否文本消息
			 */
			websocketServiceFactory.execute(ctx,frame);
		} catch (Exception e) {
//		    e.printStackTrace();
			log.error("请求异常",e);
//			httpResponseHandler.responseFailed(ctx);
		}

	}

	@Override
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
		log.info("channelRegistered");
		super.channelRegistered(ctx);
	}

	@Override
	public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
		log.info("channelUnregistered");
		super.channelUnregistered(ctx);
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		log.info("channelActive");
		super.channelActive(ctx);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		log.info("channelInactive");
		super.channelInactive(ctx);
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		log.info("channelReadComplete");
		ctx.flush();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
	}

}