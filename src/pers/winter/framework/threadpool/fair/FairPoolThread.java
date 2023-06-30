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
package pers.winter.framework.threadpool.fair;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pers.winter.framework.threadpool.IExecutorHandler;

/**
 * Worker thread of {@link FairPoolExecutor}, based on Java thread.
 * @param <T> Class type of the tasks
 * @author Winter
 */
public class FairPoolThread<T> implements Thread.UncaughtExceptionHandler, Runnable {
    private static final Logger logger = LogManager.getLogger(FairPoolThread.class);
    private Thread thread;
    private volatile boolean interrupted = false;
    private String name;
    private final FairPoolExecutor<T> boss;
    private IExecutorHandler<T> handler;

    public FairPoolThread(String name, FairPoolExecutor<T> boss, IExecutorHandler<T> handler){
        this.name = name;
        this.boss = boss;
        this.handler = handler;
        initThread();
    }

    /**
     * Start the thread.
     */
    public void start(){
        thread.start();
    }

    /**
     * Interrupt the thread.
     * Different from Thread.interrupt(), the method will set the interrupted flag and never clear it.
     */
    public void interrupt(){
        interrupted = true;
        this.thread.interrupt();
    }

    /**
     * Get the state of thread.
     */
    public Thread.State getState(){
        return thread.getState();
    }

    @Override
    public void run() {
        while(!interrupted){
            try {
                FairPoolUserQueue<T> userQueue = boss.takeWork();
                T task = userQueue.takeWork();
                try{
                    handler.execute(task);
                }catch (Throwable e){
                    handler.exceptionCaught(task,e);
                } finally {
                    userQueue.endWork();
                }
            } catch (InterruptedException e) {
                logger.info("Thread {} interrupted!",Thread.currentThread().getName());
            }
        }
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        logger.error("Uncaught exception while executing! Thread: {}",this.name,e);
        if(!interrupted){
            initThread();
        }
    }

    private void initThread(){
        this.thread = new Thread(this);
        this.thread.setName(name);
        this.thread.setUncaughtExceptionHandler(this);
    }
}
