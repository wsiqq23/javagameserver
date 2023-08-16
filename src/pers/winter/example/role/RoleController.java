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
package pers.winter.example.role;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pers.winter.bean.CreateRoleBean;
import pers.winter.db.entity.Role;
import pers.winter.framework.entity.EntityManager;
import pers.winter.framework.utils.SnowFlakeIdGenerator;

public class RoleController {
    private static final Logger logger = LogManager.getLogger(RoleController.class);
    public static final RoleController INSTANCE = new RoleController();
    private RoleController(){}

    public Role createRole(long accountId, CreateRoleBean createRoleBean) throws Exception {
        if(createRoleBean == null){
            return null;
        }
        long now = System.currentTimeMillis();
        Role role = new Role();
        role.setCreateTime(now);
        role.setId(SnowFlakeIdGenerator.generateId());
        role.setAccountId(accountId);
        role.setRace(createRoleBean.getRace());
        role.setSex(createRoleBean.getSex());
        role.setJob(createRoleBean.getJob());
        role.setName(createRoleBean.getName());
        role.insert();
        EntityManager.INSTANCE.buildCache(role.getKeyID(),Role.class);;
        return role;
    }
}
