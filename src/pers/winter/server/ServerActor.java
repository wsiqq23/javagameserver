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
package pers.winter.server;

import pers.winter.config.ApplicationConfig;
import pers.winter.config.ConfigManager;
import pers.winter.message.MessageCenter;
import pers.winter.server.grpc.GrpcServer;
import pers.winter.server.kcp.KcpServer;
import pers.winter.server.socket.IServer;
import pers.winter.server.socket.SocketServer;
import pers.winter.server.socket.WebSocketServer;

public class ServerActor {
    public static final ServerActor INSTANCE = new ServerActor();
    private IServer socketServer;
    private IServer webSocketServer;
    private IServer kcpServer;
    public void start() throws Exception{
        MessageCenter.INSTANCE.start();
        if(ConfigManager.INSTANCE.getConfig(ApplicationConfig.class).getSocketPort()>0){
            socketServer = new SocketServer();
            socketServer.start(ConfigManager.INSTANCE.getConfig(ApplicationConfig.class).getSocketPort());
        }
        if(ConfigManager.INSTANCE.getConfig(ApplicationConfig.class).getWebSocketPort()>0) {
            webSocketServer = new WebSocketServer();
            webSocketServer.start(ConfigManager.INSTANCE.getConfig(ApplicationConfig.class).getWebSocketPort());
        }
        if(ConfigManager.INSTANCE.getConfig(ApplicationConfig.class).getKcpPort()>0){
            kcpServer = new KcpServer();
            kcpServer.start(ConfigManager.INSTANCE.getConfig(ApplicationConfig.class).getKcpPort());
        }
        if(ConfigManager.INSTANCE.getConfig(ApplicationConfig.class).getGrpcPort()>0){
            GrpcServer server = new GrpcServer();
            server.start(ConfigManager.INSTANCE.getConfig(ApplicationConfig.class).getGrpcPort());
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
        if(kcpServer != null){
            kcpServer.stop();
        }
    }
}
