package com.xxl.job.core.biz;

import com.xxl.job.core.biz.model.LogResult;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.biz.model.TriggerParam;

/**
 * Created by xuxueli on 17/3/1.
 */
public interface ExecutorBiz {

    /**
     * 心跳
     *
     * @return
     */
    ReturnT<String> beat();

    /**
     * 闲时心跳
     *
     * @param jobId
     * @return
     */
    ReturnT<String> idleBeat(int jobId);

    /**
     * 停止任务
     *
     * @param jobId
     * @return
     */
    ReturnT<String> kill(int jobId);

    /**
     * 记录日志
     *
     * @param logDateTime
     * @param logId
     * @param fromLineNum
     * @return
     */
    ReturnT<LogResult> log(long logDateTime, int logId, int fromLineNum);

    /**
     * 运行
     *
     * @param triggerParam
     * @return
     */
    ReturnT<String> run(TriggerParam triggerParam);
}