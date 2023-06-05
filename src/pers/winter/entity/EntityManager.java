package pers.winter.entity;

import com.alibaba.fastjson.JSON;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.conversions.Bson;
import pers.winter.db.AbstractBaseEntity;
import pers.winter.db.Constants;
import pers.winter.db.DatabaseCenter;
import pers.winter.db.EntityVersionProxy;
import pers.winter.utils.SnowFlakeIdGenerator;

import java.util.List;
import java.util.Set;

public class EntityManager {
    public static final EntityManager INSTANCE = new EntityManager();
    private static final Logger logger = LogManager.getLogger(EntityManager.class);
    private EntityManager(){}

    public <T extends AbstractBaseEntity> T select(long id, Class<T> entityClass) throws Exception{
        return DatabaseCenter.INSTANCE.select(id,entityClass);
    }

    public <T extends AbstractBaseEntity> List<T> selectByKey(long key, Class<T> entityClass) throws Exception{
        return DatabaseCenter.INSTANCE.selectByKey(key,entityClass);
    }

    public <T extends AbstractBaseEntity> List<T> selectCustom(int dbID, String sql, Class<T> entityClass) throws Exception{
        return DatabaseCenter.INSTANCE.selectCustom(dbID, sql,entityClass);
    }

    public <T extends AbstractBaseEntity> List<T> selectCustom(Bson queryBson, Class<T> entityClass) throws Exception{
        return DatabaseCenter.INSTANCE.selectCustom(0, queryBson, entityClass);
    }

    public Set<Integer> getAllMySqlDbID(){
        return DatabaseCenter.INSTANCE.getAllMySqlDbID();
    }

    public boolean save(Set<AbstractBaseEntity> entities) {
        for(AbstractBaseEntity entity:entities){
            if(entity.getAction() == null){
                logger.info("Action undefined for {}, data: {}",entity.getClass().getSimpleName(), JSON.toJSONString(entity));
                continue;
            }
            if(entity.getAction() == Constants.Action.INSERT && entity.getId() == 0) {
                entity.setId(SnowFlakeIdGenerator.generateId());
            }
            EntityVersionProxy.setEntityVersion(entity, entity.getEntityVersion() + 1);
        }
        DatabaseCenter.INSTANCE.save(entities);
        return true;
    }
}
