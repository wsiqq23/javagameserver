package pers.winter.db;

import com.alibaba.fastjson.JSON;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pers.winter.config.ApplicationConfig;
import pers.winter.config.ConfigManager;
import pers.winter.db.mongo.MongoConnector;
import pers.winter.db.mysql.MySqlConnector;
import pers.winter.threadpool.IExecutorHandler;
import pers.winter.threadpool.fair.FairPoolExecutor;
import pers.winter.utils.SnowFlakeIdGenerator;

import java.util.List;
import java.util.Set;

public class DatabaseCenter {
    private static final Logger logger = LogManager.getLogger(DatabaseCenter.class);
    public static final DatabaseCenter INSTANCE = new DatabaseCenter();

    private MySqlConnector mySqlConnector;
    private MongoConnector mongoConnector;
    private FairPoolExecutor<AbstractBaseEntity> executor;
    private DatabaseCenter(){}

    public void init() throws Exception{
        SnowFlakeIdGenerator.initNodeID(ConfigManager.INSTANCE.getConfig(ApplicationConfig.class).getNodeID());
        mySqlConnector = new MySqlConnector();
        mongoConnector = new MongoConnector();
        startExecutor();
    }

    private void startExecutor(){
        executor = new FairPoolExecutor<>("DatabaseExecutor", ConfigManager.INSTANCE.getConfig(ApplicationConfig.class).getDatabaseThreadPoolCount(), new IExecutorHandler<AbstractBaseEntity>() {
            @Override
            public void execute(AbstractBaseEntity task) throws Throwable {
                AbstractConnector connector = getConnector(task.getClass());
                switch (task.getAction()){
                    case DELETE:
                        connector.delete(task);
                        break;
                    case INSERT:
                        connector.insert(task);
                        break;
                    case UPDATE:
                        connector.update(task);
                        break;
                    default:
                        throw new IllegalArgumentException("Action invalid.");
                }
            }

            @Override
            public void exceptionCaught(AbstractBaseEntity task, Throwable cause) {
                logger.error("Exception while saving {}, data: {}",task.getClass().getSimpleName(), JSON.toJSONString(task),cause);
            }
        });
        executor.start();
    }

    private AbstractConnector getConnector(Class<? extends AbstractBaseEntity> entityClass){
        AnnTable annTable = entityClass.getAnnotation(AnnTable.class);
        if(annTable.dbType() == Constants.DBType.MONGO){
            return mongoConnector;
        } else {
            return mySqlConnector;
        }
    }

    public <T extends AbstractBaseEntity> T select(long id, Class<T> entityClass) throws Exception{
        AbstractConnector connector = getConnector(entityClass);
        return connector.select(id,entityClass);
    }

    public <T extends AbstractBaseEntity> List<T> selectByKey(long id, Class<T> entityClass) throws Exception{
        AbstractConnector connector = getConnector(entityClass);
        return connector.selectByKey(id,entityClass);
    }

    public <T extends AbstractBaseEntity> List<T> selectCustom(int dbID, Object condition, Class<T> entityClass) throws Exception{
        AbstractConnector connector = getConnector(entityClass);
        return connector.selectCustom(dbID, condition,entityClass);
    }

    public Set<Integer> getAllMySqlDbID(){
        return mySqlConnector.getAllDbId();
    }

    public void save(Set<AbstractBaseEntity> entities){
        for(AbstractBaseEntity entity:entities){
            executor.add(entity.getKeyID(),entity);
        }
    }
}
