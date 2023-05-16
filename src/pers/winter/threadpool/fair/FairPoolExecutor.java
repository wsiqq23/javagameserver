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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pers.winter.threadpool.IExecutorHandler;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * A fair thread pool executor.
 * "Fair" means the executor will do one task for a user on the loop, and one task for next user on next loop.
 * Every user has chance to execute no matter how many tasks he owns.
 * @param <T> Class type of the tasks
 * @author Winter
 */
public class FairPoolExecutor<T> {
    private static final Logger logger = LogManager.getLogger(FairPoolExecutor.class);
    private final String name;
    private final IExecutorHandler<T> handler;
    private FairPoolThread[] threads;
    private LinkedBlockingQueue<FairPoolUserQueue<T>> taskSchedule = new LinkedBlockingQueue<>();
    private ConcurrentHashMap<Object, FairPoolUserQueue<T>> userTasks = new ConcurrentHashMap<>();
    private volatile State state = State.NEW;

    /**
     * Create an executor using default processors.
     * @param name Name of the executor.
     * @param handler Handler to deal with tasks.
     */
    public FairPoolExecutor(String name, IExecutorHandler<T> handler) {
        this(name, Runtime.getRuntime().availableProcessors(), handler);
    }

    /**
     * Create an executor using the specified count of threads.
     * @param name Name of the executor.
     * @param threadSize Count of threads created.
     * @param handler Handler to deal with tasks.
     */
    public FairPoolExecutor(String name, int threadSize, IExecutorHandler<T> handler) {
        this.name = name;
        this.handler = handler;
        threads = new FairPoolThread[threadSize];
        for(int i = 0; i < threadSize;i++){
            threads[i] = new FairPoolThread(String.format("%s-%d",this.name,i),this,this.handler);
        }
    }

    /**
     * Start the executor. Before put a task into the executor, you must start it.
     * @throws IllegalStateException Throws while the executor is already terminated
     */
    public void start() throws IllegalStateException {
        if(state != State.NEW){
            throw new IllegalStateException("Executor terminated!");
        }
        state = State.RUNNABLE;
        for(int i = 0;i<threads.length;i++){
            threads[i].start();
        }
    }

    /**
     * Terminate the executor.
     * Warning: once the executor was terminated, it cannot start up again, you must create a new executor if you want to use it.
     * @param discardRemains {@code true} to discard the remaining tasks, {@code  false} to complete the remaining tasks.
     */
    public void terminate(boolean discardRemains){
        if(state == State.TERMINATED || state == State.TERMINATING){
            return;
        }
        state = State.TERMINATING;
        for(int i = 0;i<threads.length;i++){
            threads[i].interrupt();
        }
        boolean allTerminated;
        do{
            allTerminated = true;
            for(int i = 0;i<threads.length;i++){
                Thread.State state = threads[i].getState();
                if(state != Thread.State.TERMINATED){
                    allTerminated = false;
                    break;
                }
            }
            try{
                Thread.sleep(1);
            }catch (InterruptedException e){
                logger.warn("Terminating executor interrupted!",e);
            }
        } while (!allTerminated);
        if(!discardRemains) {
            completeRemainingTasks();
        }
        state = State.TERMINATED;
    }

    /**
     * Complete the remaining tasks with a single thread.
     */
    private void completeRemainingTasks(){
        while(!taskSchedule.isEmpty()){
            try{
                FairPoolUserQueue<T> userQueue = taskSchedule.poll();
                if(userQueue != null){
                    T task = userQueue.takeWork();
                    try{
                        handler.execute(task);
                    }catch (Exception e){
                        handler.exceptionCaught(task,e);
                    } finally {
                        userQueue.endWork();
                    }
                }
            } catch (Exception e){
                logger.error("Uncaught exception while executing!",e);
            }
        }
    }

    /**
     * Add a task to executor, task will be put into the tail of the user queue and wait.
     * @param id Producer id
     * @param task Task to do
     */
    public void add(Object id, T task) throws IllegalStateException {
        if(state == State.TERMINATED){
            throw new IllegalStateException("Executor is terminated!");
        }
        FairPoolUserQueue<T> userQueue = this.userTasks.get(id);
        if(userQueue == null){
            this.userTasks.putIfAbsent(id, new FairPoolUserQueue<>(id,this));
            userQueue = this.userTasks.get(id);
        }
        userQueue.addWork(task);
    }

    /**
     * Get an estimation of numbers of the remaining tasks.
     * "Estimation" means the method does not calculate an exact value, because doing so would block all queues.
     */
    public long getEstimatedTaskCount(){
        long total = 0;
        for(FairPoolUserQueue<T> userQueue: userTasks.values()){
            if(!userQueue.isIdle()){
                total += userQueue.getLeftTasks();
            }
        }
        return total;
    }

    protected FairPoolUserQueue<T> takeWork() throws InterruptedException{
        return taskSchedule.take();
    }

    protected void scheduleWork(FairPoolUserQueue userQueue){
        taskSchedule.add(userQueue);
    }

    /**
     * State of the executor
     */
    public enum State {
        /** State for an executor which has not yet started. */
        NEW,
        /** State for an executor which has started. */
        RUNNABLE,
        /** The executor is terminating. */
        TERMINATING,
        /** The executor is terminated. */
        TERMINATED
    }
}
