package com.xxl.job.admin.core.conf;

import com.xxl.job.admin.dao.XxlJobGroupDao;
import com.xxl.job.admin.dao.XxlJobInfoDao;
import com.xxl.job.admin.dao.XxlJobLogDao;
import com.xxl.job.admin.dao.XxlJobRegistryDao;
import com.xxl.job.core.biz.AdminBiz;
import lombok.Data;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * xxl-job config
 *
 * @author xuxueli 2017-04-28
 */
@Data
@Configuration
public class XxlJobAdminConfig implements InitializingBean {
    private static XxlJobAdminConfig adminConfig = null;

    public static XxlJobAdminConfig getAdminConfig() {
        return adminConfig;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        adminConfig = this;
    }

    // conf

    @Value("${xxl.job.mail.host}")
    private String mailHost;

    @Value("${xxl.job.mail.port}")
    private String mailPort;

    @Value("${xxl.job.mail.ssl}")
    private boolean mailSSL;

    @Value("${xxl.job.mail.username}")
    private String mailUsername;

    @Value("${xxl.job.mail.password}")
    private String mailPassword;

    @Value("${xxl.job.mail.sendNick}")
    private String mailSendNick;

    @Value("${xxl.job.login.username}")
    private String loginUsername;

    @Value("${xxl.job.login.password}")
    private String loginPassword;

    @Value("${xxl.job.i18n}")
    private String i18n;

    @Value("${xxl.job.accessToken}")
    private String accessToken;

    // dao, service

    @Resource
    private XxlJobLogDao xxlJobLogDao;
    @Resource
    private XxlJobInfoDao xxlJobInfoDao;
    @Resource
    private XxlJobRegistryDao xxlJobRegistryDao;
    @Resource
    private XxlJobGroupDao xxlJobGroupDao;
    @Resource
    private AdminBiz adminBiz;
}