package com.goodman17.goodlog.spring.test.service;

import com.goodman17.goodlog.core.annotation.OperationLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author lirenhao
 * date: 2025/2/24 17:09
 */
@Slf4j
@Service
public class LogTestService {

    @OperationLog(bizId = "#{#p0}", bizType = "'WORKFLOW'", msg = "修改工作流")
    public void printLog(String bizId) {
        log.info("print log");
    }
}
