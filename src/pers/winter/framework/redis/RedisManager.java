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

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.pubsub.RedisPubSubAdapter;
import io.lettuce.core.pubsub.RedisPubSubListener;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pers.winter.config.ConfigManager;
import pers.winter.config.RedisConfig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class RedisManager {
    private static final Logger logger = LogManager.getLogger(RedisManager.class);
    public static final RedisManager INSTANCE = new RedisManager();

    private RedisConfig config;

    private RedisClient client;
    private RedisClusterClient clusterClient;
    private RedisConnection<String, String> connection;
    private StatefulRedisPubSubConnection<String,String> pubSubConnection;
    private RedisPubSubListener<String,String> pubSubListener;
    private RedisSubscriberContainer subscriberContainer;

    private RedisManager() {
    }

    public void init() throws IOException {
        config = ConfigManager.INSTANCE.getConfig(RedisConfig.class);
        initConnections();
    }

    private void initConnections() {
        boolean isCluster = config.isCluster();
        if (isCluster) {
            List<RedisURI> redisURIList = new ArrayList<>();
            String[] nodes = config.getUrl().split(",");
            if (config.getPassword() == null || config.getPassword().isEmpty()) {
                for (String node : nodes) {
                    String[] tmp = node.split(":");
                    redisURIList.add(RedisURI.create(tmp[0], Integer.parseInt(tmp[1])));
                }
            } else {
                for (String node : nodes) {
                    String[] tmp = node.split(":");
                    redisURIList.add(RedisURI.builder().withHost(tmp[0]).withPort(Integer.parseInt(tmp[1])).withPassword(config.getPassword().toCharArray()).build());
                }
            }
            clusterClient = RedisClusterClient.create(redisURIList);
            pubSubConnection = clusterClient.connectPubSub();
            logger.info("Connected to redis cluster:{}",config.getUrl());
        } else {
            if (config.getPassword() == null || config.getPassword().isEmpty()) {
                String[] tmp = config.getUrl().split(":");
                client = RedisClient.create(RedisURI.create(tmp[0], Integer.parseInt(tmp[1])));
            } else {
                String[] tmp = config.getPassword().split(":");
                client = RedisClient.create(RedisURI.builder().withHost(tmp[0]).withPort(Integer.parseInt(tmp[1])).withPassword(config.getPassword().toCharArray()).build());
            }
            pubSubConnection = client.connectPubSub();
            logger.info("Connected to redis:{}",config.getUrl());
        }
        connection = new RedisConnection<>(isCluster, isCluster ? clusterClient.connect() : client.connect());
        pubSubListener = new RedisPubSubAdapter<>(){
            @Override
            public void message(String channel,String message){
                subscriberContainer.onMessage(channel, message);
            }
            @Override
            public void subscribed(String channel,long count){
                logger.debug("Subscribed! Channel:{},count:{}",channel,count);
            }
        };
        subscriberContainer = new RedisSubscriberContainer();
    }

    public RedisConnection<String, String> getConnection() {
        return connection;
    }
    public StatefulRedisPubSubConnection<String,String> getPubSubConnection(){return pubSubConnection;}
    public void addPubSubListener(String channel, Consumer<String> listener){
        subscriberContainer.addListener(channel,listener);
    }
    public void removePubSubListener(String channel, Consumer<String> listener){
        subscriberContainer.removeListener(channel,listener);
    }
    public void syncPublish(String channel,String message){
        pubSubConnection.sync().publish(channel,message);
    }
    public void asyncPublish(String channel,String message){
        pubSubConnection.async().publish(channel,message);
    }
}
