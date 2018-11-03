package com.xxl.job.core.biz.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Created by xuxueli on 16/7/22.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TriggerParam implements Serializable {
    private static final long serialVersionUID = 42L;

    private int jobId;

    private String executorHandler;
    private String executorParams;
    private String executorBlockStrategy;
    private int executorTimeout;

    private int logId;
    private long logDateTime;

    private String glueType;
    private String glueSource;
    private long glueUpdateTime;

    private int broadcastIndex;
    private int broadcastTotal;
}