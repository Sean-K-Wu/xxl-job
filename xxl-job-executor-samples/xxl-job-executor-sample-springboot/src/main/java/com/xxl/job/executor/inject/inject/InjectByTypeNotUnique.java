package com.xxl.job.executor.inject.inject;

import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.log.XxlJobLogger;
import com.xxl.job.executor.inject.component.Human;

import javax.inject.Inject;

public class InjectByTypeNotUnique extends IJobHandler {

    @Inject
    private Human human;

    @Override
    public ReturnT<String> execute(String param) {
        XxlJobLogger.log("inject class: {}", human.getClass().getName());
        return ReturnT.SUCCESS;
    }
}