package ${daoClass[0..(daoClass?last_index_of(".") - 1)]};

import pers.winter.framework.db.AbstractBaseEntity;
import ${class};
import pers.winter.framework.db.mysql.AbstractBaseDao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ${daoClass?split(".")?last} extends AbstractBaseDao {
    private static final String TABLE_NAME = "${class?split(".")?last?lower_case}";
    private static final String KEY_NAME = "${key}";
    @Override
    public boolean insert(Connection connection, AbstractBaseEntity entity) throws SQLException {
        String sql = String.format(SQL_INSERT,TABLE_NAME,"(?,?<#list fields as field>,?</#list>)");
        PreparedStatement preparedStatement = null;
        try{
            preparedStatement = connection.prepareStatement(sql);
            entity.toPreparedStatement(preparedStatement);
            if(preparedStatement.executeUpdate() == 1){
                return true;
            }
        } finally {
            if (preparedStatement != null) {
                preparedStatement.close();
            }
        }
        return false;
    }

    @Override
    public boolean delete(Connection connection, AbstractBaseEntity entity) throws SQLException {
        String sql = String.format(SQL_DELETE,TABLE_NAME);
        PreparedStatement preparedStatement = null;
        try{
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setLong(1,entity.getId());
            if(preparedStatement.executeUpdate() == 1){
                return true;
            }
        } finally {
            if(preparedStatement != null){
                preparedStatement.close();
            }
        }
        return false;
    }

    @Override
    public boolean update(Connection connection, AbstractBaseEntity entity) throws SQLException {
        String sql = String.format(SQL_UPDATE,TABLE_NAME,"id=?,entityVersion=?<#list fields as field>,${field.name}=?</#list> where id=?");
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = connection.prepareStatement(sql);
            entity.toPreparedStatement(preparedStatement);
            preparedStatement.setLong(${fields?size + 3},entity.getId());
            if (preparedStatement.executeUpdate() == 1) {
                return true;
            }
        } finally {
            if(preparedStatement != null){
                preparedStatement.close();
            }
        }
        return false;
    }

    @Override
    public List<${class?split(".")?last}> selectByKey(Connection connection, long keyID) throws SQLException {
        String sql = String.format(SQL_SELECT_BY_KEY,TABLE_NAME,KEY_NAME);
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        List<${class?split(".")?last}> entities = new ArrayList<>();
        try{
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setLong(1,keyID);
            resultSet = preparedStatement.executeQuery();
            while(resultSet.next()){
                ${class?split(".")?last} entity = new ${class?split(".")?last}();
                entity.fromResultSet(resultSet);
                entities.add(entity);
            }
        } finally {
            if(resultSet != null){
                resultSet.close();
            }
            if(preparedStatement != null){
                preparedStatement.close();
            }
        }
        return entities;
    }

    @Override
    public void create(Connection connection) throws SQLException {
        StringBuilder sb = new StringBuilder();
        sb.append("create table if not exists student(");
        sb.append("id bigint not null");
        sb.append(",entityVersion int comment 'Version of data'");
<#list fields as field>
        sb.append(",${field.name} ${field.dbType} comment '${field.comment}'");
</#list>
        sb.append(",primary key(id)");
        sb.append(",key ${key}index0(${key})");
        sb.append(")");
        Statement statement = null;
        try{
            statement = connection.createStatement();
            statement.executeUpdate(sb.toString());
        } finally {
            if(statement != null){
                statement.close();
            }
        }
    }
}