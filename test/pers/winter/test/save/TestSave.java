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
package pers.winter.test.save;

import pers.winter.bean.Transcript;
import pers.winter.framework.config.ConfigManager;
import pers.winter.db.entity.Student;
import pers.winter.framework.entity.EntityManager;
import pers.winter.framework.entity.Transaction;

import java.util.List;
import java.util.concurrent.CountDownLatch;

public class TestSave {
    private static void testInsert(){
        for(short i = 0;i<10;i++){
            Student student = new Student();
            student.setId(1000+i);
            student.setBirthday(System.currentTimeMillis());
            student.setTranscript(i%2==0?null:new Transcript());
            student.setDormitory(1);
            student.setName(Thread.currentThread().getName()+"Insert");
            student.setSex(i);
            student.insert();
        }
    }
    private static void testUpdate() throws Exception {
        List<Student> students = EntityManager.INSTANCE.selectByKey(1, Student.class);
        for(int i = 0;i<students.size();i++){
            Student student = students.get(i);
            student.setName(Thread.currentThread().getName());
            if(student.getTranscript() == null){
                student.setTranscript(new Transcript());
            }
            student.getTranscript().setChinese((short) 100);
            student.getTranscript().setMath((short) 100);
            student.getTranscript().setEnglish((short) 100);
            student.update();
        }
    }

    private static void testDelete() throws Exception{
        List<Student> students = EntityManager.INSTANCE.selectByKey(1, Student.class);
        while(!students.isEmpty()){
            students.get(students.size() - 1).delete();
        }
    }

    private static void testTransaction() throws Exception{
        new Transaction("TestTransaction",10) {
            @Override
            protected void process() {
                try{
                    testInsert();
                    testUpdate();
                    testDelete();
                } catch (Exception e){
                    e.printStackTrace();
                    System.exit(-1);
                }
            }
            @Override
            protected void failed() {
                System.out.println(Thread.currentThread().getName() + " failed!");
            }
        }.run();
    }

    public static void main(String[] args) throws Throwable {
        ConfigManager.INSTANCE.init();
        EntityManager.INSTANCE.init();
        List<Student> students = EntityManager.INSTANCE.selectByKey(1,Student.class);
        if(students.isEmpty()) {
            testInsert();
        }
        final int threadSize = 10;
        CountDownLatch cd = new CountDownLatch(threadSize);
        for(int i = 0;i<threadSize;i++){
            Thread t = new Thread(()->{
                try {
                    testTransaction();
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(-1);
                }
                cd.countDown();
            });
            t.setName("Thread"+i);
            t.start();
        }
        cd.await();
    }
}
