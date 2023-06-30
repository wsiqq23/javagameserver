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
package pers.winter.framework.db;

import com.alibaba.fastjson.annotation.JSONField;
import org.bson.Document;
import pers.winter.framework.cache.thread.ThreadCacheManager;
import pers.winter.framework.db.mongo.ISerializableMongoObject;
import pers.winter.framework.entity.ICloneable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * The superclass for all database entities
 */
public abstract class AbstractBaseEntity implements ISerializableMongoObject, ICloneable {
    private long id;
    private int entityVersion = 0;
    private transient Constants.Action action;
    /**
     * The primary identifier of the entity
     */
    public long getId() {
        return id;
    }
    /**
     * The primary identifier of the entity
     */
    public void setId(long id) {
        this.id = id;
    }
    public int getEntityVersion(){
        return entityVersion;
    }
    @JSONField(name="entityVersion")
    public void modifyEntityVersion(int entityVersion){
        this.entityVersion = entityVersion;
    }
    protected void setEntityVersion(int entityVersion){
        this.entityVersion = entityVersion;
    }
    /**
     * Get the name of the key.
     */
    public abstract String getKey();
    /**
     * The key id of the entity, can be not unique
     */
    public abstract long getKeyID();
    /**
     * Entity's action: insert or delete or update
     */
    public Constants.Action getAction() {
        return action;
    }
    /**
     * Entity's action: insert or delete or update
     * Priority: delete > insert > update
     */
    private void setAction(Constants.Action action) {
        if(this.action != null && this.action.toNumber() < action.toNumber()){
            return;
        }
        this.action = action;
        ThreadCacheManager.INSTANCE.entityChanges(this);
    }

    public void update(){
        setAction(Constants.Action.UPDATE);
    }

    public void insert(){
        setAction(Constants.Action.INSERT);
    }

    public void delete(){
        setAction(Constants.Action.DELETE);
    }

    @Override
    public abstract void fromDocument(Document document);
    /**
     * Deserialize MySQL ResultSet data into object.
     */
    public abstract void fromResultSet(ResultSet rest) throws SQLException;
    @Override
    public abstract Document toDocument();
    /**
     * Serialize object data into MySQL PreparedStatement
     */
    public abstract void toPreparedStatement(PreparedStatement stat) throws SQLException;
}
