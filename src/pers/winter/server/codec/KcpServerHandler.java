package pers.winter.server.codec;

import io.jpower.kcp.netty.UkcpChannel;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetSocketAddress;

@ChannelHandler.Sharable
public class KcpServerHandler extends ChannelInboundHandlerAdapter {
    private final Logger logger = LogManager.getLogger(KcpServerHandler.class);
    public static final KcpServerHandler INSTANCE = new KcpServerHandler();
    private KcpServerHandler(){}
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        logger.debug("Receive Proto message: {}",msg);
        ctx.channel().writeAndFlush(msg);
    }
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
        logger.debug("Channel active! IP: {}, port: {}",address.getAddress().getHostAddress(),address.getPort());
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("Exception in KCP!", cause);
    }
}
