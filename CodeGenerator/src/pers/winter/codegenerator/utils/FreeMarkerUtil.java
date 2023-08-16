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
package pers.winter.codegenerator.utils;

import freemarker.template.Configuration;
import freemarker.template.Template;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class FreeMarkerUtil {
    private static final String TEMPLATE_DIR = "templates";
    public static void generate(String templatePath, String exportPath, String exportFileName, Map<String,Object> params) throws Exception {
        File exportDir = new File(exportPath);
        if(!exportDir.exists()){
            exportDir.mkdirs();
        }
        try(Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(exportPath+File.separator+exportFileName), StandardCharsets.UTF_8))){
            Configuration config = new Configuration(Configuration.VERSION_2_3_32);
            config.setDefaultEncoding(String.valueOf(StandardCharsets.UTF_8));
            config.setDirectoryForTemplateLoading(new File(FileUtil.getAbsolutePath(TEMPLATE_DIR)));
            Template template = config.getTemplate(templatePath);
            template.process(params,writer);
            writer.flush();
        }
    }
}
