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
package pers.winter.codegenerator.entity;

import pers.winter.codegenerator.field.Field;

public class MySqlField extends Field {
    private String dbType;
    public MySqlField(String type, String name, String comment) {
        super(type, name, comment);
    }
    public String getDbType(){
        return this.dbType;
    }
    public void setDbType(String dbType){
        this.dbType = dbType;
    }
}
