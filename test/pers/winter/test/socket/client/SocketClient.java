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

import java.net.InetSocketAddress;
import java.util.Scanner;

import io.netty.util.concurrent.DefaultThreadFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import pers.winter.server.codec.MessageEncoder;
import pers.winter.test.socket.client.SocketClientHandler;

public class SocketClient {
    private Logger log = LogManager.getLogger(SocketClient.class);
    protected EventLoopGroup workerGroup = null;
    protected Channel channel = null;
    private String ip = null;
    private int port = 0;
    public SocketClient(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }
    public boolean connect() throws Exception {
        Bootstrap bootstrap = new Bootstrap();
        this.workerGroup = new NioEventLoopGroup(1, new DefaultThreadFactory("ClientWorkerGroup"));
        bootstrap.group(this.workerGroup);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(MessageEncoder.INSTANCE);
                ch.pipeline().addLast(SocketClientHandler.INSTANCE);
            }
        });
        // 发起异步连接操作
        ChannelFuture future = bootstrap.connect(new InetSocketAddress(this.ip, this.port)).sync();
        if (future.isSuccess()) {
            this.channel = future.channel();
            log.info("Netty socket client to [ip:{}, port:{}]", this.ip, port);
            return true;
        } else {
            return false;
        }
    }

    public void send(String msg){
        channel.writeAndFlush(msg);
    }


    public void disconnect() {
        this.workerGroup.shutdownGracefully().syncUninterruptibly();
        log.info("Netty socket closed");
    }

    public void reconnect() throws Exception {
        // 先断开
        this.disconnect();
        // 再连接
        this.connect();
    }

    public static void main(String[] args) throws Exception {
        SocketClient client = new SocketClient("127.0.0.1",7001);
        client.connect();
        Scanner scanner = new Scanner(System.in);
        String nextLine;
        while(true){
            nextLine = scanner.nextLine();
            client.send(nextLine);
        }
    }
}

