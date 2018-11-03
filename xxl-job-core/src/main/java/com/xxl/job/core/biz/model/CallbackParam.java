package com.xxl.job.core.biz.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Created by xuxueli on 17/3/2.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CallbackParam implements Serializable {
    private static final long serialVersionUID = 42L;

    private int logId;
    private long logDateTime;
    private ReturnT<String> executeResult;
}