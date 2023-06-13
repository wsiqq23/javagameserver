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

@AnnConfig(filePath="config/application.properties")
public class ApplicationConfig {
    private int socketPort;
    private int webSocketPort;
    private short idleStateTime;
    private short messageThreadPoolCount;
    private short databaseThreadPoolCount;
    private long nodeID;
    private int cacheExpire;
    public int getSocketPort() {
        return socketPort;
    }
    public int getWebSocketPort() {
        return webSocketPort;
    }
    public short getIdleStateTime(){
        return idleStateTime;
    }
    public short getMessageThreadPoolCount() {
        return messageThreadPoolCount;
    }
    public short getDatabaseThreadPoolCount(){return databaseThreadPoolCount;}
    public long getNodeID(){return nodeID;}
    public int getCacheExpire(){return cacheExpire;}
}
