package pers.winter.server.kcp;

import io.jpower.kcp.netty.ChannelOptionHelper;
import io.jpower.kcp.netty.UkcpChannel;
import io.jpower.kcp.netty.UkcpChannelOption;
import io.jpower.kcp.netty.UkcpServerChannel;
import io.netty.bootstrap.UkcpServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pers.winter.server.codec.*;
import pers.winter.server.socket.IServer;

import java.nio.ByteOrder;

public class KcpServer implements IServer {
    private static final Logger logger = LogManager.getLogger(KcpServer.class);
    private EventLoopGroup group = null;
    @Override
    public void start(int port) {
        group = new NioEventLoopGroup();
        UkcpServerBootstrap b = new UkcpServerBootstrap();
        b.childOption(UkcpChannelOption.UKCP_AUTO_SET_CONV,true);
        b.group(group).channel(UkcpServerChannel.class).childHandler(new ChannelInitializer<UkcpChannel>() {
            @Override
            public void initChannel(UkcpChannel ch) throws Exception {
                ch.pipeline().addLast(ProtoEncoder.INSTANCE);
                ch.pipeline().addLast(JsonEncoder.INSTANCE);
                ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(ByteOrder.BIG_ENDIAN,Constants.MAX_PACKAGE_LENGTH,0,4,0,0,true));
                ch.pipeline().addLast(new MessageDecoder());
                ch.pipeline().addLast(KcpServerHandler.INSTANCE);
            }
        });
        ChannelOptionHelper.nodelay(b, true, 20, 2, true).childOption(UkcpChannelOption.UKCP_MTU, 512);
        ChannelFuture future;
        try {
            future = b.bind(port).sync();
            if(!future.isSuccess()){
                logger.error("Start kcp server failed!");
                System.exit(-1);
            } else {
                logger.info("Start kcp server success! Listening port {} ", port);
            }
        } catch (InterruptedException e) {
            logger.error("Start kcp server exception!",e);
            System.exit(-1);
        }
    }

    @Override
    public void stop() {
        if(group != null){
            group.shutdownGracefully().syncUninterruptibly();
        }
    }
}
