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

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import pers.winter.codegenerator.Constants;
import pers.winter.codegenerator.Generator;
import pers.winter.codegenerator.field.Field;
import pers.winter.codegenerator.utils.FreeMarkerUtil;

import java.io.File;
import java.util.*;

public class EntityParser {
    public static EntityParser INSTANCE = new EntityParser();

    private EntityParser(){}

    public void parse(Element entityElement) throws Exception {
        List<Field> fields = generateFields(entityElement);
        generateEntity(entityElement, fields);
        if(entityElement.getAttribute("dbType").equalsIgnoreCase("MYSQL")){
            generateDao(entityElement,fields);
        }
    }

    private void generateEntity(Element entityElement,List<Field> fields) throws Exception {
        Map<String,Object> params = new HashMap<>();
        for(int i = 0;i<entityElement.getAttributes().getLength();i++){
            params.put(entityElement.getAttributes().item(i).getNodeName(),entityElement.getAttributes().item(i).getNodeValue());
        }
        Set<String> imports = new HashSet<>();
        boolean isMySQL = entityElement.getAttribute("dbType").equalsIgnoreCase("MYSQL");
        for(Field field:fields){
            if(field.getImport() != null){
                imports.add(field.getImport());
                imports.add("pers.winter.framework.utils.CloneUtil");
                if(isMySQL){
                    imports.add("com.alibaba.fastjson.JSON");
                    if(field.isMap()){
                        imports.add("com.alibaba.fastjson.TypeReference");
                    }
                } else {
                    imports.add("pers.winter.framework.utils.MongoCodecUtil");
                }
            }
            if(field.isList() || field.isMap()){
                imports.add("java.util.*");
            }
        }
        params.put("imports",imports);
        params.put("fields",fields);

        String[] tmp = entityElement.getAttribute("class").split("\\.");
        String fileName = tmp[tmp.length - 1] + ".java";
        tmp[tmp.length - 1] = "";
        String exportDir = Constants.EXPORT_DIR + File.separator + String.join(File.separator,tmp);
        FreeMarkerUtil.generate(Constants.TEMPLATE_PATH_ENTITY,exportDir,fileName,params);
    }

    private List<Field> generateFields(Element entityElement){
        boolean isMySQL = entityElement.getAttribute("dbType").equalsIgnoreCase("MYSQL");
        NodeList fieldElements = entityElement.getElementsByTagName("field");
        List<Field> fields = new ArrayList<>(fieldElements.getLength());
        for(int i = 0;i< fieldElements.getLength();i++){
            Element fieldElement = (Element) fieldElements.item(i);
            Field field;
            if(isMySQL){
                field = new MySqlField(fieldElement.getAttribute("type"),fieldElement.getAttribute("name"),fieldElement.getAttribute("comment"));
                ((MySqlField)field).setDbType(fieldElement.getAttribute("dbType"));
            } else {
                field = new Field(fieldElement.getAttribute("type"),fieldElement.getAttribute("name"),fieldElement.getAttribute("comment"));
            }
            fields.add(field);
        }
        return fields;
    }

    private void generateDao(Element entityElement,List<Field> fields) throws Exception {
        Map<String,Object> params = new HashMap<>();
        for(int i = 0;i<entityElement.getAttributes().getLength();i++){
            params.put(entityElement.getAttributes().item(i).getNodeName(),entityElement.getAttributes().item(i).getNodeValue());
        }
        params.put("fields",fields);

        String[] tmp = entityElement.getAttribute("daoClass").split("\\.");
        String fileName = tmp[tmp.length - 1] + ".java";
        tmp[tmp.length - 1] = "";
        String exportDir = Constants.EXPORT_DIR + File.separator + String.join(File.separator,tmp);
        FreeMarkerUtil.generate(Constants.TEMPLATE_PATH_DAO,exportDir,fileName,params);
    }
}
