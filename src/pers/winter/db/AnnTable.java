package pers.winter.db;

import pers.winter.db.mysql.AbstractBaseDao;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AnnTable {
    String key();
    Constants.DBType dbType();
    Class<? extends AbstractBaseDao> daoClass();
}
