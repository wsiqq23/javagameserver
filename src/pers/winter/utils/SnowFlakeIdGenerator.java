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
package pers.winter.utils;

public class SnowFlakeIdGenerator {
    private static long nodeID;
    private static long lastTs;
    private static long lastInnerID = -1;
    // The node ID occupies 14 bits and supports a maximum of 16384 nodes.
    private static final long maxNodeID = 1 << 14;
    // The internal ID occupies 8 bits, and each node supports the addition of 256 new IDs per millisecond.
    private static final long maxInnerID = 1 << 8;
    public static void initNodeID(long id) throws IllegalArgumentException{
        if(id >= maxNodeID || id < 0){
            throw new IllegalArgumentException("Invalid node id: " + id);
        }
        nodeID = id;
    }
    public static synchronized long generateId(){
        long ts = System.currentTimeMillis();
        if (ts < lastTs) {
            ts = lastTs;
        } else if (ts > lastTs) {
            lastInnerID = -1;
        }
        lastTs = ts;
        lastInnerID++;
        if (lastInnerID >= maxInnerID) {
            lastTs = ++ts;
            lastInnerID = 0;
        }
        return ts << 22 | nodeID << 8 | lastInnerID;
    }

    public static long revertNodeFromID(long snowflakeID){
        int mask = (1 << 14) - 1;
        return snowflakeID >> 8 & mask;
    }
}
