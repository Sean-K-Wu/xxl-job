package com.xxl.job.core.thread;

import com.xxl.job.core.log.XxlJobFileAppender;
import com.xxl.job.core.util.FileUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * job file clean thread
 *
 * @author xuxueli 2017-12-29 16:23:43
 */
@Slf4j
public class JobLogFileCleanThread extends Thread {
    private static final int MIN_RETAIN_DAYS = 3;
    private static final long MILLISECOND_OF_ONE_DAY = 24 * 60 * 60 * 1000L;

    @Getter
    private static JobLogFileCleanThread instance = new JobLogFileCleanThread();

    private Thread cleanLogFileThread;
    private volatile boolean toStop = false;

    public void start(final long logRetentionDays) {
        // limit min value
        if (logRetentionDays < MIN_RETAIN_DAYS) {
            return;
        }

        // yyyy-MM-dd/9999.log
        cleanLogFileThread = new Thread(() -> {
            while (!toStop) {
                try {
                    // clean log dir, over logRetentionDays
                    File[] childDirs = new File(XxlJobFileAppender.getLogBasePath()).listFiles();
                    if (null != childDirs && childDirs.length > 0) {
                        // today
                        Calendar todayCal = Calendar.getInstance();
                        todayCal.set(Calendar.HOUR_OF_DAY, 0);
                        todayCal.set(Calendar.MINUTE, 0);
                        todayCal.set(Calendar.SECOND, 0);
                        todayCal.set(Calendar.MILLISECOND, 0);
                        Date todayDate = todayCal.getTime();

                        for (File childFile : childDirs) {
                            // 非目录直接跳过
                            if (!childFile.isDirectory()) {
                                continue;
                            }
                            // 不是任务执行日志跳过，例如gluesource
                            if (childFile.getName().indexOf("-") == -1) {
                                continue;
                            }

                            // file create date
                            Date logFileCreateDate = null;
                            try {
                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                                logFileCreateDate = simpleDateFormat.parse(childFile.getName());
                            } catch (ParseException e) {
                                log.error(e.getMessage(), e);
                            }
                            if (null == logFileCreateDate) {
                                continue;
                            }

                            if ((todayDate.getTime() - logFileCreateDate.getTime()) >= logRetentionDays * MILLISECOND_OF_ONE_DAY) {
                                FileUtil.deleteRecursively(childFile);
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
                try {
                    TimeUnit.DAYS.sleep(1);
                } catch (InterruptedException e) {
                    log.error(e.getMessage(), e);
                }
            }
            log.info(">>>>>>>>>>> xxl-job, executor JobLogFileCleanThread thread destory.");

        }, "CLEAN_LOG_FILE_THREAD");

        cleanLogFileThread.setDaemon(true);
        cleanLogFileThread.start();
    }

    public void toStop() {
        toStop = true;
        if (null == cleanLogFileThread) {
            return;
        }

        // interrupt and wait
        cleanLogFileThread.interrupt();
        try {
            cleanLogFileThread.join();
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }
    }
}