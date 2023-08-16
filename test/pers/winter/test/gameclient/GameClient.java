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
package pers.winter.test.gameclient;

import com.alibaba.fastjson.JSON;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pers.winter.bean.CreateRoleBean;
import pers.winter.bean.SocketServer;
import pers.winter.example.Constants;
import pers.winter.message.multiroles.login.*;
import pers.winter.test.http.HttpRequestUtil;
import pers.winter.test.socket.client.SocketClient;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

public class GameClient {
    private static final Logger logger = LogManager.getLogger(GameClient.class);
    private final String channel;
    private final String platformId;
    private long accountId;
    private Consumer<Object> messageHandler;
    private SocketClient socketClient;
    private LinkedBlockingQueue<Object> actionQueue = new LinkedBlockingQueue<>();
    public GameClient(String channel,String platformId){
        this.channel = channel;
        this.platformId = platformId;
        initMessageHandler();
    }
    public void login(String ip,int port) throws Exception {
        byte[] response = HttpRequestUtil.get(String.format("http://%s:%d/login?channel=%s&platformId=%s",ip,port,channel,platformId));
        LoginResponse loginResponse = JSON.parseObject(new String(response),LoginResponse.class);
        if(loginResponse.code == Constants.ResponseCodes.SUCCESS.getValue()){
            accountId = loginResponse.accountId;
            logger.info("Http login to {}:{} success! Account:{}.",ip,port,accountId);
            socketHandshake(loginResponse.server, loginResponse.timestamp, loginResponse.signature);
        } else {
            logger.error("Http login to {}:{} failed! Code:{}.",ip,port,loginResponse.code);
        }
    }
    public void addAction(Object action){
        this.actionQueue.add(action);
    }
    private void socketHandshake(SocketServer socketServer,long timestamp,String signature) throws Exception {
        socketClient = new SocketClient(socketServer.getIp(),socketServer.getPort());
        socketClient.connect(messageHandler);
        Handshake handshake = new Handshake();
        handshake.accountId = accountId;
        handshake.timestamp = timestamp;
        handshake.signature = signature;
        socketClient.send(handshake);
    }
    private void initMessageHandler(){
        messageHandler = message -> {
            if(message.getClass() == HandshakeResponse.class){
                HandshakeResponse handshakeResponse = (HandshakeResponse) message;
                if(handshakeResponse.code == Constants.ResponseCodes.SUCCESS.getValue()) {
                    logger.info("Socket login success!");
                    RoleLogin roleLogin = new RoleLogin();
                    if (handshakeResponse.roles.isEmpty()) {
                        roleLogin.createRoleBean = new CreateRoleBean();
                        roleLogin.createRoleBean.setJob((byte) 1);
                        roleLogin.createRoleBean.setName("Medivh");
                        roleLogin.createRoleBean.setRace((byte) 1);
                        roleLogin.createRoleBean.setSex((short) 1);
                    } else {
                        roleLogin.roleID = handshakeResponse.roles.get(0).getId();
                    }
                    socketClient.send(roleLogin);
                } else {
                    logger.error("Socket login failed! Code:{}",handshakeResponse.code);
                    socketClient.disconnect();
                }
                return;
            } else if(message.getClass() == RoleLoginResponse.class){
                RoleLoginResponse roleLoginResponse = (RoleLoginResponse) message;
                if(roleLoginResponse.code != Constants.ResponseCodes.SUCCESS.getValue()){
                    logger.error("Role login failed! Code:{}",roleLoginResponse.code);
                    socketClient.disconnect();
                    return;
                } else {
                    logger.info("Role login success!");
                }
            }
            processAction();
        };
    }
    private void processAction(){
        Object message;
        try {
            message = actionQueue.take();
            socketClient.send(message);
        } catch (InterruptedException e) {
            logger.error("Interrupt!",e);
        }
    }
}
