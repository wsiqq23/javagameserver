package pers.winter.cache.thread;

import pers.winter.db.AbstractBaseEntity;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The ThreadCacheManager class manages inside-thread caches for entities.
 * @author Winter
 */
public class ThreadCacheManager {
    public static final ThreadCacheManager INSTANCE = new ThreadCacheManager();
    private Map<Thread,ThreadCache> threadCaches = new ConcurrentHashMap<>();
    private ThreadCacheManager(){}
    /**
     * Initializes the thread cache for the current thread.
     */
    public void initThread(){
        threadCaches.put(Thread.currentThread(), new ThreadCache());
    }
    /**
     * Remove the thread cache for the current thread.
     */
    public void removeThread(){
        threadCaches.remove(Thread.currentThread());
    }
    /**
     * Retrieves entities from the thread cache based on the specified key and entity class.
     * @param key         The keyID to look up in the cache.
     * @param entityClass The class of the entities to retrieve.
     * @param <T>         The type of the entity extending AbstractBaseEntity.
     * @return The list of entities retrieved from the thread cache, or null if the cache is not found.
     */
    public <T extends AbstractBaseEntity> List<T> selectByKey(long key, Class<T> entityClass) {
        ThreadCache threadCache = threadCaches.get(Thread.currentThread());
        if(threadCache == null){
            return null;
        }
        return threadCache.selectByKey(key,entityClass);
    }
    /**
     * Synchronizes the specified entities to the thread cache for the given key and entity class.
     * @param key         The key associated with the entities.
     * @param entities    The entities to synchronize to the cache.
     * @param entityClass The class of the entities being synchronized.
     * @param <T>         The type of the entity extending AbstractBaseEntity.
     */
    public <T extends AbstractBaseEntity> void syncToCache(long key,List<T> entities,Class<T> entityClass){
        ThreadCache threadCache = threadCaches.get(Thread.currentThread());
        if(threadCache == null){
            return;
        }
        threadCache.syncToCache(key,entities,entityClass);
    }
    /**
     * Synchronizes the specified entities from the thread cache to the provided list.
     * @param entities    The list of entities to synchronize with the thread cache.
     * @param entityClass The class of the entities being synchronized.
     * @param <T>         The type of the entity extending AbstractBaseEntity.
     */
    public <T extends AbstractBaseEntity> void syncFromCache(List<T> entities,Class<T> entityClass){
        ThreadCache threadCache = threadCaches.get(Thread.currentThread());
        if(threadCache == null){
            return;
        }
        threadCache.syncFromCache(entities,entityClass);
    }
    /**
     * Tracks changes made to the specified entity in the thread cache.
     * @param entity The entity changed.
     */
    public void entityChanges(AbstractBaseEntity entity){
        ThreadCache threadCache = threadCaches.get(Thread.currentThread());
        if(threadCache == null){
            return;
        }
        threadCache.entityChanges(entity);
    }
    /**
     * Retrieves the set of entities that have been changed in the thread cache.
     * @return The set of entities that have been changed in the thread cache, or null if the cache is not found.
     */
    public Set<AbstractBaseEntity> getEntitiesChanged(){
        ThreadCache threadCache = threadCaches.get(Thread.currentThread());
        if(threadCache == null){
            return null;
        }
        return threadCache.getEntitiesChanged();
    }
    /**
     * Clears the cached entities in the thread cache.
     */
    public void clearCachedEntities(){
        ThreadCache threadCache = threadCaches.get(Thread.currentThread());
        if(threadCache == null){
            return;
        }
        threadCache.clearCachedEntities();
    }
}
