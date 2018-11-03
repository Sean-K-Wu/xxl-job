package com.xxl.job.core.thread;

import com.google.common.base.Strings;
import com.xxl.job.core.biz.AdminBiz;
import com.xxl.job.core.biz.model.RegistryParam;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.enums.RegistryConfig;
import com.xxl.job.core.executor.XxlJobExecutor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * Created by xuxueli on 17/3/2.
 */
@Slf4j
public class ExecutorRegistryThread extends Thread {
    @Getter
    private static ExecutorRegistryThread instance = new ExecutorRegistryThread();

    private Thread registryThread;
    private volatile boolean toStop = false;

    public void start(final String appName, final String address) {
        // valid
        if (Strings.isNullOrEmpty(appName)) {
            log.warn(">>>>>>>>>>> xxl-job, executor registry config fail, appName is null.");
            return;
        }
        if (null == XxlJobExecutor.getAdminBizList()) {
            log.warn(">>>>>>>>>>> xxl-job, executor registry config fail, adminAddresses is null.");
            return;
        }

        registryThread = new Thread(() -> {
            // registry
            while (!toStop) {
                try {
                    RegistryParam registryParam = new RegistryParam(RegistryConfig.RegistryTypeEnum.EXECUTOR.name(), appName, address);
                    for (AdminBiz adminBiz : XxlJobExecutor.getAdminBizList()) {
                        try {
                            ReturnT<String> registryResult = adminBiz.registry(registryParam);
                            if (null != registryResult && ReturnT.SUCCESS_CODE == registryResult.getCode()) {
                                registryResult = ReturnT.SUCCESS;
                                log.info(">>>>>>>>>>> xxl-job registry success, registryParam:{}, registryResult:{}", new Object[]{registryParam, registryResult});
                                break;
                            } else {
                                log.info(">>>>>>>>>>> xxl-job registry fail, registryParam:{}, registryResult:{}", new Object[]{registryParam, registryResult});
                            }
                        } catch (Exception e) {
                            log.info(">>>>>>>>>>> xxl-job registry error, registryParam:{}", registryParam, e);
                        }
                    }
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }

                try {
                    TimeUnit.SECONDS.sleep(RegistryConfig.BEAT_TIMEOUT);
                } catch (InterruptedException e) {
                    log.warn(">>>>>>>>>>> xxl-job, executor registry thread interrupted, error msg:{}", e.getMessage());
                }
            }

            // registry remove
            try {
                RegistryParam registryParam = new RegistryParam(RegistryConfig.RegistryTypeEnum.EXECUTOR.name(), appName, address);
                for (AdminBiz adminBiz : XxlJobExecutor.getAdminBizList()) {
                    try {
                        ReturnT<String> registryResult = adminBiz.registryRemove(registryParam);
                        if (null != registryResult && ReturnT.SUCCESS_CODE == registryResult.getCode()) {
                            registryResult = ReturnT.SUCCESS;
                            log.info(">>>>>>>>>>> xxl-job registry-remove success, registryParam:{}, registryResult:{}", new Object[]{registryParam, registryResult});
                            break;
                        } else {
                            log.info(">>>>>>>>>>> xxl-job registry-remove fail, registryParam:{}, registryResult:{}", new Object[]{registryParam, registryResult});
                        }
                    } catch (Exception e) {
                        log.info(">>>>>>>>>>> xxl-job registry-remove error, registryParam:{}", registryParam, e);
                    }
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
            log.info(">>>>>>>>>>> xxl-job, executor registry thread destory.");
        }, "REGISTRY_THREAD");

        registryThread.setDaemon(true);
        registryThread.start();
    }

    public void toStop() {
        toStop = true;
        if (null == registryThread) {
            return;
        }

        // interrupt and wait
        registryThread.interrupt();
        try {
            // 等待registryThread同步执行完registryRemove动作
            registryThread.join();
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }
    }
}