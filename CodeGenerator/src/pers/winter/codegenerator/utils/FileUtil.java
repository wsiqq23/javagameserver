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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileUtil {
    public static List<File> findAllXmlRecursively(String directoryPath){
        List<File> result = new ArrayList<>();
        File directory;
        if(directoryPath.startsWith("/")){
            directory = new File(directoryPath);
        } else {
            String rootPath = FileUtil.class.getResource("").getPath();
            String tmpDirPath = FileUtil.class.getPackageName().replace(".",File.separator);
            rootPath = rootPath.replace(tmpDirPath,"");
            directory = new File(rootPath + directoryPath);
        }
        if(!directory.exists()){
            System.out.println("Directory doesn't exist: " + directoryPath);
            return result;
        }
        for(File child:directory.listFiles()){
            if(child.isDirectory()){
                result.addAll(findAllXmlRecursively(child.getPath()));
            } else if(child.getName().endsWith(".xml")){
                result.add(child);
            }
        }
        return result;
    }

    public static String getAbsolutePath(String relativePath){
        String rootPath = FileUtil.class.getResource("").getPath();
        String tmpDirPath = FileUtil.class.getPackageName().replace(".",File.separator);
        rootPath = rootPath.replace(tmpDirPath,"");
        return rootPath + relativePath;
    }

    public static void deleteFileRecursively(File file){
        if(!file.exists()){
            return;
        }
        if(file.isDirectory()){
            for(File child:file.listFiles()){
                if(child.isDirectory()){
                    deleteFileRecursively(child);
                } else {
                    child.delete();
                }
            }
            file.delete();
        } else {
            file.delete();
        }
    }
}
