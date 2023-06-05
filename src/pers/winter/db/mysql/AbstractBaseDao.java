package pers.winter.db.mysql;

import pers.winter.db.AbstractBaseEntity;

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
    protected static final String SQL_SELECT = "select * from %s where id=?";
    protected static final String SQL_SELECT_BY_KEY = "select * from %s where %s=?";

    public abstract boolean insert(Connection connection, AbstractBaseEntity entity) throws SQLException;
    public abstract boolean delete(Connection connection, AbstractBaseEntity entity) throws SQLException;
    public abstract boolean update(Connection connection, AbstractBaseEntity entity) throws SQLException;
    public abstract <T extends AbstractBaseEntity> T select(Connection connection, long id) throws SQLException;
    public abstract <T extends AbstractBaseEntity> List<T> selectByKey(Connection connection, long keyID) throws SQLException;
    public abstract void create(Connection connection) throws SQLException;
}
