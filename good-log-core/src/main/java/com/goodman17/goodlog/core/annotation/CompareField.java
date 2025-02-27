package com.goodman17.goodlog.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 比较字段
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface CompareField {

    /**
     * 字段的别名
     * 
     * @return 别名
     */
    String alias();

    /**
     * 是否忽略比较
     * 
     * @return 是否忽略比较
     */
    boolean ignore() default false;

    /**
     * 比较方法
     * 
     * @return 比较方法
     */
    String diffMethod() default "";
}