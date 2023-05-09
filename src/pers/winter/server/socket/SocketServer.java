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

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pers.winter.server.codec.JsonEncoder;
import pers.winter.server.codec.MessageDecoder;
import pers.winter.server.codec.ProtoEncoder;

/**
 * A socket server
 *
 * @author Winter
 */
public class SocketServer implements IServer {
    private static final Logger logger = LogManager.getLogger(SocketServer.class);
    private EventLoopGroup bossGroup = null;
    private EventLoopGroup workerGroup = null;

    @Override
    public void start(int port) {
        ServerBootstrap b = new ServerBootstrap();
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();
        b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(ProtoEncoder.INSTANCE);
                ch.pipeline().addLast(JsonEncoder.INSTANCE);
                ch.pipeline().addLast(MessageDecoder.INSTANCE);
                ch.pipeline().addLast(SocketServerHandler.INSTANCE);
            }
        });
        ChannelFuture future;
        try {
            future = b.bind(port).sync();
            if(!future.isSuccess()){
                logger.error("Start socket server failed!");
                System.exit(-1);
            } else {
                logger.info("Start socket server success! Listening port {} ", port);
            }
        } catch (InterruptedException e) {
            logger.error("Start socket server exception!",e);
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
