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
package pers.winter.framework.entity;

import com.alibaba.fastjson.JSON;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.conversions.Bson;
import pers.winter.framework.cache.memory.MemoryCache;
import pers.winter.framework.cache.redis.RedisCache;
import pers.winter.framework.cache.thread.ThreadCacheManager;
import pers.winter.framework.db.AbstractBaseEntity;
import pers.winter.framework.db.AnnTable;
import pers.winter.framework.db.Constants;
import pers.winter.framework.db.DatabaseCenter;
import pers.winter.framework.redis.RedisManager;
import pers.winter.framework.utils.SnowFlakeIdGenerator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EntityManager {
    public static final EntityManager INSTANCE = new EntityManager();
    private static final Logger logger = LogManager.getLogger(EntityManager.class);

    private MemoryCache memoryCache;
    private RedisCache redisCache;

    private EntityManager(){}

    public void init() throws Exception {
        DatabaseCenter.INSTANCE.init();
        RedisManager.INSTANCE.init();
        memoryCache = new MemoryCache();
        redisCache = new RedisCache();
    }

    public <T extends AbstractBaseEntity> List<T> selectByKey(long key, Class<T> entityClass) throws Exception{
        List<T> result = ThreadCacheManager.INSTANCE.selectByKey(key,entityClass);
        if(result != null){
            return result;
        }
        AnnTable annTable = entityClass.getAnnotation(AnnTable.class);
        if(annTable.cacheType() == Constants.CacheType.MEMORY) {
            result = memoryCache.selectByKey(key, entityClass);
        } else {
            result = redisCache.selectByKey(key,entityClass);
        }
        if(result != null){
            ThreadCacheManager.INSTANCE.syncToCache(key, result,entityClass);
            return result;
        }
        if(annTable.cacheType() == Constants.CacheType.MEMORY) {
            memoryCache.lockCache(key,entityClass);
            try{
                result = memoryCache.selectByKey(key,entityClass);
                if(result == null){
                    result = DatabaseCenter.INSTANCE.selectByKey(key,entityClass);
                    memoryCache.syncToCache(key,result,entityClass);
                    result = memoryCache.selectByKey(key,entityClass);
                }
            } finally {
                memoryCache.unlockCache(key,entityClass);
            }
        } else {
            redisCache.lockCache(key,entityClass);
            try{
                result = redisCache.selectByKey(key,entityClass);
                if(result == null){
                    result = DatabaseCenter.INSTANCE.selectByKey(key,entityClass);
                    redisCache.syncToCache(key,result,entityClass);
                }
            } finally {
                redisCache.unlockCache(key,entityClass);
            }
        }
        ThreadCacheManager.INSTANCE.syncToCache(key, result,entityClass);
        return result;
    }

    /**
     * Query data by custom condition from MySQL.
     * WARNING: the entities returned by this method cannot be saved directly.
     * If user wants to save, call {@link #buildCache(long, Class)} first.
     * @param sql the sql statement for query
     * @param entityClass the class for the entity to be queried
     * @return all entities match the condition
     * @throws Exception while exception occurs.
     */
    public <T extends AbstractBaseEntity> List<T> selectCustom(int dbID, String sql, Class<T> entityClass) throws Exception{
        List<T> entities = DatabaseCenter.INSTANCE.selectCustom(dbID, sql,entityClass);
        syncFromCache(entities, entityClass);
        return entities;
    }

    /**
     * Query data by custom condition from Mongodb.
     * WARNING: the entities returned by this method cannot be saved directly.
     * If user wants to save, call {@link #buildCache(long, Class)} first.
     * @param queryBson the Bson for query
     * @param entityClass the class for the entity to be queried
     * @return all entities match the condition
     * @throws Exception while exception occurs.
     */
    public <T extends AbstractBaseEntity> List<T> selectCustom(Bson queryBson, Class<T> entityClass) throws Exception{
        List<T> entities = DatabaseCenter.INSTANCE.selectCustom(0, queryBson, entityClass);
        syncFromCache(entities, entityClass);
        return entities;
    }

    /**
     * Build cache by the specific key.
     * If a keyID is newly created or got by selectByCustom, this method must be called before save.
     */
    public <T extends AbstractBaseEntity> void buildCache(long key,Class<T> entityClass) throws Exception {
        List<T> entities;
        AnnTable annTable = entityClass.getAnnotation(AnnTable.class);
        if(annTable.cacheType() == Constants.CacheType.MEMORY) {
            entities = memoryCache.selectByKey(key, entityClass);
        } else {
            entities = redisCache.selectByKey(key,entityClass);
        }
        if(entities != null){
            return;
        }
        if(annTable.cacheType() == Constants.CacheType.MEMORY) {
            memoryCache.lockCache(key,entityClass);
            try{
                entities = memoryCache.selectByKey(key,entityClass);
                if(entities == null){
                    entities = DatabaseCenter.INSTANCE.selectByKey(key,entityClass);
                    memoryCache.syncToCache(key,entities,entityClass);
                }
            } finally {
                memoryCache.unlockCache(key,entityClass);
            }
        } else {
            redisCache.lockCache(key,entityClass);
            try{
                entities = redisCache.selectByKey(key,entityClass);
                if(entities == null){
                    entities = DatabaseCenter.INSTANCE.selectByKey(key,entityClass);
                    redisCache.syncToCache(key,entities,entityClass);
                }
            } finally {
                redisCache.unlockCache(key,entityClass);
            }
        }
    }

    private <T extends AbstractBaseEntity> void syncFromCache(List<T> entities, Class<T> entityClass){
        AnnTable annTable = entityClass.getAnnotation(AnnTable.class);
        if(annTable.cacheType() == Constants.CacheType.MEMORY) {
            memoryCache.syncFromCache(entities, entityClass);
        } else {
            redisCache.syncFromCache(entities,entityClass);
        }
        ThreadCacheManager.INSTANCE.syncFromCache(entities,entityClass);
    }

    public Set<Integer> getAllMySqlDbID(){
        return DatabaseCenter.INSTANCE.getAllMySqlDbID();
    }

    public void saveWithoutCache(Set<AbstractBaseEntity> entities) {
        Set<AbstractBaseEntity> entitiesToSave = new HashSet<>();
        for(AbstractBaseEntity entity:entities){
            if(entity.getAction() == null){
                logger.info("Action undefined for {}, data: {}",entity.getClass().getSimpleName(), JSON.toJSONString(entity));
                continue;
            }
            if(entity.getAction() == Constants.Action.INSERT && entity.getId() == 0) {
                entity.setId(SnowFlakeIdGenerator.generateId());
            }
            entitiesToSave.add(entity);
        }
        DatabaseCenter.INSTANCE.save(entitiesToSave);
    }

    public boolean save(Set<AbstractBaseEntity> entities) {
        List<AbstractBaseEntity> entitiesInRedis = new ArrayList<>();
        List<AbstractBaseEntity> entitiesInMemory = new ArrayList<>();
        Set<AbstractBaseEntity> entitiesToSave = new HashSet<>();
        for(AbstractBaseEntity entity:entities){
            if(entity.getAction() == null){
                logger.info("Action undefined for {}, data: {}",entity.getClass().getSimpleName(), JSON.toJSONString(entity));
                continue;
            }
            if(entity.getAction() == Constants.Action.INSERT && entity.getId() == 0) {
                entity.setId(SnowFlakeIdGenerator.generateId());
            }
            AnnTable annTable = entity.getClass().getAnnotation(AnnTable.class);
            if(annTable.cacheType() == Constants.CacheType.REDIS){
                entitiesInRedis.add(entity);
            } else {
                entitiesInMemory.add(entity);
            }
            entitiesToSave.add(entity);
        }
        boolean memoryLocked = true;
        boolean redisLocked = true;
        try{
            if(!entitiesInMemory.isEmpty() && !memoryCache.lockEntities(entitiesInMemory)) {
                logger.debug("Failed to lock memory.");
                memoryLocked = false;
                return false;
            }
            if(!entitiesInRedis.isEmpty() && !redisCache.lockEntities(entitiesInRedis)) {
                logger.debug("Failed to lock redis.");
                redisLocked = false;
                return false;
            }
            if(!entitiesInMemory.isEmpty() && !memoryCache.checkVersion(entitiesInMemory)){
                logger.debug("Failed to check memory version.");
                return false;
            }
            if(!entitiesInRedis.isEmpty() && !redisCache.checkVersion(entitiesInRedis)){
                logger.debug("Failed to check redis version.");
                return false;
            }
            for(AbstractBaseEntity entity:entitiesToSave){
                entity.modifyEntityVersion(entity.getEntityVersion() + 1);
            }
            if(!entitiesInMemory.isEmpty()){
                memoryCache.save(entitiesInMemory);
            }
            if(!entitiesInRedis.isEmpty()){
                redisCache.save(entitiesInRedis);
            }
            DatabaseCenter.INSTANCE.save(entitiesToSave);
        } finally {
            if(memoryLocked && !entitiesInMemory.isEmpty()){
                memoryCache.unlockEntities(entitiesInMemory);
            }
            if(redisLocked && !entitiesInRedis.isEmpty()){
                redisCache.unlockEntities(entitiesInRedis);
            }
        }
        return true;
    }
}
