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
package pers.winter.threadpool.fair;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Task queue in the fair pool executor, caches the tasks of one producer.
 * @param <T> Class type for the tasks
 * @author Winter
 */
public class FairPoolUserQueue<T> {
    private final Object id;
    private volatile Queue<T> tasks = new LinkedList<>();
    private ReentrantLock lock = new ReentrantLock();
    private boolean isIdle = true;
    private long activeTs;
    private FairPoolExecutor boss;
    public FairPoolUserQueue(Object id, FairPoolExecutor boss){
        this.id = id;
        this.boss = boss;
    }

    /**
     * Take a task
     */
    public T takeWork(){
        lock.lock();
        try{
            T work = tasks.poll();
            return work;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Add a new task into the queue
     */
    @SuppressWarnings("unchecked")
    public void addWork(T task){
        lock.lock();
        try{
            tasks.add(task);
            activeTs = System.currentTimeMillis();
            if(isIdle){
                boss.scheduleWork(this);
                isIdle = false;
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Notify the queue when the worker thread completes the previous work.
     */
    @SuppressWarnings("unchecked")
    public void endWork(){
        lock.lock();
        try{
            if(tasks.isEmpty()){
                isIdle = true;
            } else {
                activeTs = System.currentTimeMillis();
                boss.scheduleWork(this);
            }
        } finally {
            lock.unlock();
        }
    }

    public int getLeftTasks(){
        return this.tasks.size();
    }

    /**
     * Whether the queue is empty && not in schedule
     */
    public boolean isIdle(){
        return this.isIdle;
    }

    /**
     * Last active timestamp
     */
    public long getActiveTs(){
        return activeTs;
    }
}
