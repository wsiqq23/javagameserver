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
package pers.winter.framework.db;

import pers.winter.framework.db.mysql.AbstractBaseDao;

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
