package pers.winter.entity;

import com.alibaba.fastjson.JSON;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.conversions.Bson;
import pers.winter.cache.memory.MemoryCache;
import pers.winter.cache.thread.ThreadCacheManager;
import pers.winter.db.*;
import pers.winter.utils.SnowFlakeIdGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class EntityManager {
    public static final EntityManager INSTANCE = new EntityManager();
    private static final Logger logger = LogManager.getLogger(EntityManager.class);

    private MemoryCache memoryCache;
    private EntityManager(){}

    public void init() throws Exception {
        DatabaseCenter.INSTANCE.init();
        memoryCache = new MemoryCache();
    }

    public <T extends AbstractBaseEntity> List<T> selectByKey(long key, Class<T> entityClass) throws Exception{
        List<T> result;
        AnnTable annTable = entityClass.getAnnotation(AnnTable.class);
        if(annTable.cacheType() == Constants.CacheType.MEMORY){
            result = ThreadCacheManager.INSTANCE.selectByKey(key,entityClass);
            if(result != null){
                return result;
            }
            result = memoryCache.selectByKey(key,entityClass);
            if(result != null){
                ThreadCacheManager.INSTANCE.syncToCache(key, result,entityClass);
                return result;
            }
            memoryCache.lockCache(key,entityClass);
            try{
                result = memoryCache.selectByKey(key,entityClass);
                if(result == null){
                    result = DatabaseCenter.INSTANCE.selectByKey(key,entityClass);
                    memoryCache.syncToCache(key,result,entityClass);
                    result = memoryCache.selectByKey(key,entityClass);
                }
                ThreadCacheManager.INSTANCE.syncToCache(key, result,entityClass);
            }finally {
                memoryCache.unlockCache(key,entityClass);
            }
        } else {
            result = new ArrayList<>();
        }
        return result;
    }

    public <T extends AbstractBaseEntity> List<T> selectCustom(int dbID, String sql, Class<T> entityClass) throws Exception{
        List<T> entities = DatabaseCenter.INSTANCE.selectCustom(dbID, sql,entityClass);
        syncFromCache(entities, entityClass);
        return entities;
    }

    public <T extends AbstractBaseEntity> List<T> selectCustom(Bson queryBson, Class<T> entityClass) throws Exception{
        List<T> entities = DatabaseCenter.INSTANCE.selectCustom(0, queryBson, entityClass);
        syncFromCache(entities, entityClass);
        return entities;
    }

    private <T extends AbstractBaseEntity> void syncFromCache(List<T> entities, Class<T> entityClass){
        memoryCache.syncFromCache(entities,entityClass);
        ThreadCacheManager.INSTANCE.syncFromCache(entities,entityClass);
    }

    public Set<Integer> getAllMySqlDbID(){
        return DatabaseCenter.INSTANCE.getAllMySqlDbID();
    }

    public boolean save(Set<AbstractBaseEntity> entities) {
        List<AbstractBaseEntity> entitiesInRedis = new ArrayList<>();
        List<AbstractBaseEntity> entitiesInMemory = new ArrayList<>();
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
        }
        boolean memoryLocked = true;
        boolean redisLocked = true;
        try{
            if(!entitiesInMemory.isEmpty() && !memoryCache.lockEntities(entitiesInMemory)) {
                memoryLocked = false;
                return false;
            }
            if(!entitiesInRedis.isEmpty()){
                //TODO lock entities in redis
            }
            if(!entitiesInMemory.isEmpty() && !memoryCache.checkVersion(entitiesInMemory)){
                return false;
            }
            if(!entitiesInRedis.isEmpty()){
                //TODO check version in redis
            }
            for(AbstractBaseEntity entity:entities){
                EntityVersionProxy.setEntityVersion(entity, entity.getEntityVersion() + 1);
            }
            if(!entitiesInMemory.isEmpty()){
                memoryCache.save(entitiesInMemory);
            }
            if(!entitiesInRedis.isEmpty()){
                //TODO save in redis
            }
            DatabaseCenter.INSTANCE.save(entities);
        } finally {
            if(memoryLocked && !entitiesInMemory.isEmpty()){
                memoryCache.unlockEntities(entitiesInMemory);
            }
            if(redisLocked && !entitiesInRedis.isEmpty()){
                //TODO release lock in redis
            }
        }
        return true;
    }
}
