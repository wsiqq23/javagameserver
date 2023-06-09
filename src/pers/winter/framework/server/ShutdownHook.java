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
package pers.winter.framework.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pers.winter.framework.db.DatabaseCenter;

public class ShutdownHook extends Thread{
    private static final Logger logger = LogManager.getLogger(ShutdownHook.class);

    @Override
    public void run() {
        logger.warn("Shutdown Start!");
        ServerActor.INSTANCE.terminate();
        DatabaseCenter.INSTANCE.terminate();
        logger.warn("Shutdown Over!");
    }
}
