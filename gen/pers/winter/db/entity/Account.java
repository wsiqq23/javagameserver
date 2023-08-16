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
package pers.winter.db.entity;

import org.bson.Document;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import pers.winter.framework.db.AbstractBaseEntity;
import pers.winter.framework.db.AnnTable;
import pers.winter.framework.db.Constants;

@AnnTable(key = "id", dbType = Constants.DBType.MONGO,  cacheType = Constants.CacheType.MEMORY,userCache = false)
public class Account extends AbstractBaseEntity {
    private String channel;
    private String platformId;
    private long createTime;
    private long recentRole;
    private long recentLogin;

    public String getChannel(){
        return channel;
    }
    public void setChannel(String channel){
        this.channel = channel;
    }
    public String getPlatformId(){
        return platformId;
    }
    public void setPlatformId(String platformId){
        this.platformId = platformId;
    }
    public long getCreateTime(){
        return createTime;
    }
    public void setCreateTime(long createTime){
        this.createTime = createTime;
    }
    public long getRecentRole(){
        return recentRole;
    }
    public void setRecentRole(long recentRole){
        this.recentRole = recentRole;
    }
    public long getRecentLogin(){
        return recentLogin;
    }
    public void setRecentLogin(long recentLogin){
        this.recentLogin = recentLogin;
    }

    @Override
    public String getKey() {
        return "id";
    }

    @Override
    public long getKeyID() {
        return getId();
    }

    @Override
    public void fromDocument(Document document) {
        this.setId((long) document.get("id"));
        this.setEntityVersion((int) document.get("entityVersion"));
        this.setChannel((String) document.get("channel"));
        this.setPlatformId((String) document.get("platformId"));
        this.setCreateTime((long) document.get("createTime"));
        this.setRecentRole((long) document.get("recentRole"));
        this.setRecentLogin((long) document.get("recentLogin"));
    }

    @Override
    public void fromResultSet(ResultSet rest) throws SQLException {
    }

    @Override
    public Document toDocument() {
        Document document = new Document();
        document.put("id", this.getId());
        document.put("entityVersion", this.getEntityVersion());
        document.put("channel",this.getChannel());
        document.put("platformId",this.getPlatformId());
        document.put("createTime",this.getCreateTime());
        document.put("recentRole",this.getRecentRole());
        document.put("recentLogin",this.getRecentLogin());
        return document;
    }

    @Override
    public void toPreparedStatement(PreparedStatement stat) throws SQLException {
    }

    @Override
    public Account deepClone(){
        Account account = new Account();
        account.setId(getId());
        account.setEntityVersion(getEntityVersion());
        account.setChannel(this.getChannel());
        account.setPlatformId(this.getPlatformId());
        account.setCreateTime(this.getCreateTime());
        account.setRecentRole(this.getRecentRole());
        account.setRecentLogin(this.getRecentLogin());
        return account;
    }
}