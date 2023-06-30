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
package pers.winter.redis;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class RedisSubscriberContainer {
    private ConcurrentHashMap<String,RedisSubscriber> subscribers = new ConcurrentHashMap<>();
    protected RedisSubscriberContainer(){}
    public void addListener(String channel, Consumer<String> listener){
        if(!subscribers.contains(channel)){
            subscribers.putIfAbsent(channel,new RedisSubscriber(channel));
        }
        RedisSubscriber subscriber = subscribers.get(channel);
        subscriber.addListener(listener);
    }
    public void removeListener(String channel, Consumer<String> listener){
        RedisSubscriber subscriber = subscribers.get(channel);
        if(subscriber != null){
            subscriber.removeListener(listener);
        }
    }
    public void onMessage(String channel,String message){
        RedisSubscriber subscriber = subscribers.get(channel);
        if(subscriber != null){
            subscriber.onMessage(message);
        }
    }
}
