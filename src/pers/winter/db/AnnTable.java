package pers.winter.db;

import pers.winter.db.mysql.AbstractBaseDao;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used for Entity classes that will be inserted into a database.
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AnnTable {
    /** Specifies the key associated with the table in the database. */
    String key();
    /** Specifies the type of database: MySQL or MongoDB */
    Constants.DBType dbType();
    /** Specifies the type of caching mechanism: memory or redis. */
    Constants.CacheType cacheType();
    /** Specifies the DAO (Data Access Object) class associated with the Entity. */
    Class<? extends AbstractBaseDao> daoClass();
    /**  Specifies the table is cached in the user cache. */
    boolean userCache();
}
