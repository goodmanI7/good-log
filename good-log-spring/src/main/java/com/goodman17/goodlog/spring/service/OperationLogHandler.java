package com.goodman17.goodlog.spring.service;


import com.goodman17.goodlog.core.annotation.OperationLog;
import com.goodman17.goodlog.core.spel.SpelExpressionParser;

/**
 * 操作日志处理器接口
 * <p>
 * 该接口定义了处理操作日志的方法，实现该接口可以自定义日志处理逻辑。
 * 在方法执行完成后，会调用该接口的实现类来处理日志信息。
 * </p>
 *
 * @author goodman17
 */
public interface OperationLogHandler {

    /**
     * 处理操作日志
     *
     * @param operationLogs 方法上标注的操作日志注解数组
     * @param methodName    被调用的方法名称
     * @param args          方法的参数数组
     * @param returnValue   方法的返回值
     * @param parser        SpEL表达式解析器，用于解析日志表达式
     * @param e             方法执行过程中抛出的异常，如果没有异常则为null
     * @param timerCount    方法执行耗时(毫秒)
     */
    void handleLog(OperationLog[] operationLogs, String methodName, Object[] args, Object returnValue,
                   SpelExpressionParser parser, Exception e, long timerCount);
}
