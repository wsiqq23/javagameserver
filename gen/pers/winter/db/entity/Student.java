package pers.winter.db.entity;

import com.alibaba.fastjson.JSON;
import org.bson.Document;
import pers.winter.bean.Transcript;
import pers.winter.db.AbstractBaseEntity;
import pers.winter.db.AnnTable;
import pers.winter.db.Constants;
import pers.winter.db.dao.StudentDao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@AnnTable(key = "dormitory", dbType = Constants.DBType.MYSQL, daoClass = StudentDao.class, cacheType = Constants.CacheType.MEMORY,userCache = true)
public class Student extends AbstractBaseEntity {
    private short sex;
    private String name;
    private long birthday;
    private long dormitory;
    private Transcript transcript;

    public short getSex() {
        return sex;
    }

    public void setSex(short sex) {
        this.sex = sex;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getBirthday() {
        return birthday;
    }

    public void setBirthday(long birthday) {
        this.birthday = birthday;
    }

    public long getDormitory() {
        return dormitory;
    }

    public void setDormitory(long dormitory) {
        this.dormitory = dormitory;
    }

    public Transcript getTranscript() {
        return transcript;
    }

    public void setTranscript(Transcript transcript) {
        this.transcript = transcript;
    }

    @Override
    public String getKey() {
        return "dormitory";
    }

    @Override
    public long getKeyID() {
        return getDormitory();
    }

    @Override
    public void fromDocument(Document document) {
        this.setId((long) document.get("id"));
        this.setEntityVersion((int) document.get("entityVersion"));
        this.setSex((short) (int) document.get("sex"));
        this.setName((String) document.get("name"));
        this.setBirthday((long) document.get("birthday"));
        this.setDormitory((long) document.get("dormitory"));
        if (document.get("transcript") == null) {
            this.setTranscript(null);
        } else {
            this.setTranscript(new Transcript());
            this.getTranscript().fromDocument(document.get("transcript", Document.class));
        }
    }

    @Override
    public void fromResultSet(ResultSet rest) throws SQLException {
        this.setId(rest.getLong(1));
        this.setEntityVersion(rest.getInt(2));
        this.setSex(rest.getShort(3));
        this.setName(rest.getString(4));
        this.setBirthday(rest.getLong(5));
        this.setDormitory(rest.getLong(6));
        this.setTranscript(JSON.parseObject(rest.getString(7), Transcript.class));
    }

    @Override
    public Document toDocument() {
        Document document = new Document();
        document.put("id", this.getId());
        document.put("entityVersion", this.getEntityVersion());
        document.put("sex", this.getSex());
        document.put("name", this.getName());
        document.put("birthday", this.getBirthday());
        document.put("dormitory", this.getDormitory());
        document.put("transcript", this.getTranscript() == null ? null : this.getTranscript().toDocument());
        return document;
    }

    @Override
    public void toPreparedStatement(PreparedStatement stat) throws SQLException {
        stat.setLong(1, this.getId());
        stat.setInt(2, this.getEntityVersion());
        stat.setShort(3, this.getSex());
        stat.setString(4, this.getName());
        stat.setLong(5, this.getBirthday());
        stat.setLong(6, this.getDormitory());
        stat.setString(7, JSON.toJSONString(this.getTranscript()));
    }

    @Override
    public Student deepClone(){
        Student student = new Student();
        student.setId(getId());
        student.setEntityVersion(getEntityVersion());
        student.setSex(getSex());
        student.setName(getName());
        student.setBirthday(getBirthday());
        student.setDormitory(getDormitory());
        student.setTranscript(getTranscript() == null?null:getTranscript().deepClone());
        return student;
    }
}
