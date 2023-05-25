/*
 * Copyright 2023 Winter Game Server
 *
 * The Winter Game Server licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package pers.winter.server.codec;

import com.google.protobuf.GeneratedMessageV3;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.TooLongFrameException;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pers.winter.message.AbstractBaseMessage;
import pers.winter.message.MessageCenter;

import java.net.InetSocketAddress;

public class WebSocketServerHandler extends SimpleChannelInboundHandler<WebSocketFrame> {
    private static final Logger logger = LogManager.getLogger(WebSocketServerHandler.class);
    private final MessageDecoder decoder = new MessageDecoder();
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, WebSocketFrame webSocketFrame) throws Exception {
        if(webSocketFrame instanceof BinaryWebSocketFrame){
            Object msg = decoder.decode(webSocketFrame.content());
            if(msg instanceof AbstractBaseMessage){
                AbstractBaseMessage baseMessage = (AbstractBaseMessage)msg;
                baseMessage.setContext(channelHandlerContext);
                MessageCenter.INSTANCE.receiveMessage(baseMessage);
            } else if(msg instanceof GeneratedMessageV3){
                logger.debug("Receive Proto message: {}",msg);
            }
        }
    }
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (evt instanceof IdleStateEvent && ((IdleStateEvent) evt).state() == IdleState.READER_IDLE) {
            ctx.close();
        }
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if(cause != null ){
            if(cause.getMessage() != null && cause.getMessage().contains("Connection reset by peer")){
                ctx.close();
                return;
            }
        }
        Channel channel = ctx.channel();
        StringBuilder logBuilder = new StringBuilder();
        if (channel != null) {
            InetSocketAddress address = (InetSocketAddress) channel.remoteAddress();
            logBuilder.append("IP:");
            logBuilder.append(address.getAddress().getHostAddress());
            logBuilder.append(". ");
        }
        logBuilder.append("Socket exception!");
        logger.warn(logBuilder.toString() ,cause);
        ctx.close();
    }
}
