package com.xxl.job.core.thread;

import com.google.common.collect.Lists;
import com.xxl.job.core.biz.AdminBiz;
import com.xxl.job.core.biz.model.CallbackParam;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.enums.RegistryConfig;
import com.xxl.job.core.executor.XxlJobExecutor;
import com.xxl.job.core.log.XxlJobFileAppender;
import com.xxl.job.core.log.XxlJobLogger;
import com.xxl.job.core.util.FileUtil;
import com.xxl.job.core.util.JacksonUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by xuxueli on 16/7/22.
 */
@Slf4j
public class TriggerCallbackThread {
    @Getter
    private static TriggerCallbackThread instance = new TriggerCallbackThread();

    /**
     * job results callback queue
     */
    private LinkedBlockingQueue<CallbackParam> callBackQueue = new LinkedBlockingQueue<>();

    public static void pushCallBack(CallbackParam callback) {
        getInstance().callBackQueue.add(callback);
        log.debug(">>>>>>>>>>> xxl-job, push callback request, logId:{}", callback.getLogId());
    }

    /**
     * callback thread
     */
    private Thread triggerCallbackThread;
    private Thread triggerRetryCallbackThread;
    private volatile boolean toStop = false;

    public void start() {
        // valid
        if (XxlJobExecutor.getAdminBizList() == null) {
            log.warn(">>>>>>>>>>> xxl-job, executor callback config fail, adminAddresses is null.");
            return;
        }

        // callback
        triggerCallbackThread = new Thread(() -> {
            // normal callback
            while (!toStop) {
                try {
                    CallbackParam callback = getInstance().callBackQueue.take();
                    if (callback != null) {
                        // callback list param
                        List<CallbackParam> callbackParamList = Lists.newArrayList();
                        getInstance().callBackQueue.drainTo(callbackParamList);
                        callbackParamList.add(callback);

                        // callback, will retry if error
                        if (callbackParamList != null && !callbackParamList.isEmpty()) {
                            doCallback(callbackParamList);
                        }
                    }
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }

            // last callback
            try {
                List<CallbackParam> callbackParamList = Lists.newArrayList();
                getInstance().callBackQueue.drainTo(callbackParamList);
                if (callbackParamList != null && !callbackParamList.isEmpty()) {
                    doCallback(callbackParamList);
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
            log.info(">>>>>>>>>>> xxl-job, executor callback thread destroy.");

        });
        triggerCallbackThread.setDaemon(true);
        triggerCallbackThread.start();


        // retry
        triggerRetryCallbackThread = new Thread(() -> {
            while (!toStop) {
                try {
                    retryFailCallbackFile();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
                try {
                    TimeUnit.SECONDS.sleep(RegistryConfig.BEAT_TIMEOUT);
                } catch (InterruptedException e) {
                    log.warn(">>>>>>>>>>> xxl-job, executor retry callback thread interrupted, error msg:{}", e.getMessage());
                }
            }
            log.info(">>>>>>>>>>> xxl-job, executor retry callback thread destroy.");
        });
        triggerRetryCallbackThread.setDaemon(true);
        triggerRetryCallbackThread.start();

    }

    public void toStop() {
        toStop = true;
        // stop callback, interrupt and wait
        triggerCallbackThread.interrupt();
        try {
            triggerCallbackThread.join();
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }

        // stop retry, interrupt and wait
        triggerRetryCallbackThread.interrupt();
        try {
            triggerRetryCallbackThread.join();
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * do callback, will retry if error
     *
     * @param callbackParamList
     */
    private void doCallback(List<CallbackParam> callbackParamList) {
        boolean callbackRet = false;
        // callback, will retry if error
        for (AdminBiz adminBiz : XxlJobExecutor.getAdminBizList()) {
            try {
                ReturnT<String> callbackResult = adminBiz.callback(callbackParamList);
                if (callbackResult != null && ReturnT.SUCCESS_CODE == callbackResult.getCode()) {
                    callbackLog(callbackParamList, "<br>----------- xxl-job job callback finish.");
                    callbackRet = true;
                    break;
                } else {
                    callbackLog(callbackParamList, "<br>----------- xxl-job job callback fail, callbackResult:" + callbackResult);
                }
            } catch (Exception e) {
                callbackLog(callbackParamList, "<br>----------- xxl-job job callback error, errorMsg:" + e.getMessage());
            }
        }
        if (!callbackRet) {
            appendFailCallbackFile(callbackParamList);
        }
    }

    /**
     * callback log
     */
    private void callbackLog(List<CallbackParam> callbackParamList, String logContent) {
        for (CallbackParam callbackParam : callbackParamList) {
            String logFileName = XxlJobFileAppender.makeLogFileName(new Date(callbackParam.getLogDateTime()), callbackParam.getLogId());
            XxlJobFileAppender.CONTEXT_HOLDER.set(logFileName);
            XxlJobLogger.log(logContent);
        }
    }


    // ---------------------- fail-callback file ----------------------

    private static String failCallbackFileName = XxlJobFileAppender.getLogBasePath().concat(File.separator).concat("xxl-job-callback").concat(".log");

    private void appendFailCallbackFile(List<CallbackParam> callbackParamList) {
        // append file
        String content = JacksonUtil.writeValueAsString(callbackParamList);
        FileUtil.appendFileLine(failCallbackFileName, content);
    }

    private void retryFailCallbackFile() {
        // load and clear file
        List<String> fileLines = FileUtil.loadFileLines(failCallbackFileName);
        FileUtil.deleteFile(failCallbackFileName);

        // parse
        List<CallbackParam> failCallbackParamList = new ArrayList<>();
        if (fileLines != null && fileLines.size() > 0) {
            for (String line : fileLines) {
                List<CallbackParam> failCallbackParamListTmp = JacksonUtil.readValue(line, List.class, CallbackParam.class);
                if (failCallbackParamListTmp != null && failCallbackParamListTmp.size() > 0) {
                    failCallbackParamList.addAll(failCallbackParamListTmp);
                }
            }
        }

        // retry callback, 100 lines per page
        if (failCallbackParamList.size() > 0) {
            int pagesize = 100;
            List<CallbackParam> pageData = new ArrayList<>();
            for (int i = 0; i < failCallbackParamList.size(); i++) {
                pageData.add(failCallbackParamList.get(i));
                if (i > 0 && i % pagesize == 0) {
                    doCallback(pageData);
                    pageData.clear();
                }
            }
            if (pageData.size() > 0) {
                doCallback(pageData);
            }
        }
    }
}