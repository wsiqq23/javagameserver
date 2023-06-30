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
package pers.winter.framework.redis;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class RedisSubscriber {
    private static final Logger logger = LogManager.getLogger(RedisSubscriber.class);

    private String channel;
    private Set<Consumer<String>> listeners = ConcurrentHashMap.newKeySet();
    public RedisSubscriber(String channel){
        this.channel = channel;
    }
    public void addListener(Consumer<String> listener){
        if(listeners.isEmpty()){
            synchronized (listeners){
                if(listeners.isEmpty()){
                    RedisManager.INSTANCE.getPubSubConnection().sync().subscribe(channel);
                }
            }
        }
        listeners.add(listener);
    }
    public void removeListener(Consumer<String> listener){
        listeners.remove(listener);
    }
    public void onMessage(String message){
        Iterator<Consumer<String>> iterator = listeners.iterator();
        while(iterator.hasNext()){
            Consumer<String> listener = iterator.next();
            try{
                listener.accept(message);
            } catch (Exception e){
                logger.error("Exception while executing subscriber!",e);
            }
        }
    }
}
