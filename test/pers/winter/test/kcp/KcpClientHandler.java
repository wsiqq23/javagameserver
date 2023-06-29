package pers.winter.test.kcp;

import io.jpower.kcp.netty.UkcpChannel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class KcpClientHandler extends SimpleChannelInboundHandler {
    private Logger logger = LogManager.getLogger(KcpClientHandler.class);
    private int conv;
    public KcpClientHandler(int conv){
        this.conv = conv;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object msg) throws Exception {
        logger.debug("Self conv:{}, Receive Proto message: {}",conv,msg);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        UkcpChannel kcpCh = (UkcpChannel) ctx.channel();
        kcpCh.conv(conv);
    }
}
