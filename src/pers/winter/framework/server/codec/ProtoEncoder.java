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
package pers.winter.framework.server.codec;

import com.google.protobuf.GeneratedMessageV3;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * Message encoder for Protocol Buffers message
 * @author Winter
 */
@ChannelHandler.Sharable
public class ProtoEncoder extends MessageToByteEncoder<GeneratedMessageV3> {
    public static final ProtoEncoder INSTANCE = new ProtoEncoder();
    private ProtoEncoder(){}
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, GeneratedMessageV3 generatedMessageV3, ByteBuf byteBuf) throws Exception {
        byte[] data = generatedMessageV3.toByteArray();
        int length = Constants.ENCODE_HEADER_LENGTH + data.length;
        byteBuf.writeInt(length);
        byteBuf.writeByte(Constants.CODEC_PROTO);
        int messageID = ProtoMessageDictionary.getMessageID(generatedMessageV3);
        byteBuf.writeInt(messageID);
        byteBuf.writeBytes(data);
    }
}
