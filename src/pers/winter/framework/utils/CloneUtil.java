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

import com.alibaba.fastjson.JSON;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pers.winter.framework.entity.ICloneable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CloneUtil {
    private static final Logger logger = LogManager.getLogger(CloneUtil.class);
    @SuppressWarnings("unchecked")
    public static <T> T deepClone(T source){
        if(source == null){
            return null;
        }
        if(source.getClass().isPrimitive() || source.getClass() == String.class){
            return source;
        } else {
            if(source instanceof ICloneable){
                return (T) ((ICloneable)source).deepClone();
            }
            if(source.getClass() == ArrayList.class){
                ArrayList<?> sourceList = (ArrayList<?>) source;
                ArrayList<Object> clonedList = new ArrayList<>(sourceList.size());
                for(Object value:((ArrayList<?>) source)){
                    clonedList.add(deepClone(value));
                }
                return (T) clonedList;
            }
            if(source.getClass() == HashMap.class){
                HashMap<?,?> sourceMap = (HashMap<?, ?>) source;
                HashMap<Object, Object> clonedMap = new HashMap<>(sourceMap.size());
                for (Map.Entry<?,?> entry: sourceMap.entrySet()){
                    clonedMap.put(deepClone(entry.getKey()),deepClone(entry.getValue()));
                }
                return (T) clonedMap;
            }
        }
        String json = JSON.toJSONString(source);
        logger.warn("Unexpected type {} for deep clone:{}",source.getClass(),json);
        return (T) JSON.parseObject(json,source.getClass());
    }
}
