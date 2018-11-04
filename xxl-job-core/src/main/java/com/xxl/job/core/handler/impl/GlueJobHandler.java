package com.xxl.job.core.handler.impl;

import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.log.XxlJobLogger;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * glue job handler
 *
 * @author xuxueli 2016-5-19 21:05:45
 */
@Data
@AllArgsConstructor
public class GlueJobHandler extends IJobHandler {
    private IJobHandler jobHandler;
    private long glueUpdatetime;

    @Override
    public ReturnT<String> execute(String param) throws Exception {
        XxlJobLogger.log("----------- glue.version:" + glueUpdatetime + " -----------");
        return jobHandler.execute(param);
    }
}