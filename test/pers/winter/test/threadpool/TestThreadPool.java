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
package pers.winter.test.threadpool;

import pers.winter.threadpool.IExecutorHandler;
import pers.winter.threadpool.fair.FairPoolExecutor;

public class TestThreadPool {
    public static void testFairPool() throws InterruptedException {
        final int producerCount = 10;
        final int taskPerProducer = 10;
        FairPoolExecutor<Integer> executor = new FairPoolExecutor<Integer>("TestPool", 3, new IExecutorHandler<Integer>() {
            @Override
            public void execute(Integer task) throws InterruptedException {
                System.out.println("Do task:\t"+task/producerCount+"\t"+task%producerCount);
            }
            @Override
            public void exceptionCaught(Integer task, Throwable cause) {
                cause.printStackTrace();
                System.exit(-1);
            }
        });
        executor.start();
        for(int i = 0; i < producerCount; i++){
            Thread t = new Thread(()->{
                int index = Integer.parseInt(Thread.currentThread().getName());
                for(int j = 0;j<taskPerProducer;j++){
                    executor.add(index,index*taskPerProducer+j);
                }
            });
            t.setName(String.valueOf(i));
            t.start();
        }
        Thread.sleep(5000);
        System.out.println("New round=============");
        for(int i = 0; i < producerCount; i++){
            Thread t = new Thread(()->{
                int index = Integer.parseInt(Thread.currentThread().getName());
                for(int j = 0;j<taskPerProducer;j++){
                    executor.add(index,index*taskPerProducer+j);
                }
            });
            t.setName(String.valueOf(i));
            t.start();
        }
    }
    public static void main(String[] args) throws Exception {
        testFairPool();
    }
}
