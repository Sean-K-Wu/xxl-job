package com.xxl.job.core.glue;

import com.google.common.base.Strings;
import com.xxl.job.core.executor.XxlJobExecutor;
import com.xxl.job.core.handler.IJobHandler;
import groovy.lang.GroovyClassLoader;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;

import javax.annotation.Resource;
import javax.inject.Inject;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * glue factory, product class/object by name
 *
 * @author xuxueli 2016-1-2 20:02:27
 */
@Slf4j
public class GlueFactory {
    @Getter
    private static GlueFactory instance = new GlueFactory();

    /**
     * groovy class loader
     */
    private GroovyClassLoader groovyClassLoader = new GroovyClassLoader();

    /**
     * load instance
     *
     * @param codeSource
     * @return
     * @throws Exception
     */
    public IJobHandler loadNewInstance(String codeSource) throws Exception {
        if (!Strings.isNullOrEmpty(codeSource)) {
            Class<?> clazz = groovyClassLoader.parseClass(codeSource);
            if (null != clazz) {
                Object instance = clazz.newInstance();
                if (null != instance) {
                    if (instance instanceof IJobHandler) {
                        injectService(instance);
                        return (IJobHandler) instance;
                    } else {
                        throw new IllegalArgumentException(">>>>>>>>>>> xxl-glue, loadNewInstance error, "
                                + "cannot convert from instance[" + instance.getClass() + "] to IJobHandler");
                    }
                }
            }
        }
        throw new IllegalArgumentException(">>>>>>>>>>> xxl-glue, loadNewInstance error, instance is null");
    }

    /**
     * inject action of spring
     *
     * @param instance
     */
    private void injectService(Object instance) {
        if (null == instance) {
            return;
        }

        Field[] fields = instance.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }

            Object fieldBean = null;
            String fieldName = field.getName();
            Class<?> fieldType = field.getType();
            Qualifier qualifier = AnnotationUtils.getAnnotation(field, Qualifier.class);
            String qualifierValue = null == qualifier ? "" : qualifier.value();
            // with bean-id, bean could be found by both @Resource and @Autowired, or bean could only be found by @Autowired
            ApplicationContext applicationContext = XxlJobExecutor.getApplicationContext();
            if (null != AnnotationUtils.getAnnotation(field, Resource.class)) {
                // @Resource：JSR-250
                // CommonAnnotationBeanPostProcessor
                Resource resource = AnnotationUtils.getAnnotation(field, Resource.class);
                String resourceName = resource.name();
                if (!Strings.isNullOrEmpty(resourceName)) {
                    // 1.1. Resource Name
                    try {
                        fieldBean = applicationContext.getBean(resourceName);
                    } catch (Exception e) {
                        log.warn(e.getMessage(), e);
                    }
                }
                if (null == fieldBean && !Strings.isNullOrEmpty(fieldName)) {
                    // 1.2. FieldName Name
                    try {
                        fieldBean = applicationContext.getBean(fieldName);
                    } catch (Exception e) {
                        log.warn(e.getMessage(), e);
                    }
                }
                if (null == fieldBean) {
                    // 2. Type
                    try {
                        fieldBean = applicationContext.getBean(fieldType);
                    } catch (Exception e) {
                        log.warn(e.getMessage(), e);
                    }
                }
                if (null == fieldBean && !Strings.isNullOrEmpty(qualifierValue)) {
                    // 3. Qualifier
                    try {
                        fieldBean = applicationContext.getBean(qualifierValue);
                    } catch (Exception e) {
                        log.warn(e.getMessage(), e);
                    }
                }
            } else if (null != AnnotationUtils.getAnnotation(field, Autowired.class) ||
                    null != AnnotationUtils.getAnnotation(field, Inject.class)) {
                // @Autowired：Spring + @Inject：JSR-330
                // AutowiredAnnotationBeanPostProcessor
                // 1. Type
                try {
                    fieldBean = applicationContext.getBean(fieldType);
                } catch (Exception e) {
                    log.warn(e.getMessage(), e);
                }
                if (null == fieldBean && !Strings.isNullOrEmpty(qualifierValue)) {
                    // 2. Qualifier
                    try {
                        fieldBean = applicationContext.getBean(qualifierValue);
                    } catch (Exception e) {
                        log.warn(e.getMessage(), e);
                    }
                }
                if (null == fieldBean && !Strings.isNullOrEmpty(fieldName)) {
                    // 3. Name
                    try {
                        fieldBean = applicationContext.getBean(fieldName);
                    } catch (Exception e) {
                        log.warn(e.getMessage(), e);
                    }
                }
            }

            if (null != fieldBean) {
                field.setAccessible(true);
                try {
                    field.set(instance, fieldBean);
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }
}