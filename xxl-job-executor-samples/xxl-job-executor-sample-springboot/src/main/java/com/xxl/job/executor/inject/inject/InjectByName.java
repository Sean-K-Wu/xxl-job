package com.xxl.job.executor.inject.inject;

import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.log.XxlJobLogger;
import com.xxl.job.executor.inject.component.Human;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.inject.Inject;

public class InjectByName extends IJobHandler {

    @Inject
    private Human man;

    @Inject
    @Qualifier("nobody")
    private Human woman;

    @Override
    public ReturnT<String> execute(String param) {
        XxlJobLogger.log("inject class: {}", man.getClass().getName());
        XxlJobLogger.log("inject class: {}", woman.getClass().getName());
        return ReturnT.SUCCESS;
    }
}