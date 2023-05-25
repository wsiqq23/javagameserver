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
package pers.winter.serviceimpl;

import com.alibaba.fastjson.JSON;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pers.winter.message.AnnMessageServiceImpl;
import pers.winter.message.json.Bye;
import pers.winter.message.json.Hello;

@AnnMessageServiceImpl
public class DemoServiceIml {
    private static final Logger logger = LogManager.getLogger(DemoServiceIml.class);
    public void hello(Hello request){
        logger.info("Receive Hello: {}", JSON.toJSONString(request));
        Hello response = new Hello();
        response.data = "Fine, thank you, and you?";
        response.time = System.currentTimeMillis();
        request.getChannel().writeAndFlush(response);
    }

    public void bye(Bye request){
        logger.info("Receive Bye: {}", JSON.toJSONString(request));
        Bye response = new Bye();
        response.data1 = "Good night!";
        response.data2 = "Have a good dream.";
        request.getChannel().writeAndFlush(response);
    }
}
