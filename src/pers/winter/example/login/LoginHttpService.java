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
package pers.winter.example.login;

import com.mongodb.client.model.Filters;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pers.winter.db.entity.Account;
import pers.winter.example.Constants;
import pers.winter.example.sdk.AbstractBaseSdk;
import pers.winter.example.sdk.SdkCenter;
import pers.winter.example.server.allocator.ServerAllocator;
import pers.winter.framework.entity.EntityManager;
import pers.winter.framework.server.http.AnnHttpRequestMapping;
import pers.winter.framework.server.http.AnnHttpRestController;
import pers.winter.framework.server.http.HttpServerHandler;
import pers.winter.framework.utils.HttpUtil;
import pers.winter.framework.utils.SnowFlakeIdGenerator;
import pers.winter.message.multiroles.login.LoginResponse;

import java.util.List;
import java.util.Map;
import java.util.Set;

@AnnHttpRestController
public class LoginHttpService {
    private Logger logger = LogManager.getLogger(LoginHttpService.class);
    @AnnHttpRequestMapping(value = "/login",method = HttpServerHandler.HttpRequestMethod.GET)
    public FullHttpResponse login(FullHttpRequest request) throws Exception {
        LoginResponse response = new LoginResponse();
        Map<String, List<String>> parameters = HttpUtil.getParametersFromGet(request);
        if(!parameters.containsKey("channel") || !parameters.containsKey("platformId")){
            response.code = Constants.ResponseCodes.FAIL.getValue();
            logger.info("Illegal arguments for http login:{}",parameters);
            return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.copiedBuffer(response.serialized()));
        }
        String channel = parameters.get("channel").get(0);
        AbstractBaseSdk sdk = SdkCenter.INSTANCE.getSdk(channel);
        if(!sdk.login(request)){
            response.code = Constants.ResponseCodes.FAIL.getValue();
            logger.info("SDK verify failed for channel {}.",channel);
            return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.copiedBuffer(response.serialized()));
        }
        String platformId = parameters.get("platformId").get(0);
        long now = System.currentTimeMillis();
        List<Account> accounts = EntityManager.INSTANCE.selectCustom(Filters.and(Filters.eq("channel",channel),Filters.eq("platformId",platformId)),Account.class);
        Account account;
        if(accounts.isEmpty()){
            account = new Account();
            account.setId(SnowFlakeIdGenerator.generateId());
            account.setChannel(channel);
            account.setPlatformId(platformId);
            account.setCreateTime(now);
            account.insert();
        } else {
            account = accounts.get(0);
            account.update();
        }
        account.setRecentLogin(now);
        EntityManager.INSTANCE.buildCache(account.getKeyID(),Account.class);
        if(EntityManager.INSTANCE.save(Set.of(account))){
            response.accountId = account.getId();
            response.server = ServerAllocator.allocateServer(account);
            response.timestamp = now;
            response.signature = ServerAllocator.buildSignature(response.accountId, response.timestamp);
            response.code = Constants.ResponseCodes.SUCCESS.getValue();
        } else {
            logger.info("Save failed for channel:{}, platformId:{}.",channel,platformId);
            response.code = Constants.ResponseCodes.FAIL.getValue();
        }
        return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,HttpResponseStatus.OK,Unpooled.copiedBuffer(response.serialized()));
    }
}
