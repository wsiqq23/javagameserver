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
package pers.winter.server.socket;

import com.alibaba.fastjson.JSON;
import com.google.protobuf.GeneratedMessageV3;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.TooLongFrameException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pers.winter.server.codec.AbstractBaseMessage;

import java.net.InetSocketAddress;

@ChannelHandler.Sharable
public class SocketServerHandler extends ChannelInboundHandlerAdapter {
    private final Logger logger = LogManager.getLogger(SocketServerHandler.class);
    public static final SocketServerHandler INSTANCE = new SocketServerHandler();
    private SocketServerHandler(){}
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(msg instanceof AbstractBaseMessage){
            logger.info("Receive JSON message: "+JSON.toJSONString(msg));
        } else if(msg instanceof GeneratedMessageV3){
            logger.info("Receive Proto message: "+msg);
        }
        ctx.writeAndFlush(msg);
    }
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
        logger.debug("Channel active! IP: {}, port: {}",address.getAddress().getHostAddress(),address.getPort());
    }
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
        logger.debug("Channel inactive! IP: {}, port: {}",address.getAddress().getHostAddress(),address.getPort());
    }
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        Channel channel = ctx.channel();
        StringBuilder logBuilder = new StringBuilder();
        if (channel != null) {
            InetSocketAddress address = (InetSocketAddress) channel.remoteAddress();
            logBuilder.append("IP:");
            logBuilder.append(address.getAddress().getHostAddress());
            logBuilder.append(". ");
        }
        if(cause != null ){
            if(cause instanceof TooLongFrameException) {
                logBuilder.append("Frame too long!");
                logger.debug(logBuilder.toString(), cause);
                return;
            }
            if(cause.getMessage() != null && cause.getMessage().contains("Connection reset by peer")){
                ctx.close();
                return;
            }
        }
        logBuilder.append("Socket exception!");
        logger.warn(logBuilder.toString() ,cause);
        ctx.close();
    }
}