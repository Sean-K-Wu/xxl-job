package com.xxl.job.executor.inject.inject;

import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.log.XxlJobLogger;
import com.xxl.job.executor.inject.component.Man;

import javax.inject.Inject;

public class InjectByType extends IJobHandler {

    @Inject
    private Man woman;

    @Override
    public ReturnT<String> execute(String param) {
        XxlJobLogger.log("inject class: {}", woman.getClass().getName());
        return ReturnT.SUCCESS;
    }
}
