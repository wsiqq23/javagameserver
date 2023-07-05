package pers.winter.framework.server.http;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pers.winter.framework.config.ApplicationConfig;
import pers.winter.framework.config.ConfigManager;
import pers.winter.framework.server.IServer;
import pers.winter.framework.server.codec.*;

import java.util.concurrent.TimeUnit;

public class HttpServer implements IServer {
    private static final Logger logger = LogManager.getLogger(HttpServer.class);
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
                ch.pipeline().addLast(new IdleStateHandler(readerIdleTime, 0, 0, TimeUnit.SECONDS));
                ch.pipeline().addLast(new HttpServerCodec());
                ch.pipeline().addLast(new HttpObjectAggregator(Constants.MAX_PACKAGE_LENGTH));
                ch.pipeline().addLast(new HttpContentCompressor());
                ch.pipeline().addLast(new ChunkedWriteHandler());
                ch.pipeline().addLast(HttpServerHandler.INSTANCE);
            }
        });
        ChannelFuture future;
        try {
            future = b.bind(port).sync();
            if(!future.isSuccess()){
                logger.error("Start http server failed!");
                System.exit(-1);
            } else {
                HttpServerHandler.INSTANCE.initServices();
                logger.info("Start http server success! Listening port {} ", port);
            }
        } catch (Exception e) {
            logger.error("Start http server exception!",e);
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
