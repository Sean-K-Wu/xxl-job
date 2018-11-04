package com.xxl.job.executor.inject.inject;

import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.log.XxlJobLogger;
import com.xxl.job.executor.inject.component.Human;

import javax.annotation.Resource;

public class ResourceByFieldName extends IJobHandler {

    @Resource
    private Human man;

    @Override
    public ReturnT<String> execute(String param) {
        XxlJobLogger.log("inject class: {}", man.getClass().getName());
        return ReturnT.SUCCESS;
    }
}