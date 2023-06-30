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
package pers.winter.config;

import pers.winter.utils.ClassScanner;
import sun.misc.Unsafe;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class ConfigManager {
    public static final ConfigManager INSTANCE = new ConfigManager();
    private static Unsafe UNSAFE;
    private final Map<Class<?>, Object> data = new HashMap<>();

    private ConfigManager() {
    }

    /**
     * Get a config data by config class
     * @param cls the class of the config class
     * @throws IllegalArgumentException cannot load a config not annotated with the Annotation "AnnConfig"
     */
    @SuppressWarnings("unchecked")
    public <T> T getConfig(Class<T> cls) throws IllegalArgumentException {
        if (!cls.isAnnotationPresent(AnnConfig.class)) {
            throw new IllegalArgumentException("Class not annotated by AnnConfig");
        }
        return (T) data.get(cls);
    }

    public void init() throws Exception {
        Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
        theUnsafe.setAccessible(true);
        UNSAFE = (Unsafe) theUnsafe.get(null);
        List<Class<?>> classes = ClassScanner.getTypesAnnotatedWith(AnnConfig.class);
        for (Class<?> cls : classes) {
            Constructor<?> defaultConstructor = cls.getConstructor();
            Object config = defaultConstructor.newInstance();
            AnnConfig annConfig = cls.getAnnotation(AnnConfig.class);
            loadConfig(config, annConfig);
            data.put(cls, config);
        }
    }

    private void loadConfig(Object config, AnnConfig annConfig) throws Exception {
        InputStream is = ConfigManager.class.getClassLoader().getResourceAsStream(annConfig.filePath());
        Properties properties = new Properties();
        properties.load(is);
        is.close();
        Field[] fields = config.getClass().getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            long offset = UNSAFE.objectFieldOffset(field);
            String v = (String) properties.get(field.getName());
            if(v == null){
                continue;
            }
            Class<?> type = field.getType();
            if (byte.class == type) {
                UNSAFE.putByte(config, offset, Byte.parseByte(v));
            } else if (short.class == type) {
                UNSAFE.putShort(config, offset, Short.parseShort(v));
            } else if (int.class == type) {
                UNSAFE.putInt(config, offset, Integer.parseInt(v));
            } else if (long.class == type) {
                UNSAFE.putLong(config, offset, Long.parseLong(v));
            } else if (float.class == type) {
                UNSAFE.putFloat(config, offset, Float.parseFloat(v));
            } else if (double.class == type) {
                UNSAFE.putDouble(config, offset, Double.parseDouble(v));
            } else if (boolean.class == type) {
                UNSAFE.putBoolean(config, offset, Boolean.parseBoolean(v));
            } else if (String.class == type) {
                UNSAFE.putObject(config, offset, v);
            } else {
                throw new Exception(type.getSimpleName() + " not support!");
            }
        }
    }
}
