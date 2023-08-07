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
package pers.winter.framework.cache.redis;

import com.alibaba.fastjson.JSON;
import io.lettuce.core.LettuceFutures;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.ScriptOutputType;
import io.lettuce.core.SetArgs;
import io.lettuce.core.api.async.RedisHashAsyncCommands;
import io.lettuce.core.api.async.RedisKeyAsyncCommands;
import io.lettuce.core.api.sync.RedisHashCommands;
import io.lettuce.core.api.sync.RedisKeyCommands;
import io.lettuce.core.api.sync.RedisStringCommands;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pers.winter.framework.config.ApplicationConfig;
import pers.winter.framework.config.ConfigManager;
import pers.winter.framework.db.AbstractBaseEntity;
import pers.winter.framework.db.Constants;
import pers.winter.framework.redis.RedisConnection;
import pers.winter.framework.redis.RedisManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class RedisCache {
    private static final Logger logger = LogManager.getLogger(RedisCache.class);
    private static final String REDIS_DATA_KEY_FORMAT = "Data_%s_%d";
    private static final String REDIS_LOCKALL_KEY_FORMAT = "Lock_%s_%d";
    private static final String REDIS_LOCKONE_KEY_FORMAT = "Lock_%s_%d_%d";

    private static final String SCRIPT_LOCK_PATH = "redisScript/lock.lua";

    private static final String REDIS_RESULT_OK = "OK";

    private final int CACHE_EXPIRE_TIME;
    private final String LOCK_SCRIPT;

    public RedisCache() throws IOException {
        CACHE_EXPIRE_TIME = ConfigManager.INSTANCE.getConfig(ApplicationConfig.class).getCacheExpire();
        InputStream is = ConfigManager.class.getClassLoader().getResourceAsStream(SCRIPT_LOCK_PATH);
        byte[] script = is.readAllBytes();
        LOCK_SCRIPT = new String(script);
    }

    public <T extends AbstractBaseEntity> List<T> selectByKey(long keyID, Class<T> entityClass) {
        List<T> result = null;
        String redisKey = getRedisDataKey(keyID, entityClass);
        Map<String, String> redisResult;
        RedisHashCommands<String, String> syncCommands = RedisManager.INSTANCE.getConnection().syncHashCommands();
        RedisKeyAsyncCommands<String, String> asyncCommands = RedisManager.INSTANCE.getConnection().asyncKeyCommands();
        redisResult = syncCommands.hgetall(redisKey);
        if (redisResult.size() > 0) {
            result = new ArrayList<>();
            Iterator<String> iterator = redisResult.values().iterator();
            while (iterator.hasNext()) {
                String redisData = iterator.next();
                if (redisData.isEmpty()) { // Init flag
                    continue;
                }
                result.add(JSON.parseObject(redisData, entityClass));
            }
            asyncCommands.expire(redisKey, CACHE_EXPIRE_TIME);
        }
        return result;
    }

    private String getRedisDataKey(long key, Class<? extends AbstractBaseEntity> entityClass) {
        return String.format(REDIS_DATA_KEY_FORMAT, entityClass.getSimpleName(), key);
    }

    private String getRedisLockAllKey(long key, Class<? extends AbstractBaseEntity> entityClass) {
        return String.format(REDIS_LOCKALL_KEY_FORMAT, entityClass.getSimpleName(), key);
    }

    private String getRedisLockOneKey(AbstractBaseEntity entity) {
        return String.format(REDIS_LOCKONE_KEY_FORMAT, entity.getClass().getSimpleName(), entity.getKeyID(), entity.getId());
    }

    public void lockCache(long key, Class<? extends AbstractBaseEntity> entityClass) throws Exception {
        RedisStringCommands<String, String> commands = RedisManager.INSTANCE.getConnection().syncStringCommands();
        String ret;
        int tryTimes = 0;
        do {
            ret = commands.set(getRedisLockAllKey(key, entityClass), "1", SetArgs.Builder.nx().ex(5));
        } while (!REDIS_RESULT_OK.equals(ret) && tryTimes++ < 10);
        if (!REDIS_RESULT_OK.equals(ret)) {
            logger.info("Lock {}:{} failed! Try {} times.", entityClass.getSimpleName(), key, tryTimes);
            throw new Exception("Redis lock failed!");
        }
    }

    public void unlockCache(long key, Class<? extends AbstractBaseEntity> entityClass) {
        RedisKeyAsyncCommands<String, String> commands = RedisManager.INSTANCE.getConnection().asyncKeyCommands();
        commands.del(getRedisLockAllKey(key, entityClass));
    }

    public <T extends AbstractBaseEntity> void syncToCache(long key, List<T> entities, Class<T> entityClass) {
        RedisHashAsyncCommands<String, String> commands = RedisManager.INSTANCE.getConnection().asyncHashCommands();
        Map<String, String> data = new HashMap<>(entities.size() + 1);
        data.put("", "");
        for (T entity : entities) {
            data.put(String.valueOf(entity.getId()), JSON.toJSONString(entity));
        }
        commands.hmset(getRedisDataKey(key, entityClass), data);
    }

    public <T extends AbstractBaseEntity> void syncFromCache(List<T> entities, Class<T> entityClass) {
        RedisHashCommands<String, String> commands = RedisManager.INSTANCE.getConnection().syncHashCommands();
        String data;
        for (int i = 0; i < entities.size(); i++) {
            T entity = entities.get(i);
            data = commands.hget(getRedisDataKey(entity.getKeyID(), entityClass), String.valueOf(entity.getId()));
            if (data != null) {
                entities.set(i, JSON.parseObject(data, entityClass));
            }
        }
    }

    public boolean lockEntities(List<AbstractBaseEntity> entities) {
        final int size = entities.size();
        String[] lockKeys = new String[size];
        for (int i = 0; i < lockKeys.length; i++) {
            lockKeys[i] = getRedisLockOneKey(entities.get(i));
        }
        RedisConnection<String, String> connection = RedisManager.INSTANCE.getConnection();
        if (connection.isCluster()) {
            RedisStringCommands<String, String> commands = connection.syncStringCommands();
            SetArgs args = SetArgs.Builder.nx().ex(5);
            List<String> locked = new ArrayList<>();
            boolean allLocked = true;
            try {
                for (int i = 0; i < size; i++) {
                    if (REDIS_RESULT_OK.equals(commands.set(lockKeys[i], "1", args))) {
                        locked.add(lockKeys[i]);
                    } else {
                        logger.debug("Lock failed for key: {}", lockKeys[i]);
                        allLocked = false;
                        break;
                    }
                }
            } finally {
                if (!allLocked && !locked.isEmpty()) {
                    RedisKeyCommands<String, String> keyCommands = connection.syncKeyCommands();
                    for (String lockKey : locked) {
                        keyCommands.del(lockKey);
                    }
                }
            }
            return allLocked;
        } else {
            long result = RedisManager.INSTANCE.getConnection().syncScriptCommands().eval(LOCK_SCRIPT, ScriptOutputType.INTEGER, lockKeys);
            return result == 0;
        }
    }

    public void unlockEntities(List<AbstractBaseEntity> entities) {
        RedisKeyAsyncCommands<String, String> commands = RedisManager.INSTANCE.getConnection().asyncKeyCommands();
        for (AbstractBaseEntity entity : entities) {
            commands.del(getRedisLockOneKey(entity));
        }
    }

    public boolean checkVersion(List<AbstractBaseEntity> entities) {
        RedisConnection<String, String> connection = RedisManager.INSTANCE.getConnection();
        RedisHashAsyncCommands<String, String> commands = connection.asyncHashCommands();
        RedisFuture<String>[] futures = new RedisFuture[entities.size()];
        for (int i = 0; i < entities.size(); i++) {
            AbstractBaseEntity entity = entities.get(i);
            futures[i] = commands.hget(getRedisDataKey(entity.getKeyID(), entity.getClass()), String.valueOf(entity.getId()));
        }
        LettuceFutures.awaitAll(5, TimeUnit.SECONDS, futures);
        boolean checkVersionSuccess = true;
        try {
            for (int i = 0; i < futures.length; i++) {
                AbstractBaseEntity entity = entities.get(i);
                AbstractBaseEntity entityInRedisCache = null;
                String redisData = futures[i].get();
                if (redisData != null) {
                    entityInRedisCache = JSON.parseObject(redisData, entities.get(i).getClass());
                }
                if (entity.getAction() == Constants.Action.UPDATE) {
                    if (entityInRedisCache == null) {
                        logger.debug("Check version failed! Data was deleted. Class: {}. Key ID: {}, unique ID: {}.", entity.getClass().getSimpleName(), entity.getKeyID(), entity.getId());
                        checkVersionSuccess = false;
                        break;
                    } else if (entity.getEntityVersion() != entityInRedisCache.getEntityVersion()) {
                        logger.debug("Check version failed! Data has expired. Class: {}. Key ID: {}, unique ID: {}, current version: {}, checked version: {}.", entity.getClass().getSimpleName(), entity.getKeyID(), entity.getId(), entityInRedisCache.getEntityVersion(), entity.getEntityVersion());
                        checkVersionSuccess = false;
                        break;
                    }
                } else if (entity.getAction() == Constants.Action.INSERT) {
                    if (entityInRedisCache != null) {
                        logger.debug("Check version failed! Data was inserted. Class: {}. Key ID: {}, unique ID: {}.", entity.getClass().getSimpleName(), entity.getKeyID(), entity.getId());
                        checkVersionSuccess = false;
                        break;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Exception in checkVersion!", e);
            return false;
        }
        return checkVersionSuccess;
    }

    public void save(List<AbstractBaseEntity> entities) {
        RedisHashAsyncCommands<String, String> hashAsyncCommands = RedisManager.INSTANCE.getConnection().asyncHashCommands();
        for (AbstractBaseEntity entity : entities) {
            switch (entity.getAction()) {
                case INSERT:
                case UPDATE:
                    hashAsyncCommands.hset(getRedisDataKey(entity.getKeyID(), entity.getClass()), String.valueOf(entity.getId()), JSON.toJSONString(entity));
                    break;
                case DELETE:
                    hashAsyncCommands.hdel(getRedisDataKey(entity.getKeyID(), entity.getClass()), String.valueOf(entity.getId()));
                    break;
            }
        }
    }
}
