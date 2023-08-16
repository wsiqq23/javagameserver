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
package pers.winter.framework.timer;

import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pers.winter.framework.config.ApplicationConfig;
import pers.winter.framework.config.ConfigManager;
import pers.winter.framework.entity.Transaction;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * The TimerTaskManager class provides a mechanism to manage timer tasks using ScheduledExecutorService and HashedWheelTimer.
 * @author Winter
 */
public class TimerTaskManager {
    private static final Logger logger = LogManager.getLogger(TimerTaskManager.class);
    private static TimerTaskManager instance;

    private final ScheduledExecutorService service;
    private final HashedWheelTimer hashedWheelTimer;
    private TimerTaskManager(){
        int poolSize = ConfigManager.INSTANCE.getConfig(ApplicationConfig.class).getTimerThreadPoolCount()>0?ConfigManager.INSTANCE.getConfig(ApplicationConfig.class).getTimerThreadPoolCount():Runtime.getRuntime().availableProcessors();
        service = Executors.newScheduledThreadPool(poolSize);
        hashedWheelTimer = new HashedWheelTimer();
    }

    /**
     * Returns the singleton instance of TimerTaskManager.
     *
     * @return The TimerTaskManager instance.
     */
    public static TimerTaskManager getInstance(){
        if(instance == null){
            synchronized (TimerTaskManager.class){
                if(instance == null){
                    instance = new TimerTaskManager();
                }
            }
        }
        return instance;
    }

    /**
     * Schedules a task with a fixed delay.
     *
     * @param transaction   The transaction task to be scheduled.
     * @param initialDelay  The initial delay before the first execution.
     * @param delay         The delay between the termination of one execution and the commencement of the next.
     * @param timeUnit      The time unit for the initial delay and delay.
     * @return A ScheduledFuture representing the task.
     */
    public ScheduledFuture<?> scheduleWithFixedDelay(Transaction transaction, long initialDelay, long delay, TimeUnit timeUnit){
        logger.info("Schedule {} with fixed delay, initialDelay: {}, delay: {}, time unit:{}",transaction.getName(),initialDelay,delay,timeUnit);
        return service.scheduleWithFixedDelay(transaction,initialDelay,delay,timeUnit);
    }

    /**
     * Schedules a task at a fixed rate.
     *
     * @param transaction   The transaction task to be scheduled.
     * @param initialDelay  The initial delay before the first execution.
     * @param delay         The delay between the termination of one execution and the commencement of the next.
     * @param timeUnit      The time unit for the initial delay and delay.
     * @return A ScheduledFuture representing the task.
     */
    public ScheduledFuture<?> scheduleAtFixedRate(Transaction transaction, long initialDelay, long delay, TimeUnit timeUnit){
        logger.info("Schedule {} at fixed rate, initialDelay: {}, delay: {}, time unit: {}",transaction.getName(),initialDelay,delay,timeUnit);
        return service.scheduleAtFixedRate(transaction, initialDelay, delay, timeUnit);
    }

    /**
     * Schedules a task with a single execution.
     *
     * @param transaction   The transaction task to be scheduled.
     * @param delay         The delay before the task is executed.
     * @param timeUnit      The time unit for the delay.
     * @return A ScheduledFuture representing the task.
     */
    public ScheduledFuture<?> schedule(Transaction transaction, long delay, TimeUnit timeUnit){
        logger.info("Schedule {}, delay: {},time unit: {}",transaction.getName(),delay,timeUnit);
        return service.schedule(transaction,delay,timeUnit);
    }

    /**
     * Creates a new repeated timeout task using HashedWheelTimer.
     *
     * @param transaction    The transaction task to be scheduled.
     * @param initialDelay   The initial delay before the first execution.
     * @param delay          The delay between the termination of one execution and the commencement of the next.
     * @param timeUnit       The time unit for the initial delay and delay.
     * @param maxRepeatTime  The maximum number of times the task should be repeated (0 for unlimited).
     * @return A RepeatedTimerTask representing the repeated timeout task. User can use it to cancel the task.
     */
    public RepeatedTimerTask newRepeatedTimeout(Transaction transaction, long initialDelay, long delay, TimeUnit timeUnit,long maxRepeatTime){
        logger.info("HashedWheelTimer repeated timeout, task:{}, initialDelay: {}, delay: {}, time unit: {}, max repeat time: {}.",transaction.getName(),initialDelay,delay,timeUnit,maxRepeatTime);
        RepeatedTimerTask timerTask = new RepeatedTimerTask(transaction,delay,timeUnit,maxRepeatTime);
        timerTask.setTimeout(hashedWheelTimer.newTimeout(timerTask,initialDelay,timeUnit));
        return timerTask;
    }

    /**
     * Represents a repeated timer task that implements the TimerTask interface.
     */
    public static class RepeatedTimerTask implements TimerTask {
        private final Transaction transaction;
        private final long delay;
        private final TimeUnit timeUnit;
        private final long maxRepeatTime;
        private long curRepeatedTime = 0;
        private boolean isCanceled = false;
        private Timeout timeout;
        private RepeatedTimerTask(Transaction transaction, long delay,TimeUnit timeUnit, long maxRepeatTime){
            this.transaction = transaction;
            this.delay = delay;
            this.timeUnit = timeUnit;
            this.maxRepeatTime = maxRepeatTime;
        }
        @Override
        public void run(Timeout timeout) throws Exception {
            if(!isCanceled){
                try{
                    transaction.run();
                } finally {
                    if(!isCanceled && (maxRepeatTime == 0 || ++curRepeatedTime<maxRepeatTime)){
                        setTimeout(TimerTaskManager.getInstance().hashedWheelTimer.newTimeout(this,delay,timeUnit));
                    }
                }
            }
        }
        /**
         * @return The current repeated time.
         */
        public long getCurRepeatedTime(){
            return curRepeatedTime;
        }
        private void setTimeout(Timeout timeout){
            this.timeout = timeout;
        }
        /**
         * Cancels the task.
         */
        public void cancel(){
            timeout.cancel();
            isCanceled = true;
        }
    }
}
