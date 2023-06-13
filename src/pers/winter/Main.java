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
package pers.winter;

import pers.winter.config.ConfigManager;
import pers.winter.entity.EntityManager;
import pers.winter.server.ServerActor;
import pers.winter.server.ShutdownHook;

/**
 * The main entrance of application
 * @author Winter
 */
public class Main {
    public static void main(String[] args) throws Throwable {
        ConfigManager.INSTANCE.init();
        EntityManager.INSTANCE.init();
        ServerActor.INSTANCE.start();
        Runtime.getRuntime().addShutdownHook(new ShutdownHook());
    }
}
