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

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import pers.winter.framework.message.AbstractBaseMessage;
import pers.winter.message.json.ActionFail;
import pers.winter.message.json.Bye;
import pers.winter.message.json.GenericResponse;
import pers.winter.message.json.Hello;
import pers.winter.message.multiroles.login.Handshake;
import pers.winter.message.multiroles.login.HandshakeResponse;
import pers.winter.message.multiroles.login.RoleLogin;
import pers.winter.message.multiroles.login.RoleLoginResponse;

/**
 * A dictionary for json message id.
 * @author Winter
 */
public class JsonMessageDictionary {
    /**
     * Look up the message class by message id
     */
    public static Class<? extends AbstractBaseMessage> getMessageClass(int id){
        return DICTIONARY.get(id);
    }
    /**
     * Loop up the message id by message class
     */
    public static int getMessageID(Class<? extends AbstractBaseMessage> cls){
        return DICTIONARY.inverse().get(cls);
    }
    private static final BiMap<Integer, Class<? extends AbstractBaseMessage>> DICTIONARY = HashBiMap.create();
    static {
        DICTIONARY.put(1, Hello.class);
        DICTIONARY.put(2, Bye.class);
        DICTIONARY.put(3, Handshake.class);
        DICTIONARY.put(4, HandshakeResponse.class);
        DICTIONARY.put(5, ActionFail.class);
        DICTIONARY.put(6, GenericResponse.class);
        DICTIONARY.put(7, RoleLogin.class);
        DICTIONARY.put(8, RoleLoginResponse.class);
    }
}
