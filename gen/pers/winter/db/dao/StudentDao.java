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
package pers.winter.db.dao;

import pers.winter.framework.db.AbstractBaseEntity;
import pers.winter.db.entity.Student;
import pers.winter.framework.db.mysql.AbstractBaseDao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StudentDao extends AbstractBaseDao {
    private static final String TABLE_NAME = "student";
    private static final String KEY_NAME = "dormitory";
    @Override
    public boolean insert(Connection connection, AbstractBaseEntity entity) throws SQLException {
        String sql = String.format(SQL_INSERT,TABLE_NAME,"(?,?,?,?,?,?,?,?,?,?)");
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
        String sql = String.format(SQL_UPDATE,TABLE_NAME,"id=?,entityVersion=?,sex=?,name=?,birthday=?,dormitory=?,transcript=?,transcriptMap=?,transcriptList=?,shortList=? where id=?");
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
        sb.append(",birthday bigint comment 'timestamp of the birthday of the student'");
        sb.append(",dormitory bigint comment ''");
        sb.append(",transcript json comment ''");
        sb.append(",transcriptMap json comment ''");
        sb.append(",transcriptList json comment ''");
        sb.append(",shortList json comment ''");
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