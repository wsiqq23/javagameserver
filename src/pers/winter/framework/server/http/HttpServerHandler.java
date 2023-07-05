package pers.winter.framework.server.http;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pers.winter.framework.utils.ClassScanner;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ChannelHandler.Sharable
public class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private Logger logger = LogManager.getLogger(HttpServerHandler.class);
    private Map<String, HttpRequestHandler> handlers = new HashMap<>();
    public static final HttpServerHandler INSTANCE = new HttpServerHandler();
    private HttpServerHandler(){}
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest fullHttpRequest){
        QueryStringDecoder decoder = new QueryStringDecoder(fullHttpRequest.uri());
        String path = decoder.path();
        if(!handlers.containsKey(path)){
            logger.info("Receive unhandled http request.");
            ctx.close();
            return;
        }
        HttpRequestHandler handler = handlers.get(path);
        FullHttpResponse response;
        try {
            if (handler.getRequestMethod() == HttpServerHandler.HttpRequestMethod.GET) {
                response = (FullHttpResponse) handler.getMethodHandle().invoke(handler.getService(), decoder.parameters());
            } else {
                ByteBuf content = fullHttpRequest.content();
                byte[] bytes = new byte[content.readableBytes()];
                content.readBytes(bytes);
                response= (FullHttpResponse) handler.getMethodHandle().invoke(handler.getService(), bytes);
            }
            if(fullHttpRequest.headers().containsValue(HttpHeaderNames.CONNECTION,HttpHeaderValues.CLOSE,true)){
                ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
            } else if(fullHttpRequest.headers().containsValue(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE, true)){
                ctx.writeAndFlush(response);
            } else {
                ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
            }
        } catch (Throwable e){
            logger.error("Exception on http request!",e);
            ctx.close();
        }
    }
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent && ((IdleStateEvent)evt).state().equals(IdleState.READER_IDLE)) {
            ctx.close();
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (cause != null && (cause.getMessage() == null || !cause.getMessage().contains("Connection reset by peer"))) {
            logger.error(cause.getMessage(), cause);
        }
        ctx.close();
    }

    public void initServices() throws Exception {
        List<Class<?>> classes = ClassScanner.getTypesAnnotatedWith(AnnHttpRestController.class);
        for(Class cls:classes){
            Constructor<?> defaultConstructor = cls.getConstructor();
            Object serviceInstance = defaultConstructor.newInstance();
            for(Method method:cls.getMethods()){
                if(method.isAnnotationPresent(AnnHttpRequestMapping.class)){
                    if(!FullHttpResponse.class.isAssignableFrom(method.getReturnType())){
                        logger.error("The method {}.{} doesn't return a FullHttpResponse!",cls.getSimpleName(),method.getName());
                        throw new Exception("The AnnHttpRequestMapping method must return a FullHttpResponse!");
                    }
                    AnnHttpRequestMapping requestMapping = method.getAnnotation(AnnHttpRequestMapping.class);
                    if(requestMapping.method() == HttpServerHandler.HttpRequestMethod.GET){
                        if(!Map.class.isAssignableFrom(method.getParameterTypes()[0])){
                            logger.error("The method {}.{} accepts the GET requests but doesn't accept the parameter Map<String,List<String>>!",cls.getSimpleName(),method.getName());
                            throw new Exception("Illegal parameter!");
                        }
                    } else {
                        if(!byte[].class.isAssignableFrom(method.getParameterTypes()[0])){
                            logger.error("The method {}.{} accepts the POST requests but doesn't accept the parameter byte[]!",cls.getSimpleName(),method.getName());
                            throw new Exception("Illegal parameter!");
                        }
                    }
                    MethodHandle methodHandle = MethodHandles.publicLookup().unreflect(method);
                    handlers.put(requestMapping.value(), new HttpRequestHandler(serviceInstance,methodHandle,requestMapping.method()));
                }
            }
        }
    }
    /** Enumeration defining common HTTP request methods. */
    public enum HttpRequestMethod{
        GET,POST
    }
}
