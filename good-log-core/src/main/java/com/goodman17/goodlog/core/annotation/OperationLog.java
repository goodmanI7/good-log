package com.goodman17.goodlog.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 操作日志记录注解
 * <p>
 * 支持在方法上重复声明多个操作日志记录
 * </p>
 *
 * @author goodman17
 * @since 1.0.0
 */
@Documented
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(OperationLogs.class)
@Inherited
public @interface OperationLog {

    /**
     * 业务唯一标识（必填）
     * 
     * @apiNote 支持SpEL表达式，例如：#{#order.id}
     */
    String bizId();

    /**
     * 业务类型（必填）
     * 
     * @apiNote 建议使用业务枚举值，如："ORDER", "PAYMENT"
     */
    String bizType();

    /**
     * 操作描述信息
     * 
     * @default 默认空字符串
     * @example "创建订单"
     */
    String msg() default "";

    /**
     * 操作标签（用于分类过滤）
     * 
     * @default 空数组
     * @example {"FINANCE", "NOTIFICATION"}
     */
    String[] tags() default {};

    /**
     * 扩展信息数组
     * 
     * @since 1.0.0
     */
    OperationLogExtra[] extras() default {};

    /**
     * 操作人ID
     * 
     * @default 自动从上下文中获取
     * @see com.goodman17.goodlog.core.context.OperationLogContext
     */
    String operator() default "";

    /**
     * 操作类型
     * 
     * @default 根据方法名自动推断
     * @example "CREATE", "UPDATE", "DELETE"
     */
    String operationType() default "";
}
