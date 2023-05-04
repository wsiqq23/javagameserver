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

import pers.winter.server.socket.SocketServer;
import pers.winter.server.socket.WebSocketServer;

/**
 * The main entrance of application
 * @author Winter
 */
public class Main {
    private static SocketServer socketServer;
    private static WebSocketServer webSocketServer;
    public static void main(String[] args){
        socketServer = new SocketServer();
        socketServer.start(7001);
        webSocketServer = new WebSocketServer();
        webSocketServer.start(7002);
    }
}
