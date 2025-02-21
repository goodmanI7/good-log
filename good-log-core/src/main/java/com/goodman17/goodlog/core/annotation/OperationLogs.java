package com.goodman17.goodlog.core.annotation;

import java.lang.annotation.*;

@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface OperationLogs {

    /**
     * 操作日志注解数组
     *
     * @return 返回多个操作日志配置
     */
    OperationLog[] value();
}
