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
package pers.winter.example;

import io.netty.util.AttributeKey;

/**
 * A utility class that contains constants used in the demo application.
 * @author Winter
 */
public class Constants {
    /** Redis channel for server list update notifications. */
    public static final String REDIS_CHANNEL_SERVER_LIST_UPDATE = "channel_server_list_update";
    /** Redis key for storing server list. */
    public static final String REDIS_KEY_SERVER_LIST = "key_server_list";
    /** Attribute Key representing for whether the channel is verified */
    public static final AttributeKey<Boolean> ATTRIBUTE_KEY_VERIFIED = AttributeKey.valueOf("verified");
    /** Attribute Key for user id of the channel */
    public static final AttributeKey<Long> ATTRIBUTE_KEY_USER_ID = AttributeKey.valueOf("uid");
    /**
     * Enumeration of response codes used for operations.
     */
    public enum ResponseCodes{
        /** Generic codes for operation success */
        SUCCESS(0,"Generic codes for operation success"),
        /** Generic codes for operation fail */
        FAIL(1,"Generic codes for operation fail."),
        CONNECTION_NOT_VERIFIED(100,"The socket connection has not been verified by handshake."),
        HANDSHAKE_VERIFY_FAILED(101,"Handshake verify signature failed."),
        HANDSHAKE_ACCOUNT_NOT_EXISTS(102, "The account of handshake doesn't exist."),
        ROLE_NOT_EXISTS(103,"The role for login doesn't exist."),
        ROLE_CREATE_FAIL(104,"Failed to create role.")
        ;
        private int value;
        private String comments;
        public int getValue(){return value;}
        public String getComments(){return comments;}
        ResponseCodes(int value, String comments){
            this.value = value;
            this.comments = comments;
        }
    }
}
