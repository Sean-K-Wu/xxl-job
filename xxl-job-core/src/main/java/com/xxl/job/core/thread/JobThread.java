package com.xxl.job.core.thread;

import com.xxl.job.core.biz.model.CallbackParam;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.biz.model.TriggerParam;
import com.xxl.job.core.executor.XxlJobExecutor;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.log.XxlJobFileAppender;
import com.xxl.job.core.log.XxlJobLogger;
import com.xxl.job.core.util.ShardingUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;


/**
 * handler thread
 *
 * @author xuxueli 2016-1-16 19:52:47
 */
@Slf4j
public class JobThread extends Thread {

    private int jobId;
    private IJobHandler handler;
    private LinkedBlockingQueue<TriggerParam> triggerQueue;
    /**
     * avoid repeat trigger for the same TRIGGER_LOG_ID
     */
    private Set<Integer> triggerLogIdSet;

    private volatile boolean toStop = false;
    private String stopReason;

    /**
     * if running job
     */
    private boolean running = false;
    /**
     * idle times
     */
    private int idleTimes = 0;

    public JobThread(int jobId, IJobHandler handler) {
        this.jobId = jobId;
        this.handler = handler;
        this.triggerQueue = new LinkedBlockingQueue<>();
        this.triggerLogIdSet = Collections.synchronizedSet(new HashSet<>());
    }

    public IJobHandler getHandler() {
        return handler;
    }

    /**
     * new trigger to queue
     *
     * @param triggerParam
     * @return
     */
    public ReturnT<String> pushTriggerQueue(TriggerParam triggerParam) {
        // avoid repeat
        int logId = triggerParam.getLogId();
        if (triggerLogIdSet.contains(logId)) {
            log.info(">>>>>>>>>>> repeat trigger job, logId:{}", logId);
            return new ReturnT<>(ReturnT.FAIL_CODE, "repeat trigger job, logId:" + logId);
        }

        triggerLogIdSet.add(logId);
        triggerQueue.add(triggerParam);
        return ReturnT.SUCCESS;
    }

    /**
     * kill job thread
     *
     * @param stopReason
     */
    public void toStop(String stopReason) {
        // Thread.interrupt只支持终止线程的阻塞状态(wait、join、sleep)，
        // 在阻塞出抛出InterruptedException异常, 但是并不会终止运行的线程本身；
        // 所以需要注意，此处彻底销毁本线程，需要通过共享变量方式；
        this.toStop = true;
        this.stopReason = stopReason;
    }

    /**
     * is running job
     *
     * @return
     */
    public boolean isRunningOrHasQueue() {
        return running || !triggerQueue.isEmpty();
    }

    @Override
    public void run() {
        // init
        try {
            handler.init();
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
        }

        // execute
        while (!toStop) {
            running = false;
            idleTimes++;

            TriggerParam triggerParam = null;
            ReturnT<String> executeResult = null;
            try {
                // to check toStop signal, we need cycle, so wo cannot use queue.take(), instead of poll(timeout)
                triggerParam = triggerQueue.poll(3L, TimeUnit.SECONDS);
                if (null != triggerParam) {
                    running = true;
                    idleTimes = 0;
                    triggerLogIdSet.remove(triggerParam.getLogId());

                    // log filename, like "logPath/yyyy-MM-dd/9999.log"
                    String logFileName = XxlJobFileAppender.makeLogFileName(new Date(triggerParam.getLogDateTime()), triggerParam.getLogId());
                    XxlJobFileAppender.CONTEXT_HOLDER.set(logFileName);
                    ShardingUtil.setShardingVo(new ShardingUtil.ShardingVO(triggerParam.getBroadcastIndex(), triggerParam.getBroadcastTotal()));

                    // execute
                    XxlJobLogger.log("<br>----------- xxl-job job execute start -----------<br>----------- Param:" + triggerParam.getExecutorParams());

                    if (triggerParam.getExecutorTimeout() > 0) {
                        // limit timeout
                        Thread futureThread = null;
                        try {
                            final TriggerParam triggerParamTmp = triggerParam;
                            FutureTask<ReturnT<String>> futureTask = new FutureTask<>(() ->
                                    handler.execute(triggerParamTmp.getExecutorParams()));
                            futureThread = new Thread(futureTask);
                            futureThread.start();
                            executeResult = futureTask.get(triggerParam.getExecutorTimeout(), TimeUnit.SECONDS);
                        } catch (TimeoutException e) {
                            XxlJobLogger.log("<br>----------- xxl-job job execute timeout");
                            XxlJobLogger.log(e);
                            executeResult = new ReturnT<>(IJobHandler.FAIL_TIMEOUT.getCode(), "job execute timeout ");
                        } finally {
                            if (futureThread != null) {
                                futureThread.interrupt();
                            }
                        }
                    } else {
                        // just execute
                        executeResult = handler.execute(triggerParam.getExecutorParams());
                    }

                    if (null == executeResult) {
                        executeResult = IJobHandler.FAIL;
                    }
                    XxlJobLogger.log("<br>----------- xxl-job job execute end(finish) -----------<br>----------- ReturnT:" + executeResult);
                } else {
                    if (idleTimes > 30) {
                        XxlJobExecutor.removeJobThread(jobId, "executor idle times over limit.");
                    }
                }
            } catch (Throwable e) {
                if (toStop) {
                    XxlJobLogger.log("<br>----------- JobThread toStop, stopReason:" + stopReason);
                }
                StringWriter stringWriter = new StringWriter();
                e.printStackTrace(new PrintWriter(stringWriter));
                String errorMsg = stringWriter.toString();
                executeResult = new ReturnT<>(ReturnT.FAIL_CODE, errorMsg);
                XxlJobLogger.log("<br>----------- JobThread Exception:" + errorMsg + "<br>----------- xxl-job job execute end(error) -----------");
            } finally {
                if (triggerParam != null) {
                    // callback handler info
                    if (!toStop) {
                        // common
                        TriggerCallbackThread.pushCallBack(new CallbackParam(triggerParam.getLogId(), triggerParam.getLogDateTime(), executeResult));
                    } else {
                        // killed
                        ReturnT<String> stopResult = new ReturnT<>(ReturnT.FAIL_CODE, stopReason + " [job running，killed]");
                        TriggerCallbackThread.pushCallBack(new CallbackParam(triggerParam.getLogId(), triggerParam.getLogDateTime(), stopResult));
                    }
                }
            }
        }

        // callback trigger request in queue
        while (triggerQueue != null && !triggerQueue.isEmpty()) {
            TriggerParam triggerParam = triggerQueue.poll();
            if (triggerParam != null) {
                // is killed
                ReturnT<String> stopResult = new ReturnT<>(ReturnT.FAIL_CODE, stopReason + " [job not executed, in the job queue, killed.]");
                TriggerCallbackThread.pushCallBack(new CallbackParam(triggerParam.getLogId(), triggerParam.getLogDateTime(), stopResult));
            }
        }

        // destroy
        try {
            handler.destroy();
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
        }

        log.info(">>>>>>>>>>> xxl-job JobThread stopped, hashCode:{}", Thread.currentThread());
    }
}