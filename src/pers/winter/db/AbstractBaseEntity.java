package pers.winter.db;

import org.bson.Document;
import pers.winter.cache.thread.ThreadCacheManager;
import pers.winter.db.mongo.ISerializableMongoObject;
import pers.winter.entity.ICloneable;

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
    public void setAction(Constants.Action action) {
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
