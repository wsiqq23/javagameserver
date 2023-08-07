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
import pers.winter.db.dao.RoleDao;
import pers.winter.framework.db.AbstractBaseEntity;
import pers.winter.framework.db.AnnTable;
import pers.winter.framework.db.Constants;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@AnnTable(key="id",dbType = Constants.DBType.MYSQL,cacheType = Constants.CacheType.MEMORY,daoClass = RoleDao.class,userCache = true)
public class Role extends AbstractBaseEntity {
    private long accountId;
    private short lv;
    private String name;
    private byte job;
    private short sex;
    private byte race;
    private long createTime;
    private long recentLogin;

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public short getLv() {
        return lv;
    }

    public void setLv(short lv) {
        this.lv = lv;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte getJob() {
        return job;
    }

    public void setJob(byte job) {
        this.job = job;
    }

    public short getSex() {
        return sex;
    }

    public void setSex(short sex) {
        this.sex = sex;
    }

    public byte getRace() {
        return race;
    }

    public void setRace(byte race) {
        this.race = race;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getRecentLogin() {
        return recentLogin;
    }

    public void setRecentLogin(long recentLogin) {
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
        this.setAccountId((long) document.get("accountId"));
        this.setLv((short) (int) document.get("lv"));
        this.setName((String) document.get("name"));
        this.setJob((byte) (int) document.get("job"));
        this.setSex((short) (int) document.get("sex"));
        this.setRace((byte) (int) document.get("race"));
        this.setCreateTime((long) document.get("createTime"));
        this.setRecentLogin((long) document.get("recentLogin"));
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
        Document document = new Document();
        document.put("id",this.getId());
        document.put("entityVersion",this.getEntityVersion());
        document.put("accountId",this.getAccountId());
        document.put("lv",this.getLv());
        document.put("name",this.getName());
        document.put("job",this.getJob());
        document.put("sex",this.getSex());
        document.put("race",this.getRace());
        document.put("createTime",this.getCreateTime());
        document.put("recentLogin",this.getRecentLogin());
        return document;
    }

    @Override
    public void toPreparedStatement(PreparedStatement stat) throws SQLException {
        stat.setLong(1,this.getId());
        stat.setLong(2,this.getEntityVersion());
        stat.setLong(3,this.getAccountId());
        stat.setShort(4,this.getLv());
        stat.setString(5,this.getName());
        stat.setByte(6,this.getJob());
        stat.setShort(7,this.getSex());
        stat.setByte(8,this.getRace());
        stat.setLong(9,this.getCreateTime());
        stat.setLong(10,this.getRecentLogin());
    }

    @Override
    public Object deepClone() {
        Role role = new Role();
        role.setId(getId());
        role.setEntityVersion(getEntityVersion());
        role.setAccountId(getAccountId());
        role.setLv(getLv());
        role.setName(getName());
        role.setJob(getJob());
        role.setSex(getSex());
        role.setRace(getRace());
        role.setCreateTime(getCreateTime());
        role.setRecentLogin(getRecentLogin());
        return role;
    }
}
