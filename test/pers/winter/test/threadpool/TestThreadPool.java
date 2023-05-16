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
