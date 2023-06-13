package pers.winter.cache.thread;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pers.winter.db.AbstractBaseEntity;

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
    public void entityChanges(AbstractBaseEntity entity){
        entitiesChanged.add(entity);
    }
    public Set<AbstractBaseEntity> getEntitiesChanged(){
        return entitiesChanged;
    }
    public void clearCachedEntities(){
        cache.clear();
        entitiesChanged.clear();
    }
}
