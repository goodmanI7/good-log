package com.goodman17.goodlog.spring.aop;

import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.expression.AnnotatedElementKey;
import org.springframework.expression.EvaluationContext;
import org.springframework.stereotype.Component;

import com.goodman17.goodlog.core.annotation.OperationLog;
import com.goodman17.goodlog.core.context.OperationLogContext;
import com.goodman17.goodlog.core.spel.OperationLogExpressionEvaluator;
import com.goodman17.goodlog.core.spel.SpelExpressionParser;
import com.goodman17.goodlog.spring.service.OperationLogHandler;

/**
 * 操作日志切面处理类
 * <p>
 * 功能说明：
 * 1. 拦截带有@OperationLog或@OperationLogs注解的方法
 * 2. 记录方法执行时间、参数、返回值和异常信息
 * 3. 支持SpEL表达式解析
 * </p>
 */
@Aspect
@Component
public class OperationLogAspect {

    private static final Logger log = LoggerFactory.getLogger(OperationLogAspect.class);

    // SpEL表达式解析器（线程安全）
    private final OperationLogExpressionEvaluator expressionEvaluator = new OperationLogExpressionEvaluator();

    private final OperationLogHandler operationLogHandler;

    public OperationLogAspect(OperationLogHandler operationLogHandler) {
        this.operationLogHandler = operationLogHandler;
    }

    /**
     * 环绕通知处理方法
     * 
     * @param pjp 连接点对象
     * @return 方法执行结果
     * @throws Throwable 可能抛出的异常
     */
    @Around("@annotation(com.goodman17.goodlog.core.annotation.OperationLog) || @annotation(com.goodman17.goodlog.core.annotation.OperationLogs)")
    public Object doAround(ProceedingJoinPoint pjp) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object returnValue = null;
        Exception exception = null;

        try {
            returnValue = pjp.proceed();
        } catch (Exception e) {
            exception = e;
            throw e;
        } finally {
            // 计算方法执行耗时（毫秒）
            long executionTimeMs = System.currentTimeMillis() - startTime;
            try {
                // 处理日志记录（确保在finally中执行）
                handleLog(pjp, exception, executionTimeMs, returnValue);
            } catch (Exception ex) {
                // 日志记录失败不影响主流程
                log.error("操作日志记录过程中发生异常", ex);
            }
        }
        return returnValue;
    }

    /**
     * 处理日志记录的核心方法
     * 
     * @param point           连接点对象
     * @param exception       可能存在的异常对象
     * @param executionTimeMs 方法执行时间（毫秒）
     * @param returnValue     方法返回值
     */
    private void handleLog(ProceedingJoinPoint point, Exception exception, long executionTimeMs, Object returnValue) {
        try {
            // 获取方法元数据
            MethodSignature signature = (MethodSignature) point.getSignature();
            Method method = signature.getMethod();
            Class<?> targetClass = point.getTarget().getClass();

            // 构建表达式解析上下文
            EvaluationContext evaluationContext = expressionEvaluator.createEvaluationContext(
                    method,
                    point.getArgs(),
                    point.getTarget(),
                    targetClass,
                    returnValue,
                    exception != null ? exception.getMessage() : null,
                    null);

            // 解析方法上的操作日志注解（支持多个注解）
            OperationLog[] operationLogs = method.getAnnotationsByType(OperationLog.class);

            // 创建SpEL解析器
            SpelExpressionParser spelParser = new SpelExpressionParser(
                    expressionEvaluator,
                    new AnnotatedElementKey(method, targetClass),
                    evaluationContext);

            // 调用日志处理器
            operationLogHandler.handleLog(
                    operationLogs,
                    method.getName(),
                    point.getArgs(),
                    isVoidReturnType(method) ? null : returnValue,
                    spelParser,
                    exception,
                    executionTimeMs);
        } catch (Exception ex) {
            log.error("处理操作日志时发生异常", ex);
        } finally {
            // 确保清除线程上下文数据（防止内存泄漏）
            OperationLogContext.clear();
        }
    }

    /**
     * 判断方法返回值是否为void类型
     * 
     * @param method 目标方法
     * @return true表示void返回类型，false表示有返回值
     */
    private boolean isVoidReturnType(Method method) {
        Class<?> returnType = method.getReturnType();
        return returnType == void.class || returnType == Void.class;
    }
}
