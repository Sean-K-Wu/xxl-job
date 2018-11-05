package com.xxl.job.admin.core.model;

import lombok.Data;

import java.util.Date;

/**
 * xxl-job info
 *
 * @author xuxueli  2016-1-12 18:25:49
 */
@Data
public class XxlJobInfo {

    /**
     * 主键ID	    (JobKey.name)
     */
    private int id;

    /**
     * 执行器主键ID	(JobKey.group)
     */
    private int jobGroup;
    /**
     * 任务执行CRON表达式 【base on quartz】
     */
    private String jobCron;
    private String jobDesc;

    private Date addTime;
    private Date updateTime;

    /**
     * 负责人
     */
    private String author;
    /**
     * 报警邮件
     */
    private String alarmEmail;

    /**
     * 执行器路由策略
     */
    private String executorRouteStrategy;
    /**
     * 执行器，任务Handler名称
     */
    private String executorHandler;
    /**
     * 执行器，任务参数
     */
    private String executorParam;
    /**
     * 阻塞处理策略
     */
    private String executorBlockStrategy;
    /**
     * 任务执行超时时间，单位秒
     */
    private int executorTimeout;
    /**
     * 失败重试次数
     */
    private int executorFailRetryCount;

    /**
     * GLUE类型	#com.xxl.job.core.glue.GlueTypeEnum
     */
    private String glueType;
    /**
     * GLUE源代码
     */
    private String glueSource;
    /**
     * GLUE备注
     */
    private String glueRemark;
    /**
     * GLUE更新时间
     */
    private Date glueUpdatetime;

    /**
     * 子任务ID，多个逗号分隔
     */
    private String childJobId;

    /***
     * 任务状态 【base on quartz】
     * copy from quartz
     */
    private String jobStatus;
}