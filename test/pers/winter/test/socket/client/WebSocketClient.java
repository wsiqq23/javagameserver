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

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketClientProtocolHandler;
import io.netty.handler.codec.http.websocketx.WebSocketFrameAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketClientCompressionHandler;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pers.winter.framework.server.codec.Constants;
import pers.winter.framework.server.codec.JsonEncoder;
import pers.winter.framework.server.codec.ProtoEncoder;
import pers.winter.framework.server.codec.WebSocketOutboundHandler;
import pers.winter.message.json.Bye;
import pers.winter.message.json.Hello;

import java.net.InetSocketAddress;
import java.net.URI;

public class WebSocketClient {
    private Logger log = LogManager.getLogger(WebSocketClient.class);
    protected EventLoopGroup workerGroup = null;
    protected Channel channel = null;
    private String ip = null;
    private int port = 0;
    public WebSocketClient(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }
    public boolean connect() throws Exception {
        Bootstrap bootstrap = new Bootstrap();
        this.workerGroup = new NioEventLoopGroup(1, new DefaultThreadFactory("ClientWorkerGroup"));
        bootstrap.group(this.workerGroup);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000);
        URI uri = new URI("ws://" + ip + ":" + port+"/");
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(new HttpClientCodec());
                ch.pipeline().addLast(new HttpObjectAggregator(65536));
                ch.pipeline().addLast(WebSocketClientCompressionHandler.INSTANCE);
                ch.pipeline().addLast(new WebSocketClientProtocolHandler(uri, WebSocketVersion.V13,null,true,new DefaultHttpHeaders(),65536));
                ch.pipeline().addLast(WebSocketOutboundHandler.INSTANCE);
                ch.pipeline().addLast(ProtoEncoder.INSTANCE);
                ch.pipeline().addLast(JsonEncoder.INSTANCE);
                ch.pipeline().addLast(new WebSocketFrameAggregator(Constants.MAX_PACKAGE_LENGTH));
                ch.pipeline().addLast(new WebSocketClientHandler());
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

    public void send(Object msg){
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
        WebSocketClient client = new WebSocketClient("127.0.0.1",7002);
        client.connect();
        Hello hello = new Hello();
        hello.time = System.currentTimeMillis();
        StringBuilder sb = new StringBuilder();
        for(int i = 0;i<100000;i++){
            sb.append("OKda");
        }
        hello.data = sb.toString();
        client.send(hello);
        Bye bye = new Bye();
        bye.data1 = "Good night!";
        bye.data2 = "See you tomorrow.";
        client.send(bye);
    }
}
