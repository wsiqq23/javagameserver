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
package pers.winter.config;

@AnnConfig(filePath = "config/mongodb.properties")
public class MongoDBConfig {
    private String url;
    private String databaseName;
    private int connectTimeout;
    private int maxPoolSize;
    private int minPoolSize;
    private int maxWaitTime;
    private int maxConnectionIdleTime;

    public String getUrl() {
        return url;
    }
    public String getDatabaseName(){
        return databaseName;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public int getMinPoolSize() {
        return minPoolSize;
    }

    public int getMaxWaitTime() {
        return maxWaitTime;
    }

    public int getMaxConnectionIdleTime() {
        return maxConnectionIdleTime;
    }
}
