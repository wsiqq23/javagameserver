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
package pers.winter.example.session;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.netty.channel.Channel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pers.winter.example.Constants;
import pers.winter.example.config.GameConfig;
import pers.winter.framework.config.ConfigManager;

import java.util.concurrent.TimeUnit;

/**
 * The SessionContainer class is responsible for managing user sessions.
 * It uses a cache to store and retrieve user sessions based on their UID.
 * @author Winter
 */
public class SessionContainer {
    private static final Logger logger = LogManager.getLogger(SessionContainer.class);
    private static SessionContainer instance;

    private Cache<Long, UserSession> sessions;

    private SessionContainer(){
        sessions = Caffeine.newBuilder().expireAfterAccess(ConfigManager.INSTANCE.getConfig(GameConfig.class).getSessionExpire(), TimeUnit.SECONDS).build();
    }

    /**
     * Returns the singleton instance of SessionContainer.
     *
     * @return The SessionContainer instance.
     */
    public static SessionContainer getInstance(){
        if(instance == null){
            synchronized (SessionContainer.class){
                if(instance == null){
                    instance = new SessionContainer();
                }
            }
        }
        return instance;
    }

    /**
     * Builds a new UserSession or retrieves an existing session based on UID.
     * Associates the session with the provided channel.
     *
     * @param uid     The unique identifier for the user session.
     * @param channel The Netty channel associated with the session.
     * @return The UserSession object.
     */
    public UserSession buildSession(long uid, Channel channel){
        logger.info("Session {} created.", uid);
        channel.attr(Constants.ATTRIBUTE_KEY_USER_ID).set(uid);
        UserSession session = sessions.get(uid, id-> new UserSession(uid));
        session.setChannel(channel);
        return session;
    }

    /**
     * Retrieves an existing UserSession based on UID.
     *
     * @param uid The unique identifier for the user session.
     * @return The UserSession object if found, or null if not present.
     */
    public UserSession getSession(long uid){
        return sessions.getIfPresent(uid);
    }
}
