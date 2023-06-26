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

import io.lettuce.core.api.StatefulConnection;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.*;
import io.lettuce.core.api.sync.*;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;

public class RedisConnection<K, V> {
    private final StatefulConnection<K, V> lettuceConnection;
    private final boolean cluster;

    public RedisConnection(boolean isCluster, StatefulConnection<K, V> statefulConnection) {
        this.cluster = isCluster;
        this.lettuceConnection = statefulConnection;
    }

    public boolean isCluster() {
        return cluster;
    }

    private StatefulRedisConnection<K, V> castToNode() {
        return (StatefulRedisConnection<K, V>) lettuceConnection;
    }

    private StatefulRedisClusterConnection<K, V> castToCluster() {
        return (StatefulRedisClusterConnection<K, V>) lettuceConnection;
    }

    public BaseRedisAsyncCommands<K, V> asyncBaseCommands() {
        return cluster ? castToCluster().async() : castToNode().async();
    }

    public RedisHashAsyncCommands<K, V> asyncHashCommands() {
        return cluster ? castToCluster().async() : castToNode().async();
    }

    public RedisStringAsyncCommands<K, V> asyncStringCommands() {
        return cluster ? castToCluster().async() : castToNode().async();
    }

    public RedisKeyAsyncCommands<K, V> asyncKeyCommands() {
        return cluster ? castToCluster().async() : castToNode().async();
    }

    public RedisScriptingAsyncCommands<K, V> asyncScriptCommands() {
        return cluster ? castToCluster().async() : castToNode().async();
    }

    public BaseRedisCommands<K, V> syncBaseCommands() {
        return cluster ? castToCluster().sync() : castToNode().sync();
    }

    public RedisHashCommands<K, V> syncHashCommands() {
        return cluster ? castToCluster().sync() : castToNode().sync();
    }

    public RedisStringCommands<K, V> syncStringCommands() {
        return cluster ? castToCluster().sync() : castToNode().sync();
    }

    public RedisKeyCommands<K, V> syncKeyCommands() {
        return cluster ? castToCluster().sync() : castToNode().sync();
    }

    public RedisScriptingCommands<K, V> syncScriptCommands() {
        return cluster ? castToCluster().sync() : castToNode().sync();
    }
}
