package com.xxl.job.admin.core.model;

import lombok.Data;

import java.util.Date;

/**
 * xxl-job log, used to track trigger process
 *
 * @author xuxueli  2015-12-19 23:19:09
 */
@Data
public class XxlJobLog {

    private int id;

    private int jobGroup;
    private int jobId;

    private String executorAddress;
    private String executorHandler;
    private String executorParam;
    private String executorShardingParam;
    private int executorFailRetryCount;

    private Date triggerTime;
    private int triggerCode;
    private String triggerMsg;

    private Date handleTime;
    private int handleCode;
    private String handleMsg;
}