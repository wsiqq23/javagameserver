package pers.winter.example.httpservice;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pers.winter.framework.server.http.AnnHttpRequestMapping;
import pers.winter.framework.server.http.AnnHttpRestController;
import pers.winter.framework.server.http.HttpServerHandler;

import java.util.Map;

@AnnHttpRestController
public class DemoHttpService {
    private Logger logger = LogManager.getLogger(DemoHttpService.class);
    @AnnHttpRequestMapping(value = "/testGet", method = HttpServerHandler.HttpRequestMethod.GET)
    public FullHttpResponse get(Map<String, String> parameters){
        logger.info("Get data:{}",parameters);
        ByteBuf content = Unpooled.copiedBuffer("OK", CharsetUtil.UTF_8);
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK,content);
        return response;
    }
    @AnnHttpRequestMapping(value = "/testPost", method = HttpServerHandler.HttpRequestMethod.POST)
    public FullHttpResponse post(byte[] data){
        logger.info("Post data:{}",new String(data));
        ByteBuf content = Unpooled.copiedBuffer("OK", CharsetUtil.UTF_8);
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK,content);
        return response;
    }
}
