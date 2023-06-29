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
package pers.winter.test.redis;

import com.alibaba.fastjson.JSON;
import io.lettuce.core.LettuceFutures;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.api.async.RedisHashAsyncCommands;
import io.lettuce.core.api.sync.RedisHashCommands;
import pers.winter.config.ConfigManager;
import pers.winter.redis.RedisConnection;
import pers.winter.redis.RedisManager;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class TestRedis {
    public static void main(String[] args) throws Throwable {
        ConfigManager.INSTANCE.init();
        RedisManager.INSTANCE.init();
        for(int i = 0;i<10;i++){
            RedisHashCommands<String,String> commands = RedisManager.INSTANCE.getConnection().syncHashCommands();
            commands.hset("testMap","key"+i,"value"+i);
        }
        for(int t=0;t<5;t++){
            Thread thread = new Thread(()->{
                try {
                    for (int r = 0; r < 10; r++) {
                        long t1 = System.currentTimeMillis();
                        RedisConnection<String, String> connection = RedisManager.INSTANCE.getConnection();
                        RedisHashAsyncCommands<String, String> commands = connection.asyncHashCommands();
                        Map<String, String> map = new HashMap<>();
                        final int times = 11;
                        RedisFuture<String>[] futures = new RedisFuture[times];
                        for (int i = 0; i < times; i++) {
                            futures[i] = commands.hget("testMap", "key" + i);
                        }
                        LettuceFutures.awaitAll(5, TimeUnit.SECONDS, futures);
                        for (int i = 0; i < times; i++) {
                            map.put("key" + i, futures[i].get());
                        }
                        long t2 = System.currentTimeMillis();
                        System.out.println("Thread:" + Thread.currentThread().getName() + ",Cost:" + (t2 - t1) + ",data:" + JSON.toJSONString(map));
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            });
            thread.setName("Thread"+t);
            thread.start();
        }
    }
}
