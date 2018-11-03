package com.xxl.job.core.util;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * sharding vo
 *
 * @author xuxueli 2017-07-25 21:26:38
 */
public class ShardingUtil {

    private static InheritableThreadLocal<ShardingVO> contextHolder = new InheritableThreadLocal<>();

    public static void setShardingVo(ShardingVO shardingVo) {
        contextHolder.set(shardingVo);
    }

    public static ShardingVO getShardingVo() {
        return contextHolder.get();
    }

    @Data
    @AllArgsConstructor
    public static class ShardingVO {

        /**
         * sharding index
         */
        private Integer index;
        /**
         * sharding total
         */
        private Integer total;
    }
}