package pers.winter.db.mysql;

import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.alibaba.fastjson.JSON;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pers.winter.config.AnnConfig;
import pers.winter.config.ConfigManager;
import pers.winter.config.MySqlConfig;
import pers.winter.db.AbstractBaseEntity;
import pers.winter.db.AbstractConnector;
import pers.winter.db.AnnTable;
import pers.winter.db.Constants;
import pers.winter.utils.ClassScanner;

import javax.sql.DataSource;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;

public class MySqlConnector extends AbstractConnector {
    private static final Logger logger = LogManager.getLogger(MySqlConnector.class);

    private final int dbCount;
    private final Map<Integer, DataSource> sourceMap;
    private IShardingStrategy shardingStrategy;
    private Map<Class<?>, AbstractBaseDao> daoMap = new HashMap<>();

    public MySqlConnector() throws Exception {
        this.dbCount = ConfigManager.INSTANCE.getConfig(MySqlConfig.class).getDbCount();
        this.sourceMap = new HashMap<>(dbCount);
        initPool();
        initShardingStrategy();
        createDaos();
    }

    private void initPool() throws Exception {
        InputStream is = ConfigManager.class.getClassLoader().getResourceAsStream(MySqlConfig.class.getAnnotation(AnnConfig.class).filePath());
        Properties properties = new Properties();
        properties.load(is);
        is.close();
        for(int i = 0;i<dbCount;i++){
            properties.setProperty("url",String.format(String.valueOf(properties.get("url")),i));
            sourceMap.put(i,DruidDataSourceFactory.createDataSource(properties));
        }
    }

    private void initShardingStrategy() throws Exception {
        if(dbCount > 0){
            List<Class<?>> shardingStrategies = ClassScanner.getSubTypesOf(IShardingStrategy.class);
            if(shardingStrategies.isEmpty()){
                throw new ClassNotFoundException("No sharding strategy class found!");
            }
            Class<?> shardingClass = shardingStrategies.get(0);
            this.shardingStrategy = (IShardingStrategy) shardingClass.getDeclaredConstructor().newInstance();
        }
    }

    private void createDaos() throws Exception {
        List<Class<?>> entityClasses = ClassScanner.getTypesAnnotatedWith(AnnTable.class);
        for(Class<?> cls:entityClasses){
            AnnTable annTable = cls.getAnnotation(AnnTable.class);
            if(annTable.dbType() != Constants.DBType.MYSQL){
                continue;
            }
            Class<? extends AbstractBaseDao> daoClass = annTable.daoClass();
            AbstractBaseDao dao = daoClass.getDeclaredConstructor().newInstance();
            daoMap.put(cls,dao);
            for(DataSource dataSource:sourceMap.values()){
                Connection connection = null;
                try{
                    connection = dataSource.getConnection();
                    dao.create(connection);
                } finally {
                    if(connection != null){
                        connection.close();
                    }
                }
            }
        }
    }
    @Override
    public void insert(AbstractBaseEntity entity) throws Exception{
        int dbID = shardingStrategy.getShardingByID(entity.getKeyID());
        DataSource dataSource = this.sourceMap.get(dbID);
        if(dataSource == null){
            logger.error("No data source for entity {}", JSON.toJSONString(entity));
            throw new RuntimeException("No data source for id: " + entity.getKeyID());
        }
        Connection connection = null;
        try{
            connection = dataSource.getConnection();
            AbstractBaseDao dao = daoMap.get(entity.getClass());
            if(!dao.insert(connection,entity)){
                logger.warn("Insert {}} failed! Data:{}",entity.getClass().getSimpleName(),JSON.toJSONString(entity));
            }
        } finally {
            if(connection != null){
                connection.close();
            }
        }
    }

    @Override
    public <T extends AbstractBaseEntity> List<T> selectByKey(long keyID, Class<T> cls) throws Exception {
        int dbID = shardingStrategy.getShardingByID(keyID);
        DataSource dataSource = this.sourceMap.get(dbID);
        if(dataSource == null){
            throw new RuntimeException("No data source for id: " + keyID);
        }
        Connection connection = null;
        try{
            connection = dataSource.getConnection();
            AbstractBaseDao dao = daoMap.get(cls);
            return dao.selectByKey(connection,keyID);
        } finally {
            if(connection != null){
                connection.close();
            }
        }
    }

    @Override
    public Set<Integer> getAllDbId() {
        return sourceMap.keySet();
    }

    @Override
    public <T extends AbstractBaseEntity> List<T> selectCustom(int dbID, Object condition, Class<T> cls) throws Exception {
        DataSource dataSource = this.sourceMap.get(dbID);
        if(dataSource == null){
            throw new RuntimeException("No data source for id: " + dbID);
        }
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        List<T> entities = new ArrayList<>();
        try{
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            resultSet = statement.executeQuery((String) condition);
            while(resultSet.next()){
                T entity = cls.getDeclaredConstructor().newInstance();
                entity.fromResultSet(resultSet);
                entities.add(entity);
            }
        } finally {
            if(resultSet != null){
                resultSet.close();
            }
            if(statement != null){
                statement.close();
            }
            if(connection != null){
                connection.close();
            }
        }
        return entities;
    }

    @Override
    public void delete(AbstractBaseEntity entity) throws Exception {
        int dbID = shardingStrategy.getShardingByID(entity.getKeyID());
        DataSource dataSource = this.sourceMap.get(dbID);
        if(dataSource == null){
            logger.error("No data source for entity {}", JSON.toJSONString(entity));
            throw new RuntimeException("No data source for id: " + entity.getKeyID());
        }
        Connection connection = null;
        try{
            connection = dataSource.getConnection();
            AbstractBaseDao dao = daoMap.get(entity.getClass());
            if(!dao.delete(connection,entity)){
                logger.warn("Delete {} failed! Data:{}",entity.getClass().getSimpleName(),JSON.toJSONString(entity));
            }
        } finally {
            if(connection != null){
                connection.close();
            }
        }
    }

    @Override
    public void update(AbstractBaseEntity entity) throws Exception {
        int dbID = shardingStrategy.getShardingByID(entity.getKeyID());
        DataSource dataSource = this.sourceMap.get(dbID);
        if(dataSource == null){
            logger.error("No data source for entity {}", JSON.toJSONString(entity));
            throw new RuntimeException("No data source for id: " + entity.getKeyID());
        }
        Connection connection = null;
        try{
            connection = dataSource.getConnection();
            AbstractBaseDao dao = daoMap.get(entity.getClass());
            if(!dao.update(connection,entity)){
                logger.info("Update {} failed! id:{},version:{}",entity.getClass().getSimpleName(),entity.getId(),entity.getEntityVersion());
            }
        } finally {
            if(connection != null){
                connection.close();
            }
        }
    }
}
