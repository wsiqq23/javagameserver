package pers.winter.db.mysql;

/**
 * The ShardingStrategy interface should be implemented by the user to define their own strategy for sharding data to different databases.
 * This interface provides a single method, shardKey, which takes a shardKey value and returns the id of the database where the data should be stored.
 * The user can implement this method as per their own logic for sharding the data.
 * @author Winter
 */
public interface IShardingStrategy {
    /**
     * Input the key id of the data and get which db to store.
     * @param id the key id
     * @return the db id
     */
    int getShardingByID(long id);
}
