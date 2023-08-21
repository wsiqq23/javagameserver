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
package pers.winter.framework.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileUtil {
    public static List<File> findAllFilesRecursively(String directoryPath, String ext){
        List<File> result = new ArrayList<>();
        File directory;
        if(directoryPath.startsWith("/")){
            directory = new File(directoryPath);
        } else {
            directory = new File(getAbsolutePath(directoryPath));
        }
        if(!directory.exists()){
            System.out.println("Directory doesn't exist: " + directoryPath);
            return result;
        }
        for(File child:directory.listFiles()){
            if(child.isDirectory()){
                result.addAll(findAllFilesRecursively(child.getPath(),ext));
            } else if(child.getName().endsWith("."+ext)){
                result.add(child);
            }
        }
        return result;
    }

    public static String getAbsolutePath(String relativePath){
        String rootPath = FileUtil.class.getResource("").getPath();
        if(rootPath.contains("file:")){
            rootPath = rootPath.substring("file:".length());
        }
        int jarIndex = rootPath.indexOf("jar!");
        if(jarIndex>0){
            rootPath = rootPath.substring(0,jarIndex);
            rootPath = rootPath.substring(0,rootPath.lastIndexOf(File.separator) + 1);
        } else {
            String tmpDirPath = FileUtil.class.getPackageName().replace(".",File.separator);
            rootPath = rootPath.replace(tmpDirPath,"");
        }
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
