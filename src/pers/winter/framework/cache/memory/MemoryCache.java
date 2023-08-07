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
package pers.winter.framework.cache.memory;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pers.winter.framework.config.ApplicationConfig;
import pers.winter.framework.config.ConfigManager;
import pers.winter.framework.db.AbstractBaseEntity;
import pers.winter.framework.db.AnnTable;
import pers.winter.framework.db.Constants;
import pers.winter.framework.utils.ClassScanner;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class MemoryCache {
    private static final Logger logger = LogManager.getLogger(MemoryCache.class);
    private final int CACHE_EXPIRE_TIME;
    private Cache<Long, Map<Class<? extends AbstractBaseEntity>,CachedObject>> userCaches;
    private final Map<Class<? extends AbstractBaseEntity>, Cache<Long, CachedObject>> otherCaches = new HashMap<>();

    public MemoryCache(){
        CACHE_EXPIRE_TIME = ConfigManager.INSTANCE.getConfig(ApplicationConfig.class).getCacheExpire();
        buildUserCaches();
        buildOtherCaches();
    }

    private void buildUserCaches(){
        userCaches = Caffeine.newBuilder().expireAfterAccess(CACHE_EXPIRE_TIME, TimeUnit.SECONDS).build();
    }

    @SuppressWarnings("unchecked")
    public <T extends AbstractBaseEntity> List<T> selectByKey(long key, Class<T> entityClass){
        AnnTable annTable = entityClass.getAnnotation(AnnTable.class);
        CachedObject targetCache = null;
        if(annTable.userCache()){
            Map<Class<? extends AbstractBaseEntity>,CachedObject> userCache = userCaches.getIfPresent(key);
            if(userCache != null){
                targetCache = userCache.get(entityClass);
            }
        } else {
            targetCache = otherCaches.get(entityClass).getIfPresent(key);
        }
        if(targetCache != null && targetCache.isDataInit()){
            Collection<AbstractBaseEntity> entities = targetCache.getEntities().values();
            List<T> result = new ArrayList<>();
            for(AbstractBaseEntity entity:entities){
                result.add((T) entity.deepClone());
            }
            return result;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private void buildOtherCaches(){
        List<Class<?>> classes = ClassScanner.getTypesAnnotatedWith(AnnTable.class);
        for(Class<?> cls:classes){
            AnnTable annTable = cls.getAnnotation(AnnTable.class);
            if(!annTable.userCache() && annTable.cacheType() == Constants.CacheType.MEMORY){
                otherCaches.put((Class<? extends AbstractBaseEntity>) cls,Caffeine.newBuilder().expireAfterAccess(CACHE_EXPIRE_TIME, TimeUnit.SECONDS).build());
            }
        }
    }

    public void lockCache(long key, Class<? extends AbstractBaseEntity> entityClass){
        getInitCachedObject(key,entityClass).lockFull();
    }

    public void unlockCache(long key, Class<? extends AbstractBaseEntity> entityClass){
        getInitCachedObject(key,entityClass).unlockFull();
    }

    private CachedObject getInitCachedObject(long key, Class<? extends AbstractBaseEntity> entityClass){
        AnnTable annTable = entityClass.getAnnotation(AnnTable.class);
        CachedObject targetCache;
        if(annTable.userCache()){
            Map<Class<? extends AbstractBaseEntity>,CachedObject> userCache = userCaches.get(key, k-> new ConcurrentHashMap<>());
            if(!userCache.containsKey(entityClass)){
                userCache.putIfAbsent(entityClass,new CachedObject());
            }
            targetCache = userCache.get(entityClass);
        } else {
            targetCache = otherCaches.get(entityClass).get(key,k->new CachedObject());
        }
        return targetCache;
    }

    private CachedObject getCachedObject(AbstractBaseEntity entity){
        AnnTable annTable = entity.getClass().getAnnotation(AnnTable.class);
        if(annTable.userCache()){
            return userCaches.getIfPresent(entity.getKeyID()).get(entity.getClass());
        } else {
            return otherCaches.get(entity.getClass()).getIfPresent(entity.getKeyID());
        }
    }

    public <T extends AbstractBaseEntity> void syncToCache(long key,List<T> entities,Class<T> entityClass){
        CachedObject targetCache = getInitCachedObject(key,entityClass);
        for(T entity:entities){
            targetCache.put(entity);
        }
        targetCache.setDataInit(true);
    }

    @SuppressWarnings("unchecked")
    public <T extends AbstractBaseEntity> void syncFromCache(List<T> entities, Class<T> entityClass){
        AnnTable annTable = entityClass.getAnnotation(AnnTable.class);
        CachedObject targetCache;
        if(annTable.userCache()){
            for(int i = 0;i<entities.size();i++){
                T entity = entities.get(i);
                Map<Class<? extends AbstractBaseEntity>,CachedObject> userCache = userCaches.getIfPresent(entity.getKeyID());
                if(userCache != null){
                    targetCache = userCache.get(entityClass);
                    if(targetCache != null){
                        T cachedEntity = (T) targetCache.getEntity(entity.getId());
                        if(cachedEntity != null){
                            entities.set(i, (T) cachedEntity.deepClone());
                        }
                    }
                }
            }
        } else {
            for(int i = 0;i<entities.size();i++){
                T entity = entities.get(i);
                Cache<Long, CachedObject> cache = otherCaches.get(entityClass);
                targetCache = cache.getIfPresent(entity.getKeyID());
                if(targetCache != null){
                    T cachedEntity = (T) targetCache.getEntity(entity.getId());
                    if(cachedEntity != null){
                        entities.set(i, (T) cachedEntity.deepClone());
                    }
                }
            }
        }
    }

    public boolean lockEntities(List<AbstractBaseEntity> entities){
        List<AbstractBaseEntity> lockedEntities = new ArrayList<>();
        boolean lockSuccess = true;
        try{
            for(AbstractBaseEntity entity:entities){
                CachedObject targetCache = getCachedObject(entity);
                if(targetCache.lockEntity(entity)){
                    lockedEntities.add(entity);
                } else {
                    lockSuccess = false;
                    logger.debug("Failed to lock {} in memory. Key ID: {}, unique ID: {}.",entity.getClass().getSimpleName(),entity.getKeyID(), entity.getId());
                    break;
                }
            }
        } finally {
            if(!lockSuccess && !lockedEntities.isEmpty()){
                unlockEntities(lockedEntities);
            }
        }
        return lockSuccess;
    }

    public void unlockEntities(List<AbstractBaseEntity> entities){
        for(AbstractBaseEntity entity:entities){
            CachedObject targetCache = getCachedObject(entity);
            targetCache.unlockEntity(entity);
        }
    }

    public boolean checkVersion(List<AbstractBaseEntity> entities){
        boolean checkVersionSuccess = true;
        for(AbstractBaseEntity entity:entities){
            CachedObject targetCache = getCachedObject(entity);
            if(entity.getAction() == Constants.Action.UPDATE){
                AbstractBaseEntity entityInMemoryCache = targetCache.getEntity(entity.getId());
                if(entityInMemoryCache == null){
                    logger.debug("Check version failed! Data was deleted. Class: {}. Key ID: {}, unique ID: {}.",entity.getClass().getSimpleName(),entity.getKeyID(), entity.getId());
                    checkVersionSuccess = false;
                    break;
                } else if(entity.getEntityVersion() != entityInMemoryCache.getEntityVersion()){
                    logger.debug("Check version failed! Data has expired. Class: {}. Key ID: {}, unique ID: {}, current version: {}, checked version: {}.",entity.getClass().getSimpleName(),entity.getKeyID(), entity.getId(),entityInMemoryCache.getEntityVersion(),entity.getEntityVersion());
                    checkVersionSuccess = false;
                    break;
                }
            } else if (entity.getAction() == Constants.Action.INSERT){
                if(targetCache.contains(entity.getId())){
                    logger.debug("Check version failed! Data was inserted. Class: {}. Key ID: {}, unique ID: {}.",entity.getClass().getSimpleName(),entity.getKeyID(), entity.getId());
                    checkVersionSuccess = false;
                    break;
                }
            }
        }
        return checkVersionSuccess;
    }

    public void save(List<AbstractBaseEntity> entities){
        for(AbstractBaseEntity entity:entities){
            CachedObject targetCache = getCachedObject(entity);
            switch (entity.getAction()){
                case INSERT:
                case UPDATE:
                    targetCache.put(entity);
                    break;
                case DELETE:
                    targetCache.remove(entity);
                    break;
            }
        }
    }
}
