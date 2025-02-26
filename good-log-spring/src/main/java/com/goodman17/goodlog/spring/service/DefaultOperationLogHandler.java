package com.goodman17.goodlog.spring.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.goodman17.goodlog.core.annotation.OperationLog;
import com.goodman17.goodlog.core.annotation.OperationLogExtra;
import com.goodman17.goodlog.core.context.OperationLogContext;
import com.goodman17.goodlog.core.spel.SpelExpressionParser;
import com.goodman17.goodlog.core.utils.SpelParseUtils;
import com.goodman17.goodlog.spring.utils.JsonUtils;

/**
 * 默认操作日志处理器
 * <p>
 * 负责将注解中定义的操作日志信息进行解析和记录
 * 支持多个操作日志的并行处理，提高日志处理效率
 * </p>
 * 
 * @author goodman17
 */
public class DefaultOperationLogHandler implements OperationLogHandler {

    private static final Logger log = LoggerFactory.getLogger(DefaultOperationLogHandler.class);

    /** 日志内容最大长度限制 */
    private static final int MAX_LOG_LENGTH = 500;

    /** 用于异步处理日志的线程池 */
    private final ExecutorService logExecutor = Executors.newFixedThreadPool(5);

    /**
     * 处理操作日志
     * 
     * @param operationLogs 操作日志注解数组
     * @param methodName    方法名称
     * @param args          方法参数
     * @param returnValue   方法返回值
     * @param parser        SpEL表达式解析器
     * @param e             异常信息（如果有）
     * @param timerCount    方法执行耗时（毫秒）
     */
    @Override
    public void handleLog(OperationLog[] operationLogs, String methodName, Object[] args, Object returnValue,
            SpelExpressionParser parser, Exception e, long timerCount) {
        if (operationLogs == null || operationLogs.length == 0) {
            return;
        }

        // 使用CompletableFuture并行处理多个日志记录，提高性能
        List<CompletableFuture<Void>> futures = Arrays.stream(operationLogs)
                .filter(Objects::nonNull)
                .map(operationLog -> CompletableFuture.runAsync(() -> {
                    try {
                        handleSingleLog(operationLog, methodName, args, returnValue, parser, e, timerCount);
                    } catch (Exception ex) {
                        log.error("[操作日志] 记录失败 | 方法: {} | 参数: {} | 错误: {}",
                                methodName, JsonUtils.safeJson(args), ex.getMessage(), ex);
                    }
                }, logExecutor))
                .collect(Collectors.toList());

        // 等待所有日志处理完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    /**
     * 处理单个操作日志
     * 
     * @param operationLog 操作日志注解
     * @param methodName   方法名称
     * @param args         方法参数
     * @param returnValue  方法返回值
     * @param parser       SpEL表达式解析器
     * @param e            异常信息（如果有）
     * @param timerCount   方法执行耗时（毫秒）
     */
    private void handleSingleLog(OperationLog operationLog, String methodName, Object[] args, Object returnValue,
            SpelExpressionParser parser, Exception e, long timerCount) {
        // 使用StringBuilder构建完整日志消息，减少多次日志调用
        StringBuilder logMessage = new StringBuilder(1024);
        logMessage.append("\n================================\n");
        logMessage.append("[操作日志] 方法: ").append(methodName).append(" | 耗时: ").append(timerCount).append("ms\n");

        // 记录请求上下文信息
        appendContextInfo(logMessage);

        // 记录业务信息
        appendBusinessInfo(logMessage, operationLog, parser);

        // 记录标签信息
        appendTags(logMessage, operationLog, parser);

        // 记录操作信息
        appendOperationInfo(logMessage, operationLog, parser);

        // 记录错误信息
        if (e != null) {
            logMessage.append("错误: ").append(e.getMessage()).append("\n");
        }

        // 记录返回值
        if (returnValue != null) {
            logMessage.append("返回值: ").append(JsonUtils.safeJson(returnValue)).append("\n");
        }

        // 记录扩展信息
        appendExtras(logMessage, operationLog, parser);

        logMessage.append("================================");

        // 一次性输出完整日志
        log.info(logMessage.toString());
    }

    /**
     * 添加请求上下文信息到日志
     * 
     * @param logMessage 日志消息构建器
     */
    private void appendContextInfo(StringBuilder logMessage) {
        String requestId = OperationLogContext.getRequestId();
        if (requestId != null) {
            logMessage.append("请求ID: ").append(requestId).append("\n");
        }

        String ip = OperationLogContext.getIp();
        if (ip != null) {
            logMessage.append("请求IP: ").append(ip).append("\n");
        }
    }

    /**
     * 添加业务信息到日志
     * 
     * @param logMessage   日志消息构建器
     * @param operationLog 操作日志注解
     * @param parser       SpEL表达式解析器
     */
    private void appendBusinessInfo(StringBuilder logMessage, OperationLog operationLog, SpelExpressionParser parser) {
        String bizId = SpelParseUtils.parseExpressionSafely(parser, operationLog.bizId());
        if (bizId != null) {
            logMessage.append("业务ID: ").append(bizId).append("\n");
        }

        String bizType = SpelParseUtils.parseExpressionSafely(parser, operationLog.bizType());
        if (bizType != null) {
            logMessage.append("业务类型: ").append(bizType).append("\n");
        }
    }

    /**
     * 添加标签信息到日志
     * 
     * @param logMessage   日志消息构建器
     * @param operationLog 操作日志注解
     * @param parser       SpEL表达式解析器
     */
    private void appendTags(StringBuilder logMessage, OperationLog operationLog, SpelExpressionParser parser) {
        if (operationLog.tags() != null && operationLog.tags().length > 0) {
            List<String> tags = new ArrayList<>();
            for (String tag : operationLog.tags()) {
                String parsedTag = SpelParseUtils.parseExpressionSafely(parser, tag);
                if (parsedTag != null && !parsedTag.isEmpty()) {
                    tags.add(parsedTag);
                }
            }

            if (!tags.isEmpty()) {
                logMessage.append("标签: ").append(String.join(", ", tags)).append("\n");
            }
        }
    }

    /**
     * 添加操作信息到日志
     * 
     * @param logMessage   日志消息构建器
     * @param operationLog 操作日志注解
     * @param parser       SpEL表达式解析器
     */
    private void appendOperationInfo(StringBuilder logMessage, OperationLog operationLog, SpelExpressionParser parser) {
        String operationType = SpelParseUtils.parseExpressionSafely(parser, operationLog.operationType());
        if (operationType != null) {
            logMessage.append("操作类型: ").append(operationType).append("\n");
        }

        String msg = truncateString(SpelParseUtils.parseExpressionSafely(parser, operationLog.msg()), MAX_LOG_LENGTH);
        if (msg != null) {
            logMessage.append("操作描述: ").append(msg).append("\n");
        }

        String operator = SpelParseUtils.parseExpressionSafely(parser, operationLog.operator());
        if (operator != null) {
            logMessage.append("操作人: ").append(operator).append("\n");
        }
    }

    /**
     * 添加扩展信息到日志
     * 
     * @param logMessage   日志消息构建器
     * @param operationLog 操作日志注解
     * @param parser       SpEL表达式解析器
     */
    private void appendExtras(StringBuilder logMessage, OperationLog operationLog, SpelExpressionParser parser) {
        if (operationLog.extras() != null && operationLog.extras().length > 0) {
            logMessage.append("扩展信息:\n");
            for (OperationLogExtra extra : operationLog.extras()) {
                String key = extra.key();
                String value = SpelParseUtils.parseExpressionSafely(parser, extra.value());
                if (key != null && value != null) {
                    logMessage.append("  - ").append(key).append(": ").append(value).append("\n");
                }
            }
        }
    }

    /**
     * 截断过长的字符串
     * 
     * @param input     输入字符串
     * @param maxLength 最大长度
     * @return 截断后的字符串
     */
    private String truncateString(String input, int maxLength) {
        if (input == null) {
            return null;
        }
        return input.length() > maxLength ? input.substring(0, maxLength) + "..." : input;
    }
}
