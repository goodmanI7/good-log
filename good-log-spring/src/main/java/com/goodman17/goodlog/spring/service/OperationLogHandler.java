package com.goodman17.goodlog.spring.service;


import com.goodman17.goodlog.core.annotation.OperationLog;
import com.goodman17.goodlog.core.spel.SpelExpressionParser;

public interface OperationLogHandler {

    void handleLog(OperationLog[] operationLogs, String methodName, Object[] args, Object returnValue,
                   SpelExpressionParser parser, Exception e, long timerCount);
}
