package pers.winter.test.save;

import pers.winter.bean.Transcript;
import pers.winter.config.ConfigManager;
import pers.winter.db.AbstractBaseEntity;
import pers.winter.db.DatabaseCenter;
import pers.winter.db.entity.Student;
import pers.winter.entity.EntityManager;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TestSave {
    private static void testInsert(){
        Set<AbstractBaseEntity> insertSet = new HashSet<>();
        for(short i = 0;i<10;i++){
            Student student = new Student();
            student.setBirthday(System.currentTimeMillis());
            student.setTranscript(i%2==0?null:new Transcript());
            student.setDormitory(1);
            student.setName("Name"+i);
            student.setSex(i);
            student.insert();
            insertSet.add(student);
        }
        EntityManager.INSTANCE.save(insertSet);
    }
    private static void testUpdateAndDelete(List<Student> students){
        Set<AbstractBaseEntity> set = new HashSet<>();
        for(int i = 0;i<students.size();i++){
            Student student = students.get(i);
            if(student.getTranscript() == null){
                student.setTranscript(new Transcript());
            }
            student.getTranscript().setChinese((short) 100);
            student.getTranscript().setMath((short) 100);
            student.getTranscript().setEnglish((short) 100);
            if(i%2==0){
                student.update();
            } else {
                student.delete();
            }
            set.add(student);
        }
        EntityManager.INSTANCE.save(set);
    }
    public static void main(String[] args) throws Throwable {
        ConfigManager.INSTANCE.init();
        DatabaseCenter.INSTANCE.init();
        List<Student> students = EntityManager.INSTANCE.selectByKey(1,Student.class);
        if(students.isEmpty()){
            testInsert();
            Thread.sleep(3000);
            students = EntityManager.INSTANCE.selectByKey(1,Student.class);
        }
        testUpdateAndDelete(students);
    }
}
