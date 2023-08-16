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
package pers.winter.bean;

import pers.winter.framework.entity.ICloneable;

/**
 * Information for role-creation from client
 * @author CodeGenerator
 */
public class CreateRoleBean implements ICloneable {
    private String name;
    private byte job;
    private short sex;
    private byte race;

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
    @Override
    public CreateRoleBean deepClone(){
        CreateRoleBean createRoleBean = new CreateRoleBean();
        createRoleBean.setName(this.getName());
        createRoleBean.setJob(this.getJob());
        createRoleBean.setSex(this.getSex());
        createRoleBean.setRace(this.getRace());
        return createRoleBean;
    }
}