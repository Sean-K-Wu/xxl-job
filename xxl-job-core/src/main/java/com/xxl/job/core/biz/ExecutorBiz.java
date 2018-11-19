package com.xxl.job.core.biz;

import com.xxl.job.core.biz.model.LogResult;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.biz.model.TriggerParam;

/**
 * Created by xuxueli on 17/3/1.
 */
public interface ExecutorBiz {

    /**
     * 心跳检测
     *
     * @return
     */
    ReturnT<String> beat();

    /**
     * 空闲心跳检测
     *
     * @param jobId
     * @return
     */
    ReturnT<String> idleBeat(int jobId);

    /**
     * 终止任务
     *
     * @param jobId
     * @return
     */
    ReturnT<String> kill(int jobId);

    /**
     * 获取日志
     *
     * @param logDateTime
     * @param logId
     * @param fromLineNum
     * @return
     */
    ReturnT<LogResult> log(long logDateTime, int logId, int fromLineNum);

    /**
     * 执行任务
     *
     * @param triggerParam
     * @return
     */
    ReturnT<String> run(TriggerParam triggerParam);
}