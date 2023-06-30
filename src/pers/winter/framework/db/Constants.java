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

public class Constants {
    public enum DBType{
        MONGO,MYSQL
    }
    public enum CacheType{
        REDIS,MEMORY
    }
    /**
     * Enumeration of the entity's actions.
     * delete, insert and update
     */
    public enum Action{
        DELETE(0),
        INSERT(1),
        UPDATE(2);
        private final int number;
        Action(int number){
            this.number = number;
        }
        public int toNumber(){
            return number;
        }
    }
}
