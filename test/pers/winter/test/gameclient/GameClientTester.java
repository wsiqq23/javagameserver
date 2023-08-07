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

import pers.winter.framework.config.ConfigManager;
import pers.winter.framework.entity.Transaction;
import pers.winter.framework.timer.TimerTaskManager;
import pers.winter.message.json.Bye;
import pers.winter.message.json.Hello;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class GameClientTester {
    private static List<GameClient> clients = new ArrayList<>();
    private static void initActionList(GameClient client){
        TimerTaskManager.getInstance().newRepeatedTimeout(new Transaction("GameClient") {
            @Override
            protected void process() {
                Hello hello = new Hello();
                hello.time = System.currentTimeMillis();
                hello.data = "How are you?";
                Bye bye = new Bye();
                bye.data1 = "Good night!";
                bye.data2 = "See you tomorrow.";
                client.addAction(hello);
                client.addAction(bye);
            }
            @Override
            protected void failed() {
            }
        },1,1, TimeUnit.SECONDS,0);
    }
    public static void main(String[] args) throws Exception {
        ConfigManager.INSTANCE.init();
        for(int i = 0;i<1;i++){
            GameClient client = new GameClient("demo","wx_1000"+i);
            client.login("127.0.0.1",7070);
            initActionList(client);
            clients.add(client);
        }
    }
}
