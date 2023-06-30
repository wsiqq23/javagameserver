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

import pers.winter.framework.cache.thread.ThreadCacheManager;
import pers.winter.framework.db.AbstractBaseEntity;

import java.util.Set;

public abstract class Transaction implements Runnable{
    private final String name;
    private int retryCount;
    public Transaction(String name,int retryCount){
        this(name);
        this.retryCount = retryCount;
    }
    public Transaction(String name){
        this.name = name;
    }
    public String getName(){
        return name;
    }
    public int getRetryCount(){
        return retryCount;
    }
    public void setRetryCount(int retryCount){
        this.retryCount = retryCount;
    }
    protected abstract void process();
    protected abstract void failed();
    @Override
    public void run() {
        ThreadCacheManager.INSTANCE.initThread();
        try{
            boolean result;
            int round = 0;
            do{
                process();
                result = commit();
                ThreadCacheManager.INSTANCE.clearCachedEntities();
            } while (!result && round++ < retryCount);
            if(!result){
                failed();
            }
        } finally {
            ThreadCacheManager.INSTANCE.removeThread();
        }
    }

    private boolean commit(){
        Set<AbstractBaseEntity> entitiesChanged = ThreadCacheManager.INSTANCE.getEntitiesChanged();
        if(entitiesChanged != null && !entitiesChanged.isEmpty()){
            return EntityManager.INSTANCE.save(entitiesChanged);
        }
        return true;
    }
}
