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
package pers.winter.test.socket.client;

import com.alibaba.fastjson.JSON;
import com.google.protobuf.GeneratedMessageV3;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientProtocolHandler;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pers.winter.message.AbstractBaseMessage;
import pers.winter.server.codec.MessageDecoder;

public class WebSocketClientHandler extends SimpleChannelInboundHandler<WebSocketFrame> {
    private static final Logger logger = LogManager.getLogger(WebSocketClientHandler.class);
    private final MessageDecoder decoder = new MessageDecoder();

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, WebSocketFrame webSocketFrame) throws Exception {
        if(webSocketFrame instanceof BinaryWebSocketFrame){
            Object msg = decoder.decode(webSocketFrame.content());
            if(msg instanceof AbstractBaseMessage){
                logger.debug("Receive JSON message: {}", JSON.toJSONString(msg));
            } else if(msg instanceof GeneratedMessageV3){
                logger.debug("Receive Proto message: {}",msg);
            }
        }
    }

//    @Override
//    public void userEventTriggered(ChannelHandlerContext channelHandlerContext, Object event){
//        super.userEventTriggered(channelHandlerContext, event);
//        if (event instanceof WebSocketClientProtocolHandler.ClientHandshakeStateEvent) {
//            WebSocketClientProtocolHandler.ClientHandshakeStateEvent handshakeEvt = (WebSocketClientProtocolHandler.ClientHandshakeStateEvent) evt;
//            if (handshakeEvt.equals(WebSocketClientProtocolHandler.ClientHandshakeStateEvent.HANDSHAKE_COMPLETE)) {
//                handshakerPromise.setSuccess();
//            }
//        }
//    }
//
}
