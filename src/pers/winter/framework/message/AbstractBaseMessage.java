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
package pers.winter.framework.message;

import com.alibaba.fastjson.annotation.JSONField;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import pers.winter.example.session.UserSession;

/**
 * Parent class for all messages
 * @author Winter
 */
public abstract class AbstractBaseMessage {
    private transient ChannelHandlerContext context;
    private transient UserSession session;
    public void setContext(ChannelHandlerContext context){
        this.context = context;
    }
    public ChannelHandlerContext getContext(){return this.context;}
    public void setSession(UserSession session){
        this.session = session;
    }
    public UserSession getSession(){
        return this.session;
    }
    @JSONField(serialize = false)
    public Channel getChannel(){
        return context!=null?context.channel():null;
    }
    public abstract byte[] serialized();
}
