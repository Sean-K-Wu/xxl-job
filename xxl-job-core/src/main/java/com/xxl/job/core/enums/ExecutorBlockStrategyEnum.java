package com.xxl.job.core.enums;

import com.google.common.base.Strings;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by xuxueli on 17/5/9.
 */
@AllArgsConstructor
public enum ExecutorBlockStrategyEnum {

    /**
     * 单机串行
     */
    SERIAL_EXECUTION("Serial execution"),
    /**
     * 丢弃后续调度
     */
    DISCARD_LATER("Discard Later"),
    /**
     * 覆盖之前调度
     */
    COVER_EARLY("Cover Early");

    @Getter
    @Setter
    private String title;

    public static ExecutorBlockStrategyEnum match(String name, ExecutorBlockStrategyEnum defaultItem) {
        if (!Strings.isNullOrEmpty(name)) {
            for (ExecutorBlockStrategyEnum item : ExecutorBlockStrategyEnum.values()) {
                if (item.name().equals(name)) {
                    return item;
                }
            }
        }
        return defaultItem;
    }
}