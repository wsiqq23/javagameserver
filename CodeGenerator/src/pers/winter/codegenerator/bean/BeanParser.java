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
package pers.winter.codegenerator.bean;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import pers.winter.codegenerator.Generator;
import pers.winter.codegenerator.field.Field;
import pers.winter.codegenerator.utils.FreeMarkerUtil;

import java.io.File;
import java.util.*;

public class BeanParser {
    public static BeanParser INSTANCE = new BeanParser();
    private BeanParser(){}
    public void parse(Element entityElement) throws Exception {
        List<Field> fields = generateFields(entityElement);
        generateBean(entityElement, fields);
    }
    private List<Field> generateFields(Element entityElement){
        NodeList fieldElements = entityElement.getElementsByTagName("field");
        List<Field> fields = new ArrayList<>(fieldElements.getLength());
        for(int i = 0;i< fieldElements.getLength();i++){
            Element fieldElement = (Element) fieldElements.item(i);
            Field field = new Field(fieldElement.getAttribute("type"),fieldElement.getAttribute("name"),fieldElement.getAttribute("comment"));
            fields.add(field);
        }
        return fields;
    }
    private void generateBean(Element messageElement, List<Field> fields) throws Exception {
        Map<String,Object> params = new HashMap<>();
        for(int i = 0;i<messageElement.getAttributes().getLength();i++){
            params.put(messageElement.getAttributes().item(i).getNodeName(),messageElement.getAttributes().item(i).getNodeValue());
        }
        Set<String> imports = new HashSet<>();
        for(Field field:fields){
            if(field.getImport() != null){
                imports.add(field.getImport());
            }
            if(field.isList() || field.isMap()){
                imports.add("java.util.*");
            }
        }
        params.put("imports",imports);
        params.put("fields",fields);

        String[] tmp = messageElement.getAttribute("class").split("\\.");
        String fileName = tmp[tmp.length - 1] + ".java";
        tmp[tmp.length - 1] = "";
        String exportDir = Generator.EXPORT_DIR + File.separator + String.join(File.separator,tmp);
        FreeMarkerUtil.generate(Generator.TEMPLATE_PATH_BEAN,exportDir,fileName,params);
    }
}
