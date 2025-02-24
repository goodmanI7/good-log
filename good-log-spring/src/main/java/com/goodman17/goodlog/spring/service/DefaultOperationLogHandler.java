package com.goodman17.goodlog.spring.service;

import com.goodman17.goodlog.core.annotation.OperationLog;
import com.goodman17.goodlog.core.spel.SpelExpressionParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DefaultOperationLogHandler implements OperationLogHandler {

    private static final Logger log = LoggerFactory.getLogger(DefaultOperationLogHandler.class);

    @Override
    public void handleLog(OperationLog[] operationLogs, String methodName, Object[] args, Object returnValue, SpelExpressionParser parser, Exception e, long timerCount) {
        log.info("111");
    }
}
