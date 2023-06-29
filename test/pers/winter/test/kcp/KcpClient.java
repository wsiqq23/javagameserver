package pers.winter.test.kcp;

import io.jpower.kcp.netty.UkcpChannel;
import io.jpower.kcp.netty.UkcpClientChannel;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pers.winter.message.proto.Demo;
import pers.winter.server.codec.Constants;
import pers.winter.server.codec.JsonEncoder;
import pers.winter.server.codec.MessageDecoder;
import pers.winter.server.codec.ProtoEncoder;

import java.net.InetSocketAddress;
import java.nio.ByteOrder;

public class KcpClient {
    private Logger log = LogManager.getLogger(KcpClient.class);
    protected EventLoopGroup workerGroup = null;
    protected Channel channel = null;
    private String ip;
    private int port;
    private int conv;
    public KcpClient(String ip, int port, int conv) {
        this.ip = ip;
        this.port = port;
        this.conv = conv;
    }
    public boolean connect() throws Exception {
        Bootstrap bootstrap = new Bootstrap();
        this.workerGroup = new NioEventLoopGroup();
        bootstrap.group(this.workerGroup);
        bootstrap.channel(UkcpClientChannel.class);
        bootstrap.handler(new ChannelInitializer<UkcpChannel>() {
            @Override
            protected void initChannel(UkcpChannel ch) throws Exception {
                ch.pipeline().addLast(ProtoEncoder.INSTANCE);
                ch.pipeline().addLast(JsonEncoder.INSTANCE);
                ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(ByteOrder.BIG_ENDIAN, Constants.MAX_PACKAGE_LENGTH,0,4,0,0,true));
                ch.pipeline().addLast(new MessageDecoder());
                ch.pipeline().addLast(new KcpClientHandler(conv));
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
        this.disconnect();
        this.connect();
    }

    public static void main(String[] args) throws Exception {
        for(int i = 0;i<5;i++){
            Thread t = new Thread(()->{
                final long nodeID = 1001 + Integer.parseInt(Thread.currentThread().getName());
                KcpClient client = new KcpClient("127.0.0.1",7003, (int) nodeID);
                try {
                    client.connect();
                while(true){
//                    Demo.KcpHandshake handshake = Demo.KcpHandshake.newBuilder().setNodeID(nodeID).build();
//                    client.send(handshake);
//                    Thread.sleep(1000);
                }
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(-1);
                }
            });
            t.setName(String.valueOf(i));
            t.start();
        }
    }
}
