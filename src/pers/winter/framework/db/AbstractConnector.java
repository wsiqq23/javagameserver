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

import java.util.List;
import java.util.Set;

/**
 * This abstract class represents a connector for interacting with a database.
 * It provides various methods for inserting, selecting, deleting, and updating entities.
 * @author Winter
 */
public abstract class AbstractConnector {
    /**
     * Inserts the given entity into the database.
     * @param entity the entity to be inserted
     * @throws Exception if an error occurs during insertion
     */
    public abstract void insert(AbstractBaseEntity entity) throws Exception;
    /**
     * Selects all entities from the database matching the specified key ID and class.
     * @param keyID the key ID for selection
     * @param cls the class of the entities
     * @param <T> the type of the entities
     * @return the list of selected entities
     * @throws Exception if an error occurs during selection
     */
    public abstract <T extends AbstractBaseEntity> List<T> selectByKey(long keyID, Class<T> cls) throws Exception;
    /**
     * Retrieves a set of all database IDs, only useful for mysql
     * @return the set of all database IDs
     */
    public abstract Set<Integer> getAllDbId();
    /**
     * Selects all entities from the database matching the custom sql or mongo command.
     * @param dbID the database ID for selection, only useful for mysql
     * @param condition the custom condition for selection, sql or bson
     * @param cls the class of the entities
     * @param <T> the type of the entities
     * @return the list of selected entities
     * @throws Exception if an error occurs during selection
     */
    public abstract <T extends AbstractBaseEntity> List<T> selectCustom(int dbID, Object condition, Class<T> cls) throws Exception;
    /**
     * Deletes the given entity from the database.
     * @param entity the entity to be deleted
     * @throws Exception if an error occurs during deletion
     */
    public abstract void delete(AbstractBaseEntity entity) throws Exception;
    /**
     * Updates the given entity in the database.
     * @param entity the entity to be updated
     * @throws Exception if an error occurs during updating
     */
    public abstract void update(AbstractBaseEntity entity) throws Exception;
}
