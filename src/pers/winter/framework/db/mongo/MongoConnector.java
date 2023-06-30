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
package pers.winter.framework.db.mongo;

import com.alibaba.fastjson.JSON;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.bson.conversions.Bson;
import pers.winter.framework.config.ConfigManager;
import pers.winter.framework.config.MongoDBConfig;
import pers.winter.framework.db.AbstractBaseEntity;
import pers.winter.framework.db.AbstractConnector;
import pers.winter.framework.db.AnnTable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MongoConnector extends AbstractConnector {
    private static final Logger logger = LogManager.getLogger(MongoConnector.class);
    private final MongoClient client;
    private final MongoDBConfig config;
    private final String databaseName;

    public MongoConnector() {
        this.config = ConfigManager.INSTANCE.getConfig(MongoDBConfig.class);
        this.databaseName = this.config.getDatabaseName();
        MongoClientOptions options = MongoClientOptions.builder().maxWaitTime(config.getMaxWaitTime())
                .connectTimeout(config.getConnectTimeout())
                .connectionsPerHost(config.getMaxPoolSize())
                .minConnectionsPerHost(config.getMinPoolSize())
                .maxConnectionIdleTime(config.getMaxConnectionIdleTime()).build();
        this.client = new MongoClient(config.getUrl(),options);
    }
    @Override
    public void insert(AbstractBaseEntity entity) throws Exception{
        MongoDatabase database = client.getDatabase(databaseName);
        MongoCollection<Document> collection = database.getCollection(entity.getClass().getSimpleName());
        collection.insertOne(entity.toDocument());
    }
    @Override
    public <T extends AbstractBaseEntity> List<T> selectByKey(long keyID,Class<T> cls) throws Exception{
        MongoDatabase database = client.getDatabase(databaseName);
        MongoCollection<Document> collection = database.getCollection(cls.getSimpleName());
        AnnTable annTable = cls.getAnnotation(AnnTable.class);
        FindIterable<Document> documents = collection.find(Filters.eq(annTable.key(),keyID));
        MongoCursor<Document> iterator = documents.iterator();
        List<T> entities = new ArrayList<>();
        while(iterator.hasNext()){
            Document document = iterator.next();
            try{
                T object = cls.getDeclaredConstructor().newInstance();
                object.fromDocument(document);
                entities.add(object);
            } catch (Exception e) {
                logger.error("Select from Mongo Exception!",e);
            }
        }
        return entities;
    }

    @Override
    public Set<Integer> getAllDbId() {
        return Set.of();
    }

    @Override
    public <T extends AbstractBaseEntity> List<T> selectCustom(int dbID, Object condition, Class<T> cls) throws Exception {
        if(!(condition instanceof Bson)){
            throw new IllegalArgumentException("Invalid bson condition.");
        }
        MongoDatabase database = client.getDatabase(databaseName);
        MongoCollection<Document> collection = database.getCollection(cls.getSimpleName());
        FindIterable<Document> documents = collection.find((Bson) condition);
        MongoCursor<Document> iterator = documents.iterator();
        List<T> entities = new ArrayList<>();
        while(iterator.hasNext()){
            Document document = iterator.next();
            try{
                T object = cls.getDeclaredConstructor().newInstance();
                object.fromDocument(document);
                entities.add(object);
            } catch (Exception e) {
                logger.error("Select from Mongo Exception!",e);
            }
        }
        return entities;
    }

    @Override
    public void delete(AbstractBaseEntity entity){
        MongoDatabase database = client.getDatabase(databaseName);
        MongoCollection<Document> collection = database.getCollection(entity.getClass().getSimpleName());
        DeleteResult result = collection.deleteOne(Filters.eq("id",entity.getId()));
        if(result.getDeletedCount() != 1){
            logger.warn("Delete {} failed! Data:{}",entity.getClass().getSimpleName(), JSON.toJSONString(entity));
        }
    }
    @Override
    public void update(AbstractBaseEntity entity) {
        MongoDatabase database = client.getDatabase(databaseName);
        MongoCollection<Document> collection = database.getCollection(entity.getClass().getSimpleName());
        Bson condition = Filters.and(Filters.eq("id",entity.getId()));
        Document update = entity.toDocument();
        collection.replaceOne(condition,update);
    }
}
