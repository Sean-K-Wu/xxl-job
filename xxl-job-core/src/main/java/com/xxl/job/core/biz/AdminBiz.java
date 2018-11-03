package com.xxl.job.core.biz;

import com.xxl.job.core.biz.model.CallbackParam;
import com.xxl.job.core.biz.model.RegistryParam;
import com.xxl.job.core.biz.model.ReturnT;

import java.util.List;

/**
 * @author xuxueli 2017-07-27 21:52:49
 */
public interface AdminBiz {

    String MAPPING = "/api";

    /**
     * 回调
     *
     * @param callbackParamList
     * @return
     */
    ReturnT<String> callback(List<CallbackParam> callbackParamList);

    /**
     * 注册
     *
     * @param registryParam
     * @return
     */
    ReturnT<String> registry(RegistryParam registryParam);

    /**
     * 注册移除
     *
     * @param registryParam
     * @return
     */
    ReturnT<String> registryRemove(RegistryParam registryParam);
}