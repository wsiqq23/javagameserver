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
import io.netty.handler.codec.MessageToByteEncoder;
import pers.winter.message.AbstractBaseMessage;

/**
 * Message encoder for JSON message
 * @author Winter
 */
@ChannelHandler.Sharable
public class JsonEncoder extends MessageToByteEncoder<AbstractBaseMessage> {
    public static final JsonEncoder INSTANCE = new JsonEncoder();
    private JsonEncoder(){}
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, AbstractBaseMessage message, ByteBuf byteBuf) throws Exception {
        String json = JSON.toJSONString(message);
        byte[] data = json.getBytes();
        int length = Constants.ENCODE_HEADER_LENGTH + data.length;
        byteBuf.writeInt(length);
        byteBuf.writeByte(Constants.CODEC_JSON);
        int messageID = JsonMessageDictionary.getMessageID(message.getClass());
        byteBuf.writeInt(messageID);
        byteBuf.writeBytes(data);
    }
}
