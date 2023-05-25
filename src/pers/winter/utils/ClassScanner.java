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
package pers.winter.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * A class scanner, scann classes in the project.
 * @author Winter
 */
public class ClassScanner {
    private static final Logger logger = LogManager.getLogger(ClassScanner.class);
    private static List<Class<?>> classesCache = null;

    /**
     * Get all classes extends or implements the type
     * @param type the parent or interface class
     * @return
     */
    public static List<Class<?>> getSubTypesOf(Class<?> type){
        List<Class<?>> classes = new ArrayList<>();
        for(Class<?> cls:getClassesCache()){
            if(type.isAssignableFrom(cls) && type != cls){
                classes.add(cls);
            }
        }
        return classes;
    }

    /**
     * Get all classes with the annotation
     * @param type the annotation class
     * @return
     */
    public static List<Class<?>> getTypesAnnotatedWith(Class<? extends Annotation> type){
        List<Class<?>> classes = new ArrayList<>();
        for(Class<?> cls:getClassesCache()){
            if(cls.isAnnotationPresent(type)){
                classes.add(cls);
            }
        }
        return classes;
    }

    private static List<Class<?>> getClassesCache() {
        if(classesCache == null) {
            try {
                initClasses();
            }catch (IOException e) {
                logger.error("Init class scanner exception!",e);
                System.exit(-1);
            }
        }
        return classesCache;
    }
    private synchronized static void initClasses() throws IOException {
        if(classesCache != null) {
            return;
        }
        classesCache = new ArrayList<>();
        ClassLoader loader = ClassScanner.class.getClassLoader();
        String resourceName = resourceName(ClassScanner.class.getPackageName());
        Enumeration<URL> urls = loader.getResources(resourceName);
        List<URL> pathList = new ArrayList<>();
        while(urls.hasMoreElements()) {
            URL url = urls.nextElement();
            int index = url.toExternalForm().lastIndexOf(resourceName);
            if (index != -1) {
                pathList.add(new URL(url, url.toExternalForm().substring(0, index)));
            } else {
                pathList.add(url);
            }
        }
        for(URL path:pathList){
            if(path.toExternalForm().startsWith("jar:")) {
                classesCache.addAll(getClassesFromJar(path));
            } else {
                String pathStr = path.getPath();
                if(pathStr.startsWith("file:")){
                    pathStr = pathStr.substring("file:".length());
                }
                File rootFile = new File(pathStr);
                classesCache.addAll(getClassesFromFileRecursively(rootFile,rootFile.getPath()+File.separator));
            }
        }
    }

    private static List<Class<?>> getClassesFromJar(URL path) throws IOException {
        List<Class<?>> result = new ArrayList<>();
        String fullPath = path.getPath();
        if(fullPath.startsWith("jar:")){
            fullPath = fullPath.substring("jar:".length());
        }
        if(fullPath.startsWith("file:")){
            fullPath = fullPath.substring("file:".length());
        }
        if (fullPath.endsWith("!/")) {
            fullPath = fullPath.substring(0, fullPath.lastIndexOf("!/")) + "/";
        }
        JarFile jarFile = new JarFile(fullPath);
        Enumeration<JarEntry> entries = jarFile.entries();
        while(entries.hasMoreElements()){
            JarEntry entry = entries.nextElement();
            if(entry.getName().endsWith(".class")){
                String className = entry.getName().substring(0, entry.getName().lastIndexOf(".class"));
                className = className.replace('/','.');
                Class<?> cls = null;
                try {
                    cls = Class.forName(className);
                } catch (ClassNotFoundException e) {
                }
                if(cls != null){
                    result.add(cls);
                }
            }
        }
        jarFile.close();
        return result;
    }
    private static List<Class<?>> getClassesFromFileRecursively(File file, String rootDirectory) throws IOException {
        List<Class<?>> result = new ArrayList<>();
        if(file.isDirectory()){
            File[] files = file.listFiles();
            for(File child:files){
                result.addAll(getClassesFromFileRecursively(child,rootDirectory));
            }
        } else {
            String path = file.getPath();
            if(path.endsWith(".class")) {
                String className = path.substring(rootDirectory.length());
                className = className.substring(0, className.lastIndexOf(".class"));
                className = className.replace(File.separator,".");
                Class<?> cls = null;
                try {
                    cls = Class.forName(className);
                } catch (ClassNotFoundException e) {
                }
                result.add(cls);
            }
        }
        return result;
    }
    private static String resourceName(String name) {
        if (name != null) {
            String resourceName = name.replace(".", "/");
            resourceName = resourceName.replace("\\", "/");
            if (resourceName.startsWith("/")) {
                resourceName = resourceName.substring(1);
            }
            return resourceName;
        } else {
            return null;
        }
    }
}
