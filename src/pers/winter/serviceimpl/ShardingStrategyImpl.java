package pers.winter.serviceimpl;

import pers.winter.db.mysql.IShardingStrategy;

public class ShardingStrategyImpl implements IShardingStrategy {
    @Override
    public int getShardingByID(long id) {
        return 0;
    }
}
