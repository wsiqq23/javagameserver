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

import pers.winter.framework.config.ApplicationConfig;
import pers.winter.framework.config.ConfigManager;
import pers.winter.framework.message.MessageCenter;
import pers.winter.framework.server.http.HttpServer;
import pers.winter.framework.server.socket.SocketServer;
import pers.winter.framework.server.socket.WebSocketServer;

public class ServerActor {
    public static final ServerActor INSTANCE = new ServerActor();
    private IServer socketServer;
    private IServer webSocketServer;
    private IServer httpServer;
    public void start() throws Exception{
        MessageCenter.INSTANCE.start();
        ApplicationConfig appConfig = ConfigManager.INSTANCE.getConfig(ApplicationConfig.class);
        if(appConfig.getSocketPort()>0){
            socketServer = new SocketServer();
            socketServer.start(appConfig.getSocketPort());
        }
        if(appConfig.getWebSocketPort()>0) {
            webSocketServer = new WebSocketServer();
            webSocketServer.start(appConfig.getWebSocketPort());
        }
        if(appConfig.getHttpPort()>0){
            httpServer = new HttpServer();
            httpServer.start(appConfig.getHttpPort());
        }
    }

    public void terminate() {
        MessageCenter.INSTANCE.terminate();
        if(socketServer != null){
            socketServer.stop();
        }
        if(webSocketServer != null) {
            webSocketServer.stop();
        }
        if(httpServer != null){
            httpServer.stop();
        }
    }
}
