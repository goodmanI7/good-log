package com.goodman17.goodlog.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 操作日志扩展信息注解
 * <p>
 * 用于在方法上声明需要记录到日志的额外键值对信息，
 * 通常与{@link OperationLog}或{@link OperationLogs}配合使用。
 * 支持在单个方法上重复声明多个扩展字段。
 * </p>
 *
 * @example
 * 
 *          <pre>
 * {@code
 * &#64;OperationLogExtra(key = "department", value = "IT")
 * @OperationLogExtra(key = "priority", value = "high")
 * public void someOperation() {
 *     // ...
 * }
 * }
 *          </pre>
 */
@Documented
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface OperationLogExtra {

    /**
     * 扩展信息的键
     * <p>
     * 需保证在同一方法上的多个扩展注解中保持唯一
     * </p>
     * 
     * @return 键名称，建议使用小写字母和下划线的命名方式（如：module_name）
     */
    String key();

    /**
     * 扩展信息的值
     * <p>
     * 支持以下类型：
     * </p>
     * <ul>
     * <li>直接字符串值</li>
     * <li>SpEL表达式（需配合#{...}语法使用）</li>
     * <li>方法参数引用（需配合#paramName语法使用）</li>
     * </ul>
     * 
     * @return 对应的值内容
     * @example
     * 
     *          <pre>
     * // 直接值
     * &#64;OperationLogExtra(key = "env", value = "production")
     * 
     * // SpEL表达式
     * &#64;OperationLogExtra(key = "result", value = "#{#result.status")
     * 
     * // 参数引用
     * @OperationLogExtra(key = "userId", value = "#{#userId}")
     *          </pre>
     */
    String value();
}
