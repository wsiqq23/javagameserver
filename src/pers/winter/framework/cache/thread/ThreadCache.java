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
package pers.winter.framework.cache.thread;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pers.winter.framework.db.AbstractBaseEntity;

import java.util.*;

public class ThreadCache {
    private static final Logger logger = LogManager.getLogger(ThreadCache.class);
    private Map<Class<? extends AbstractBaseEntity>, Map<Long, List<? extends AbstractBaseEntity>>> cache = new HashMap<>();
    private Set<AbstractBaseEntity> entitiesChanged = new HashSet<>();
    @SuppressWarnings("unchecked")
    public <T extends AbstractBaseEntity> List<T> selectByKey(long key, Class<T> entityClass) {
        if(cache.containsKey(entityClass)){
            return (List<T>) cache.get(entityClass).get(key);
        }
        return null;
    }
    public <T extends AbstractBaseEntity> void syncToCache(long key,List<T> entities,Class<T> entityClass){
        Map<Long,List<? extends AbstractBaseEntity>> clsCache = cache.get(entityClass);
        if(clsCache == null){
            clsCache = new HashMap<>();
            cache.put(entityClass,clsCache);
        }
        clsCache.put(key, entities);
    }
    public <T extends AbstractBaseEntity>  void syncFromCache(List<T> entities,Class<T> entityClass){
        Map<Long,List<? extends AbstractBaseEntity>> clsCache = cache.get(entityClass);
        if(clsCache != null) {
            for(int i = 0;i<entities.size();i++){
                AbstractBaseEntity entity = entities.get(i);
                if(clsCache.containsKey(entity.getKeyID())){
                    for(AbstractBaseEntity entityInCache:clsCache.get(entity.getKeyID())){
                        if(entityInCache.getId() == entity.getId()){
                            entities.set(i, (T) entityInCache);
                            break;
                        }
                    }
                }
            }
        }
    }
    @SuppressWarnings("unchecked")
    public <T extends AbstractBaseEntity> void entityChanges(T entity){
        entitiesChanged.add(entity);
        Map<Long, List<? extends AbstractBaseEntity>> entityMap;
        List<T> keyList;
        switch (entity.getAction()) {
            case DELETE:
                entityMap = cache.get(entity.getClass());
                if (entityMap != null) {
                    keyList = (List<T>) entityMap.get(entity.getKeyID());
                    if (keyList != null) {
                        keyList.remove(entity);
                    }
                }
                break;
            case INSERT:
                entityMap = cache.get(entity.getClass());
                if (entityMap == null) {
                    entityMap = new HashMap<>();
                    cache.put(entity.getClass(),entityMap);
                }
                keyList = (List<T>) entityMap.get(entity.getKeyID());
                if (keyList == null) {
                    keyList = new ArrayList<>();
                    entityMap.put(entity.getKeyID(),keyList);
                }
                keyList.add(entity);
                break;
        }
    }
    public Set<AbstractBaseEntity> getEntitiesChanged(){
        return entitiesChanged;
    }
    public void clearCachedEntities(){
        cache.clear();
        entitiesChanged.clear();
    }
}
