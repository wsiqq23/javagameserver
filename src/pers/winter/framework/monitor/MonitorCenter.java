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
package pers.winter.framework.monitor;

import pers.winter.framework.utils.ClassScanner;

import java.util.List;

/**
 * The MonitorCenter class is responsible for initializing and reporting monitor data
 * using an implementation of the IMonitorHandler interface.
 * @author Winter
 */
public class MonitorCenter {
    /**
     * The singleton instance of the MonitorCenter.
     */
    public static final MonitorCenter INSTANCE = new MonitorCenter();

    private IMonitorHandler handler;

    private MonitorCenter(){}

    /**
     * Initializes the MonitorCenter by finding and instantiating an implementation of the IMonitorHandler interface.
     * @throws Exception if an error occurs during initialization.
     */
    public void init() throws Exception {
        List<Class<?>> clsList = ClassScanner.getSubTypesOf(IMonitorHandler.class);
        if(!clsList.isEmpty()){
            Class<?> cls = clsList.get(0);
            this.handler = (IMonitorHandler) cls.getDeclaredConstructor().newInstance();
        }
    }

    /**
     * Reports monitor data using the configured IMonitorHandler implementation.
     * @param monitorBean the monitor data to be reported.
     */
    public void report(AbstractBaseMonitorBean monitorBean){
        if(handler != null){
            handler.report(monitorBean);
        }
    }
}
