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
package pers.winter.db.entity;

import org.bson.Document;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import pers.winter.framework.db.AbstractBaseEntity;
import pers.winter.framework.db.AnnTable;
import pers.winter.framework.db.Constants;
import pers.winter.framework.utils.CloneUtil;
import java.util.*;
import pers.winter.bean.Transcript;
import pers.winter.framework.utils.MongoCodecUtil;

/**
 * Demo entity
 * @author CodeGenerator
 */
@AnnTable(key = "dormitory", dbType = Constants.DBType.MONGO,  cacheType = Constants.CacheType.MEMORY,userCache = true)
public class Student extends AbstractBaseEntity {
    private short sex;
    private String name;
    private long birthday;
    private long dormitory;
    private Transcript transcript;
    private Map<String,Transcript> transcriptMap;
    private List<Transcript> transcriptList;
    private List<Short> shortList;

    /**
     * sex of the student
     */
    public short getSex(){
        return sex;
    }
    /**
     * sex of the student
     */
    public void setSex(short sex){
        this.sex = sex;
    }
    /**
     * name of the student
     */
    public String getName(){
        return name;
    }
    /**
     * name of the student
     */
    public void setName(String name){
        this.name = name;
    }
    /**
     * timestamp of the birthday of the student
     */
    public long getBirthday(){
        return birthday;
    }
    /**
     * timestamp of the birthday of the student
     */
    public void setBirthday(long birthday){
        this.birthday = birthday;
    }
    public long getDormitory(){
        return dormitory;
    }
    public void setDormitory(long dormitory){
        this.dormitory = dormitory;
    }
    public Transcript getTranscript(){
        return transcript;
    }
    public void setTranscript(Transcript transcript){
        this.transcript = transcript;
    }
    public Map<String,Transcript> getTranscriptMap(){
        return transcriptMap;
    }
    public void setTranscriptMap(Map<String,Transcript> transcriptMap){
        this.transcriptMap = transcriptMap;
    }
    public List<Transcript> getTranscriptList(){
        return transcriptList;
    }
    public void setTranscriptList(List<Transcript> transcriptList){
        this.transcriptList = transcriptList;
    }
    public List<Short> getShortList(){
        return shortList;
    }
    public void setShortList(List<Short> shortList){
        this.shortList = shortList;
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
        this.setTranscript(MongoCodecUtil.getBean(document,"transcript",Transcript.class));
        this.setTranscriptMap(MongoCodecUtil.getMap(document,"transcriptMap",Transcript.class));
        this.setTranscriptList(MongoCodecUtil.getList(document,"transcriptList",Transcript.class));
        this.setShortList(MongoCodecUtil.getList(document,"shortList",Short.class));
    }

    @Override
    public void fromResultSet(ResultSet rest) throws SQLException {
    }

    @Override
    public Document toDocument() {
        Document document = new Document();
        document.put("id", this.getId());
        document.put("entityVersion", this.getEntityVersion());
        document.put("sex",this.getSex());
        document.put("name",this.getName());
        document.put("birthday",this.getBirthday());
        document.put("dormitory",this.getDormitory());
        MongoCodecUtil.putBean(document,"transcript", this.getTranscript());
        MongoCodecUtil.putMap(document,"transcriptMap", this.getTranscriptMap(), Transcript.class);
        MongoCodecUtil.putList(document,"transcriptList", this.getTranscriptList(), Transcript.class);
        MongoCodecUtil.putList(document,"shortList", this.getShortList(), Short.class);
        return document;
    }

    @Override
    public void toPreparedStatement(PreparedStatement stat) throws SQLException {
    }

    @Override
    public Student deepClone(){
        Student student = new Student();
        student.setId(getId());
        student.setEntityVersion(getEntityVersion());
        student.setSex(this.getSex());
        student.setName(this.getName());
        student.setBirthday(this.getBirthday());
        student.setDormitory(this.getDormitory());
        student.setTranscript(CloneUtil.deepClone(this.getTranscript()));
        student.setTranscriptMap(CloneUtil.deepClone(this.getTranscriptMap()));
        student.setTranscriptList(CloneUtil.deepClone(this.getTranscriptList()));
        student.setShortList(CloneUtil.deepClone(this.getShortList()));
        return student;
    }
}