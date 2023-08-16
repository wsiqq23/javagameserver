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
package pers.winter.bean;

import org.bson.Document;
import pers.winter.framework.db.mongo.ISerializableMongoObject;
import pers.winter.framework.entity.ICloneable;

/**
 * Demo bean
 * @author CodeGenerator
 */
public class Transcript implements ICloneable, ISerializableMongoObject {
    private short math;
    private short english;
    private short chinese;

    public short getMath(){
        return math;
    }
    public void setMath(short math){
        this.math = math;
    }
    public short getEnglish(){
        return english;
    }
    public void setEnglish(short english){
        this.english = english;
    }
    public short getChinese(){
        return chinese;
    }
    public void setChinese(short chinese){
        this.chinese = chinese;
    }
    @Override
    public void fromDocument(Document document) {
        this.setMath((short) (int) document.get("math"));
        this.setEnglish((short) (int) document.get("english"));
        this.setChinese((short) (int) document.get("chinese"));
    }
    @Override
    public Document toDocument() {
        Document document = new Document();
        document.put("math",this.getMath());
        document.put("english",this.getEnglish());
        document.put("chinese",this.getChinese());
        return document;
    }
    @Override
    public Transcript deepClone(){
        Transcript transcript = new Transcript();
        transcript.setMath(this.getMath());
        transcript.setEnglish(this.getEnglish());
        transcript.setChinese(this.getChinese());
        return transcript;
    }
}