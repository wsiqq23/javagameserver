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
package pers.winter.cache.memory;

import pers.winter.db.AbstractBaseEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CachedObject {
    private boolean dataInit = false;
    private final Lock lock = new ReentrantLock();
    private final Map<Long, AbstractBaseEntity> entities = new ConcurrentHashMap<>();
    private final Map<Long, Object> entityLocks = new ConcurrentHashMap<>();
    public boolean isDataInit() {
        return dataInit;
    }
    public void setDataInit(boolean dataInit) {
        this.dataInit = dataInit;
    }
    public void lockFull() {
        lock.lock();
    }
    public void unlockFull() {
        lock.unlock();
    }
    public boolean lockEntity(AbstractBaseEntity entity) {
        return this.entityLocks.putIfAbsent(entity.getId(), true) == null;
    }
    public void unlockEntity(AbstractBaseEntity entity) {
        this.entityLocks.remove(entity.getId());
    }
    public Map<Long, AbstractBaseEntity> getEntities() {
        return entities;
    }
    public AbstractBaseEntity getEntity(long uniqueID) {
        return entities.get(uniqueID);
    }
    public boolean contains(long uniqueID) {
        return entities.containsKey(uniqueID);
    }
    public void remove(AbstractBaseEntity entity) {
        entities.remove(entity.getId());
    }
    public void put(AbstractBaseEntity entity) {
        entities.put(entity.getId(), entity);
    }
}
