package com.xxl.job.executor.inject.inject;

import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.log.XxlJobLogger;
import com.xxl.job.executor.inject.component.Man;

import javax.annotation.Resource;

public class ResourceByType extends IJobHandler {

    @Resource
    private Man man1;

    @Override
    public ReturnT<String> execute(String param) {
        XxlJobLogger.log("inject class: {}", man1.getClass().getName());
        return ReturnT.SUCCESS;
    }
}