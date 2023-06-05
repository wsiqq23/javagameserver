package pers.winter.bean;

import org.bson.Document;
import pers.winter.db.mongo.ISerializableMongoObject;

public class Transcript implements ISerializableMongoObject {
    private short math;
    private short english;
    private short chinese;

    public short getMath() {
        return math;
    }

    public void setMath(short math) {
        this.math = math;
    }

    public short getEnglish() {
        return english;
    }

    public void setEnglish(short english) {
        this.english = english;
    }

    public short getChinese() {
        return chinese;
    }

    public void setChinese(short chinese) {
        this.chinese = chinese;
    }

    @Override
    public void fromDocument(Document document) {
        this.setMath((short) (int) document.get("math"));
        this.setEnglish((short) (int) document.get("chinese"));
        this.setChinese((short) (int) document.get("english"));
    }

    @Override
    public Document toDocument() {
        Document document = new Document();
        document.put("math", this.getMath());
        document.put("english", this.getEnglish());
        document.put("chinese", this.getChinese());
        return document;
    }
}
