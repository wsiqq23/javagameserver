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
package pers.winter.server.codec;

import com.alibaba.fastjson.JSON;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * Decode byte to different types of message
 * @author Winter
 */
@ChannelHandler.Sharable
public class MessageDecoder extends ByteToMessageDecoder {
    private final Logger logger = LogManager.getLogger(MessageDecoder.class);
    public static final MessageDecoder INSTANCE = new MessageDecoder();
    private MessageDecoder(){}
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception{
        byte decoderFlag = byteBuf.readByte();
        int messageID = byteBuf.readInt();
        int readableBytes = byteBuf.readableBytes();
        byte[] bytes = new byte[readableBytes];
        byteBuf.readBytes(bytes);
        Object request = null;
        try{
            if(decoderFlag == Constants.CODEC_JSON){
                String json = new String(bytes);
                request = JSON.parseObject(json, JsonMessageDictionary.getMessageClass(messageID));
            } else if(decoderFlag == Constants.CODEC_PROTO){
                request =  ProtoMessageDictionary.getMessageParser(messageID).parseFrom(bytes);
            }
        } catch (Exception e){
            logger.info("Decode message exception!",e);
        }
        if(request != null){
            list.add(request);
        }
    }
}
