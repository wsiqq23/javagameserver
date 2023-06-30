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
package pers.winter.framework.server.socket;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketFrameAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pers.winter.framework.config.ApplicationConfig;
import pers.winter.framework.config.ConfigManager;
import pers.winter.framework.server.codec.*;

import java.util.concurrent.TimeUnit;

/**
 * A websocket server
 * @author Winter
 */
public class WebSocketServer implements IServer {
    private static final Logger logger = LogManager.getLogger(WebSocketServer.class);
    private EventLoopGroup bossGroup = null;
    private EventLoopGroup workerGroup = null;
    @Override
    public void start(int port) {
        ServerBootstrap b = new ServerBootstrap();
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();
        short readerIdleTime = ConfigManager.INSTANCE.getConfig(ApplicationConfig.class).getIdleStateTime();
        b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(new HttpServerCodec());
                ch.pipeline().addLast(new HttpObjectAggregator(65536));
                ch.pipeline().addLast(new WebSocketServerCompressionHandler());
                ch.pipeline().addLast(new WebSocketServerProtocolHandler("/",null,true));
                ch.pipeline().addLast(new IdleStateHandler(readerIdleTime, 0, 0, TimeUnit.SECONDS));
                ch.pipeline().addLast(WebSocketOutboundHandler.INSTANCE);
                ch.pipeline().addLast(ProtoEncoder.INSTANCE);
                ch.pipeline().addLast(JsonEncoder.INSTANCE);
                ch.pipeline().addLast(new WebSocketFrameAggregator(Constants.MAX_PACKAGE_LENGTH));
                ch.pipeline().addLast(new WebSocketServerHandler());
            }
        });
        ChannelFuture future;
        try {
            future = b.bind(port).sync();
            if(!future.isSuccess()){
                logger.error("Start websocket server failed!");
                System.exit(-1);
            } else {
                logger.info("Start websocket server success! Listening port {} ", port);
            }
        } catch (InterruptedException e) {
            logger.error("Start websocket server exception!",e);
            System.exit(-1);
        }
    }

    @Override
    public void stop() {
        if(bossGroup != null){
            bossGroup.shutdownGracefully().syncUninterruptibly();
        }
        if(workerGroup != null){
            workerGroup.shutdownGracefully().syncUninterruptibly();
        }
    }
}
