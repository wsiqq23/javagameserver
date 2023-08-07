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

import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.AttributeKey;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pers.winter.db.entity.Account;
import pers.winter.db.entity.Role;
import pers.winter.example.Constants;
import pers.winter.example.role.RoleController;
import pers.winter.example.server.allocator.ServerAllocator;
import pers.winter.example.session.SessionContainer;
import pers.winter.framework.entity.EntityManager;
import pers.winter.framework.entity.Transaction;
import pers.winter.framework.message.AnnMessageMethod;
import pers.winter.framework.message.AnnMessageServiceImpl;
import pers.winter.message.multiroles.login.Handshake;
import pers.winter.message.multiroles.login.HandshakeResponse;
import pers.winter.message.multiroles.login.RoleLogin;
import pers.winter.message.multiroles.login.RoleLoginResponse;

import java.util.List;

@AnnMessageServiceImpl
public class LoginSocketService {
    private static final Logger logger = LogManager.getLogger(LoginSocketService.class);
    private static final AttributeKey<Long> ATTRIBUTE_KEY_ACCOUNT_ID = AttributeKey.valueOf("accountId");

    @AnnMessageMethod(retryCount = 0)
    public void handshake(Handshake request) {
        HandshakeResponse response = new HandshakeResponse();
        if (!ServerAllocator.verifySignature(request.accountId, request.timestamp, request.signature)) {
            response.code = Constants.ResponseCodes.HANDSHAKE_VERIFY_FAILED.getValue();
            request.getChannel().writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
            return;
        }
        try {
            List<Account> accounts = EntityManager.INSTANCE.selectByKey(request.accountId, Account.class);
            if (accounts.isEmpty()) {
                response.code = Constants.ResponseCodes.HANDSHAKE_ACCOUNT_NOT_EXISTS.getValue();
                request.getChannel().writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
                return;
            }
            List<Role> roles = EntityManager.INSTANCE.selectCustom(0,"select * from role where accountId="+request.accountId,Role.class);
            response.code = Constants.ResponseCodes.SUCCESS.getValue();
            response.roles = roles;
            request.getChannel().attr(Constants.ATTRIBUTE_KEY_VERIFIED).set(true);
            request.getChannel().attr(ATTRIBUTE_KEY_ACCOUNT_ID).set(request.accountId);
        } catch (Exception e) {
            logger.error("Exception on handshake.", e);
            response.code = Constants.ResponseCodes.FAIL.getValue();
        }
        request.getChannel().writeAndFlush(response);
    }

    @AnnMessageMethod(retryCount = 0)
    public void roleLogin(RoleLogin request) throws Exception {
        Long accountID = request.getChannel().attr(ATTRIBUTE_KEY_ACCOUNT_ID).getAndSet(null);
        RoleLoginResponse response = new RoleLoginResponse();
        if(accountID == null){
            response.code = Constants.ResponseCodes.HANDSHAKE_VERIFY_FAILED.getValue();
            request.getChannel().writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
            return;
        }
        Account account = EntityManager.INSTANCE.selectByKey(accountID,Account.class).get(0);
        Role role;
        if(request.roleID>0){
            List<Role> roles = EntityManager.INSTANCE.selectByKey(request.roleID,Role.class);
            if(roles.isEmpty()){
                logger.info("The login role {} does not exist.",request.roleID);
                response.code = Constants.ResponseCodes.ROLE_NOT_EXISTS.getValue();
                request.getChannel().writeAndFlush(response);
                return;
            }
            role = roles.get(0);
            if(role.getAccountId() != accountID){
                logger.warn("The login role {} does not belong to account {}.",request.roleID,accountID);
                response.code = Constants.ResponseCodes.ROLE_NOT_EXISTS.getValue();
                request.getChannel().writeAndFlush(response);
                return;
            }
            role.setRecentLogin(System.currentTimeMillis());
        } else {
            role = RoleController.INSTANCE.createRole(accountID, request.createRoleBean);
            if(role == null){
                logger.info("Create role failed! Create bean:{}.", JSON.toJSONString(request.createRoleBean));
                response.code = Constants.ResponseCodes.ROLE_CREATE_FAIL.getValue();
                request.getChannel().writeAndFlush(response);
                return;
            }
        }
        role.setRecentLogin(System.currentTimeMillis());
        account.setRecentRole(role.getId());
        role.update();
        account.update();
        response.code = Constants.ResponseCodes.SUCCESS.getValue();
        response.roleID = role.getId();
        Transaction.setCommitListener(()->{
            SessionContainer.getInstance().buildSession(role.getId(),request.getChannel());
            request.getChannel().writeAndFlush(response);
        });
    }
}
