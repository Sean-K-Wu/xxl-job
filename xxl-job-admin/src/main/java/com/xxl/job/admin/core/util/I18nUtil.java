package com.xxl.job.admin.core.util;

import com.google.common.collect.Maps;
import com.xxl.job.admin.core.conf.XxlJobAdminConfig;
import com.xxl.job.core.util.JacksonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Properties;

/**
 * i18n util
 *
 * @author xuxueli 2018-01-17 20:39:06
 */
@Slf4j
public class I18nUtil {
    private static Properties prop = null;

    private static Properties loadI18nProp() {
        if (null != prop) {
            return prop;
        }
        try {
            // build i18n prop
            String i18n = XxlJobAdminConfig.getAdminConfig().getI18n();
            i18n = StringUtils.isNotBlank(i18n) ? ("_" + i18n) : i18n;
            String i18nFile = MessageFormat.format("i18n/message{0}.properties", i18n);

            // load prop
            Resource resource = new ClassPathResource(i18nFile);
            EncodedResource encodedResource = new EncodedResource(resource, "UTF-8");
            prop = PropertiesLoaderUtils.loadProperties(encodedResource);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return prop;
    }

    /**
     * get val of i18n key
     *
     * @param key
     * @return
     */
    public static String getString(String key) {
        return loadI18nProp().getProperty(key);
    }

    /**
     * get mult val of i18n multi key, as json
     *
     * @param keys
     * @return
     */
    public static String getMultiString(String... keys) {
        Map<String, String> map = Maps.newHashMap();

        Properties prop = loadI18nProp();
        if (null != keys && keys.length > 0) {
            for (String key : keys) {
                map.put(key, prop.getProperty(key));
            }
        } else {
            for (String key : prop.stringPropertyNames()) {
                map.put(key, prop.getProperty(key));
            }
        }

        return JacksonUtil.writeValueAsString(map);
    }
}