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
