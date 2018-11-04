package com.xxl.job.executor.inject.inject;

import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.log.XxlJobLogger;
import com.xxl.job.executor.inject.component.Human;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.inject.Inject;

public class InjectByQualifier extends IJobHandler {

    @Inject
    @Qualifier("man")
    private Human woman;

    @Override
    public ReturnT<String> execute(String param) {
        XxlJobLogger.log("inject class: {}", woman.getClass().getName());
        return ReturnT.SUCCESS;
    }
}