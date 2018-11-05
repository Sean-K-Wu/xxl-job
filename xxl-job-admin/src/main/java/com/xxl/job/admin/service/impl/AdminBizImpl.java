package com.xxl.job.admin.service.impl;

import com.xxl.job.admin.core.model.XxlJobInfo;
import com.xxl.job.admin.core.model.XxlJobLog;
import com.xxl.job.admin.core.thread.JobTriggerPoolHelper;
import com.xxl.job.admin.core.trigger.TriggerTypeEnum;
import com.xxl.job.admin.core.util.I18nUtil;
import com.xxl.job.admin.dao.XxlJobInfoDao;
import com.xxl.job.admin.dao.XxlJobLogDao;
import com.xxl.job.admin.dao.XxlJobRegistryDao;
import com.xxl.job.core.biz.AdminBiz;
import com.xxl.job.core.biz.model.CallbackParam;
import com.xxl.job.core.biz.model.RegistryParam;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;

/**
 * @author xuxueli 2017-07-27 21:54:20
 */
@Slf4j
@Service
public class AdminBizImpl implements AdminBiz {

    @Resource
    public XxlJobLogDao xxlJobLogDao;
    @Resource
    private XxlJobInfoDao xxlJobInfoDao;
    @Resource
    private XxlJobRegistryDao xxlJobRegistryDao;

    @Override
    public ReturnT<String> callback(List<CallbackParam> callbackParamList) {
        callbackParamList.forEach(callbackParam -> {
            ReturnT<String> callbackResult = callback(callbackParam);
            log.info(">>>>>>>>> JobApiController.callback {}, callbackParam={}, callbackResult={}",
                    (callbackResult.getCode() == IJobHandler.SUCCESS.getCode() ? "success" : "fail"), callbackParam, callbackResult);
        });

        return ReturnT.SUCCESS;
    }

    private ReturnT<String> callback(CallbackParam callbackParam) {
        // valid log item
        XxlJobLog log = xxlJobLogDao.load(callbackParam.getLogId());
        if (null == log) {
            return new ReturnT<>(ReturnT.FAIL_CODE, "log item not found.");
        }
        if (log.getHandleCode() > 0) {
            // avoid repeat callback, trigger child job etc
            return new ReturnT<>(ReturnT.FAIL_CODE, "log repeat callback.");
        }

        StringBuilder callbackMsg = null;
        if (IJobHandler.SUCCESS.getCode() == callbackParam.getExecuteResult().getCode()) {
            // trigger success, to trigger child job
            XxlJobInfo xxlJobInfo = xxlJobInfoDao.loadById(log.getJobId());
            if (null != xxlJobInfo && StringUtils.isNotBlank(xxlJobInfo.getChildJobId())) {
                callbackMsg = new StringBuilder("<br><br><span style=\"color:#00c0ef;\" > >>>>>>>>>>>" +
                        I18nUtil.getString("jobconf_trigger_child_run") + "<<<<<<<<<<< </span><br>");

                String[] childJobIds = xxlJobInfo.getChildJobId().split(",");
                for (int i = 0; i < childJobIds.length; i++) {
                    int childJobId = (StringUtils.isNotBlank(childJobIds[i]) && StringUtils.isNumeric(childJobIds[i])) ?
                            Integer.valueOf(childJobIds[i]) : -1;

                    if (childJobId > 0) {
                        JobTriggerPoolHelper.trigger(childJobId, TriggerTypeEnum.PARENT, 0, null, null);
                        ReturnT<String> triggerChildResult = ReturnT.SUCCESS;

                        // add msg
                        callbackMsg.append(MessageFormat.format(I18nUtil.getString("jobconf_callback_child_msg1"),
                                (i + 1),
                                childJobIds.length,
                                childJobIds[i],
                                (triggerChildResult.getCode() == ReturnT.SUCCESS_CODE ? I18nUtil.getString("system_success") : I18nUtil.getString("system_fail")),
                                triggerChildResult.getMsg()));
                    } else {
                        callbackMsg.append(MessageFormat.format(I18nUtil.getString("jobconf_callback_child_msg2"),
                                (i + 1),
                                childJobIds.length,
                                childJobIds[i]));
                    }
                }
            }
        }

        // handle msg
        StringBuilder sb = new StringBuilder();
        if (null != log.getHandleMsg()) {
            sb.append(log.getHandleMsg()).append("<br>");
        }
        if (null != callbackParam.getExecuteResult().getMsg()) {
            sb.append(callbackParam.getExecuteResult().getMsg());
        }
        if (null != callbackMsg) {
            sb.append(callbackMsg);
        }

        // success, save log
        log.setHandleTime(new Date());
        log.setHandleCode(callbackParam.getExecuteResult().getCode());
        log.setHandleMsg(sb.toString());
        xxlJobLogDao.updateHandleInfo(log);

        return ReturnT.SUCCESS;
    }

    @Override
    public ReturnT<String> registry(RegistryParam registryParam) {
        String registryGroup = registryParam.getRegistryGroup();
        String registryKey = registryParam.getRegistryKey();
        String registryValue = registryParam.getRegistryValue();
        int updateRecords = xxlJobRegistryDao.registryUpdate(registryGroup, registryKey, registryValue);
        log.info(">>>>>>>>> registry, registryGroup={}, registryKey={}, registryValue={}, updateRecords={}",
                registryGroup, registryKey, registryValue, updateRecords);
        if (updateRecords < 1) {
            xxlJobRegistryDao.registrySave(registryGroup, registryKey, registryValue);
        }
        return ReturnT.SUCCESS;
    }

    @Override
    public ReturnT<String> registryRemove(RegistryParam registryParam) {
        String registryGroup = registryParam.getRegistryGroup();
        String registryKey = registryParam.getRegistryKey();
        String registryValue = registryParam.getRegistryValue();
        xxlJobRegistryDao.registryDelete(registryGroup, registryKey, registryValue);
        log.info(">>>>>>>>> registryRemove, registryGroup={}, registryKey={}, registryValue={}",
                registryGroup, registryKey, registryValue);
        return ReturnT.SUCCESS;
    }
}