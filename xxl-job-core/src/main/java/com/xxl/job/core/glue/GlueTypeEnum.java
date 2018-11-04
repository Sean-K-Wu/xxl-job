package com.xxl.job.core.glue;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by xuxueli on 17/4/26.
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum GlueTypeEnum {
    /**
     * 容器模式，如Spring/Guice
     */
    BEAN("BEAN", false, null, null),
    GLUE_GROOVY("GLUE(Java)", false, null, null),
    GLUE_SHELL("GLUE(Shell)", true, "bash", ".sh"),
    GLUE_PYTHON("GLUE(Python)", true, "python", ".py"),
    GLUE_PHP("GLUE(PHP)", true, "php", ".php"),
    GLUE_NODE_JS("GLUE(NodeJs)", true, "node", ".js"),
    GLUE_POWER_SHELL("GLUE(PowerShell)", true, "powershell ", ".ps1");

    private String desc;
    private boolean isScript;
    private String cmd;
    private String suffix;

    public static GlueTypeEnum match(String name) {
        for (GlueTypeEnum glueTypeEnum : GlueTypeEnum.values()) {
            if (glueTypeEnum.name().equals(name)) {
                return glueTypeEnum;
            }
        }
        return null;
    }
}