package pers.winter.db.dao;

import pers.winter.db.AbstractBaseEntity;
import pers.winter.db.entity.Student;
import pers.winter.db.mysql.AbstractBaseDao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unchecked")
public class StudentDao extends AbstractBaseDao {
    private static final String TABLE_NAME = "student";
    private static final String KEY_NAME = "dormitory";
    @Override
    public boolean insert(Connection connection, AbstractBaseEntity entity) throws SQLException {
        String sql = String.format(SQL_INSERT,TABLE_NAME,"(?,?,?,?,?,?,?)");
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
        String sql = String.format(SQL_UPDATE,TABLE_NAME,"id=?,entityVersion=?,sex=?,name=?,birthday=?,dormitory=?,transcript=? where id=?");
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = connection.prepareStatement(sql);
            entity.toPreparedStatement(preparedStatement);
            preparedStatement.setLong(8,entity.getId());
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
    public List<Student> selectByKey(Connection connection, long keyID) throws SQLException {
        String sql = String.format(SQL_SELECT_BY_KEY,TABLE_NAME,KEY_NAME);
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        List<Student> entities = new ArrayList<>();
        try{
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setLong(1,keyID);
            resultSet = preparedStatement.executeQuery();
            while(resultSet.next()){
                Student entity = new Student();
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
        sb.append(",sex smallint comment 'sex of the student'");
        sb.append(",name varchar(200) comment 'name of the student'");
        sb.append(",birthday bigint comment 'birth timestamp of the student'");
        sb.append(",dormitory bigint comment 'dormitory id of the student'");
        sb.append(",transcript json comment 'transcript of the student'");
        sb.append(",primary key(id)");
        sb.append(",key dormitoryindex0(dormitory)");
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
