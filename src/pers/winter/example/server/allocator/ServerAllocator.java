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
package pers.winter.example.server.allocator;

import com.google.common.hash.Hashing;
import pers.winter.bean.SocketServer;
import pers.winter.db.entity.Account;
import pers.winter.example.config.GameConfig;
import pers.winter.framework.config.ApplicationConfig;
import pers.winter.framework.config.ConfigManager;

import java.nio.charset.StandardCharsets;

/**
 * The ServerAllocator class provides methods for allocating servers and
 * verifying signatures in the application.
 * @author Winter
 */
public class ServerAllocator {
    /**
     * Allocates a SocketServer for the specified account.
     *
     * @param account The Account for which to allocate a server.
     * @return A SocketServer instance representing the allocated server.
     */
    public static SocketServer allocateServer(Account account){
        SocketServer server = new SocketServer();
        server.ip = "127.0.0.1";
        server.port = ConfigManager.INSTANCE.getConfig(ApplicationConfig.class).getSocketPort();
        server.name = "Karazhan";
        return server;
    }
    /**
     * Builds a signature using the specified account ID and timestamp.
     *
     * @param accountId The account ID for which to build the signature.
     * @param timestamp The timestamp for which to build the signature.
     * @return The generated signature.
     */
    public static String buildSignature(long accountId, long timestamp){
        StringBuilder signString = new StringBuilder();
        signString.append(accountId);
        signString.append(ConfigManager.INSTANCE.getConfig(GameConfig.class).getSignatureKey());
        signString.append(timestamp);
        return Hashing.sha256().hashString(signString.toString(), StandardCharsets.UTF_8).toString();
    }
    /**
     * Verifies a signature for the specified account ID, timestamp, and signature.
     *
     * @param accountId The account ID to verify the signature for.
     * @param timestamp The timestamp associated with the signature.
     * @param signature The signature to be verified.
     * @return True if the signature is valid, false otherwise.
     */
    public static boolean verifySignature(long accountId, long timestamp,String signature){
        if(ConfigManager.INSTANCE.getConfig(GameConfig.class).getSignatureValidTime() > 0){
            return buildSignature(accountId,timestamp).equals(signature) && (System.currentTimeMillis() - timestamp < ConfigManager.INSTANCE.getConfig(GameConfig.class).getSignatureValidTime());
        } else {
            return buildSignature(accountId,timestamp).equals(signature);
        }
    }
}
