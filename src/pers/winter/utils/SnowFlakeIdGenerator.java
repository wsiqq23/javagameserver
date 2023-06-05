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
