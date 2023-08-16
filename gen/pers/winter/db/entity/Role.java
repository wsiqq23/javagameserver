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
import pers.winter.db.dao.RoleDao;

@AnnTable(key = "id", dbType = Constants.DBType.MYSQL, daoClass = RoleDao.class, cacheType = Constants.CacheType.MEMORY,userCache = true)
public class Role extends AbstractBaseEntity {
    private long accountId;
    private short lv;
    private String name;
    private byte job;
    private short sex;
    private byte race;
    private long createTime;
    private long recentLogin;

    public long getAccountId(){
        return accountId;
    }
    public void setAccountId(long accountId){
        this.accountId = accountId;
    }
    public short getLv(){
        return lv;
    }
    public void setLv(short lv){
        this.lv = lv;
    }
    public String getName(){
        return name;
    }
    public void setName(String name){
        this.name = name;
    }
    public byte getJob(){
        return job;
    }
    public void setJob(byte job){
        this.job = job;
    }
    public short getSex(){
        return sex;
    }
    public void setSex(short sex){
        this.sex = sex;
    }
    public byte getRace(){
        return race;
    }
    public void setRace(byte race){
        this.race = race;
    }
    public long getCreateTime(){
        return createTime;
    }
    public void setCreateTime(long createTime){
        this.createTime = createTime;
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
    }

    @Override
    public void fromResultSet(ResultSet rest) throws SQLException {
        this.setId(rest.getLong(1));
        this.setEntityVersion(rest.getInt(2));
        this.setAccountId(rest.getLong(3));
        this.setLv(rest.getShort(4));
        this.setName(rest.getString(5));
        this.setJob(rest.getByte(6));
        this.setSex(rest.getShort(7));
        this.setRace(rest.getByte(8));
        this.setCreateTime(rest.getLong(9));
        this.setRecentLogin(rest.getLong(10));
    }

    @Override
    public Document toDocument() {
        return null;
    }

    @Override
    public void toPreparedStatement(PreparedStatement stat) throws SQLException {
        stat.setLong(1, this.getId());
        stat.setInt(2, this.getEntityVersion());
        stat.setLong(3, this.getAccountId());
        stat.setShort(4, this.getLv());
        stat.setString(5, this.getName());
        stat.setByte(6, this.getJob());
        stat.setShort(7, this.getSex());
        stat.setByte(8, this.getRace());
        stat.setLong(9, this.getCreateTime());
        stat.setLong(10, this.getRecentLogin());
    }

    @Override
    public Role deepClone(){
        Role role = new Role();
        role.setId(getId());
        role.setEntityVersion(getEntityVersion());
        role.setAccountId(this.getAccountId());
        role.setLv(this.getLv());
        role.setName(this.getName());
        role.setJob(this.getJob());
        role.setSex(this.getSex());
        role.setRace(this.getRace());
        role.setCreateTime(this.getCreateTime());
        role.setRecentLogin(this.getRecentLogin());
        return role;
    }
}