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
package pers.winter.codegenerator.field;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Field {
    private final String type;
    private final String name;
    private final String comment;
    public Field(String type,String name,String comment){
        this.type = type;
        this.name = name;
        this.comment = comment;
    }
    public String getName(){
        return this.name;
    }
    public String getType(){
        return this.type;
    }
    public String getComment(){
        return this.comment;
    }
    public String getBriefType(){
        String wholeClass = getImport();
        if(wholeClass == null){
            return getType();
        }
        return getType().replace(wholeClass,wholeClass.substring(wholeClass.lastIndexOf(".") + 1));
    }
    public boolean isList(){
        return this.type.startsWith("List");
    }
    public boolean isMap(){
        return this.type.startsWith("Map");
    }
    public boolean isBean(){
        return !isList() && !isMap() && type.indexOf(".")>0;
    }
    public String getValueClass(){
        String innerStr=getType().replace("List<","").replace("Map<","").replace(">","");
        innerStr = innerStr.split(",")[innerStr.split(",").length - 1];
        if(!innerStr.contains(".")){
            return innerStr;
        } else {
            return innerStr.substring(innerStr.lastIndexOf(".") + 1);
        }
    }
    public String getImport(){
        if(type.indexOf(".")>0){
            Pattern pattern = Pattern.compile("((\\w+\\.)+\\w+)");
            Matcher matcher = pattern.matcher(type);
            if(matcher.find()){
                return matcher.group();
            }
        }
        return null;
    }
}
