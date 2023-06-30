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
package pers.winter.framework.threadpool;

/**
 * The handler for thread pool executors.
 * Before put tasks into the executor, you must create an handler to deal with the tasks.
 * @param <T> What kind of tasks will be executed.
 */
public interface IExecutorHandler<T> {
    /**
     * The thread pool will call execute when deal with a task
     * @param task Current task.
     */
    void execute(T task) throws Throwable;

    /**
     * If thread pool gets an exception while calling {@link IExecutorHandler#execute(Object)}, it will call this method to deal with the exception.
     * @param task The task causing exception
     * @param cause The exception
     */
    void exceptionCaught(T task, Throwable cause);
}
