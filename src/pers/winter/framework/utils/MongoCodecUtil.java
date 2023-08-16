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
package pers.winter.framework.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import pers.winter.framework.db.mongo.ISerializableMongoObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MongoCodecUtil {
    private static final Logger logger = LogManager.getLogger(MongoCodecUtil.class);
    public static <T> List<T> getList(Document document,String key, Class<T> cls){
        if(!document.containsKey(key) || document.get(key) == null){
            return null;
        }
        if(cls.isPrimitive() || cls == String.class){
            return document.getList(key,cls);
        }
        if(ISerializableMongoObject.class.isAssignableFrom(cls)){
            try{
                List<Document> documents = document.getList(key,Document.class);
                List<T> result = new ArrayList<>(documents.size());
                for(Document doc:documents){
                    T object = cls.getDeclaredConstructor().newInstance();
                    ((ISerializableMongoObject)object).fromDocument(doc);
                    result.add(object);
                }
                return result;
            } catch (Exception e){
                logger.error("Exception while parsing document!",e);
                return null;
            }
        }
        logger.error("Unsupported class {} for list.",cls.getSimpleName());
        return null;
    }

    public static <T> void putList(Document document,String key, List<T> data, Class<T> cls){
        if(cls.isPrimitive() || cls == String.class || data == null){
            document.put(key,data);
        } else if(ISerializableMongoObject.class.isAssignableFrom(cls)){
            List<Document> documents = new ArrayList<>(data.size());
            for(T object:data){
                documents.add(((ISerializableMongoObject)object).toDocument());
            }
            document.put(key,documents);
        } else {
            logger.error("Unsupported class {} for list.",cls.getSimpleName());
            document.put(key,null);
        }
    }

    public static <T> Map<String,T> getMap(Document document,String key,Class<T> valueCls){
        if(!document.containsKey(key) || document.get(key) == null){
            return null;
        }
        Document map = document.get(key,Document.class);
        Map<String,T> result = new HashMap<>(map.size());
        if(valueCls.isPrimitive() || valueCls == String.class){
            for (Map.Entry<String,?> entry:map.entrySet()){
                result.put(entry.getKey(), (T) entry.getValue());
            }
            return result;
        }
        if(ISerializableMongoObject.class.isAssignableFrom(valueCls)){
            try{
                for (Map.Entry<String,Object> entry:map.entrySet()){
                    Document valueDoc = (Document) entry.getValue();
                    T object = valueCls.getDeclaredConstructor().newInstance();
                    ((ISerializableMongoObject)object).fromDocument(valueDoc);
                    result.put(entry.getKey(), object);
                }
                return result;
            } catch (Exception e){
                logger.error("Exception while parsing document!",e);
                return null;
            }
        }
        logger.error("Unsupported value class {} for map.",valueCls.getSimpleName());
        return null;
    }

    public static <T> void putMap(Document document,String key,Map<String,T> data,Class<T> valueCls){
        if(data == null){
            document.put(key,null);
        } else {
            Document docData = new Document();
            if(valueCls.isPrimitive() || valueCls == String.class){
                docData.putAll(data);
            } else if(ISerializableMongoObject.class.isAssignableFrom(valueCls)){
                for (Map.Entry<String,T> entry: data.entrySet()){
                    docData.put(entry.getKey(), ((ISerializableMongoObject)entry.getValue()).toDocument());
                }
            } else {
                logger.error("Unsupported value class {} for map.",valueCls.getSimpleName());
                docData = null;
            }
            document.put(key,docData);
        }

    }

    public static <T extends ISerializableMongoObject> T getBean(Document document,String key,Class<T> valueCls){
        if(!document.containsKey(key) || document.get(key) == null){
            return null;
        }
        Document docForBean = document.get(key,Document.class);
        T object = null;
        try {
            object = valueCls.getDeclaredConstructor().newInstance();
            object.fromDocument(docForBean);
        } catch (Exception e) {
            logger.error("Exception while parsing document!",e);
        }
        return object;
    }

    public static <T extends ISerializableMongoObject> void putBean(Document document,String key,T data){
        document.put(key,data == null?null:data.toDocument());
    }
}
