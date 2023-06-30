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
package pers.winter.framework.db.mysql;

import pers.winter.framework.db.AbstractBaseEntity;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * The super class for all mysql dao.
 * @author Winter
 */
public abstract class AbstractBaseDao {
    protected static final String SQL_INSERT = "insert into %s values%s";
    protected static final String SQL_DELETE = "delete from %s where id=?";
    protected static final String SQL_UPDATE = "update %s set %s";
    protected static final String SQL_SELECT_BY_KEY = "select * from %s where %s=?";

    public abstract boolean insert(Connection connection, AbstractBaseEntity entity) throws SQLException;
    public abstract boolean delete(Connection connection, AbstractBaseEntity entity) throws SQLException;
    public abstract boolean update(Connection connection, AbstractBaseEntity entity) throws SQLException;
    public abstract <T extends AbstractBaseEntity> List<T> selectByKey(Connection connection, long keyID) throws SQLException;
    public abstract void create(Connection connection) throws SQLException;
}
