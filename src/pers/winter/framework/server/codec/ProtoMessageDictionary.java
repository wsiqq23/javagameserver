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

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Parser;
import pers.winter.message.proto.Demo;

/**
 * A dictionary for proto message id.
 * @author Winter
 */
public class ProtoMessageDictionary {
    private static final BiMap<Integer, GeneratedMessageV3> DICTIONARY = HashBiMap.create();

    /**
     * Look up the message parser by message id
     */
    public static Parser getMessageParser(int id){
        return DICTIONARY.get(id).getParserForType();
    }
    /**
     * Look up the message id by message object
     */
    public static int getMessageID(GeneratedMessageV3 message){
        return DICTIONARY.inverse().get(message.getDefaultInstanceForType());
    }
    static{
        DICTIONARY.put(1, Demo.Hello.getDefaultInstance());
        DICTIONARY.put(2, Demo.Bye.getDefaultInstance());
    }
}
