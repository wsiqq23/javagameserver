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

import io.netty.channel.Channel;

/**
 * The UserSession class represents a user session in the application.
 * It associates a user's ID with a Netty channel for communication.
 * @author Winter
 */
public class UserSession {
    private final long id;
    private Channel channel;
    /**
     * Constructs a new UserSession with the provided user ID.
     *
     * @param id The unique identifier for the user session.
     */
    public UserSession(long id){
        this.id = id;
    }
    /**
     * Retrieves the unique identifier for the user session.
     *
     * @return The user session's ID.
     */
    public long getId(){
        return id;
    }
    /**
     * Retrieves the Netty channel associated with the user session.
     *
     * @return The Netty channel associated with the session, or null if not set.
     */
    public Channel getChannel() {
        return channel;
    }
    /**
     * Associates a Netty channel with the user session.
     *
     * @param channel The Netty channel to be associated with the session.
     */
    public void setChannel(Channel channel){
        this.channel = channel;
    }
    /**
     * Sends a message through the associated Netty channel.
     * The message is flushed to the network.
     *
     * @param message The message object to be sent.
     */
    public void sendMessage(Object message){
        if(this.channel != null){
            this.channel.writeAndFlush(message);
        }
    }
}
