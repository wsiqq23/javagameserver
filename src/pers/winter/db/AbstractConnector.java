package pers.winter.db;

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
     * Selects an entity from the database based on the specified ID and class.
     * @param id the unique ID of the entity to be selected
     * @param cls the class of the entity
     * @param <T> the type of the entity
     * @return the selected entity
     * @throws Exception if an error occurs during selection
     */
    public abstract <T extends AbstractBaseEntity> T select(long id,Class<T> cls) throws Exception;
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
