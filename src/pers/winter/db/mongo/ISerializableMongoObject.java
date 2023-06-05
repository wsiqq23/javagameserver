package pers.winter.db.mongo;

import org.bson.Document;

public interface ISerializableMongoObject {
    /**
     * Deserialize Mongodb Document data into object.
     */
    void fromDocument(Document document);
    /**
     * Serialize object data into Mongodb Document
     */
    Document toDocument();
}
