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
package pers.winter.server.codec;

/**
 * Define some constants used for network
 * @author Winter
 */
public class Constants {
    /** Message encode flag for json */
    public static final byte CODEC_JSON = 1;
    /** Message encode flag for proto */
    public static final byte CODEC_PROTO = 2;
    /** Length for message package header, a byte for encode flag and an int for message id */
    public static final int ENCODE_HEADER_LENGTH = Byte.BYTES + Integer.BYTES;
    /** Max length for each message package */
    public static final int MAX_PACKAGE_LENGTH = 1024 * 1024;
}
