package com.goodman17.goodlog.spring.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import com.goodman17.goodlog.spring.service.DefaultOperationLogHandler;
import com.goodman17.goodlog.spring.service.OperationLogHandler;

/**
 * 操作日志自动配置类
 * <p>
 * 自动配置操作日志相关Bean，当没有自定义OperationLogHandler时注册默认实现
 * </p>
 */
@AutoConfiguration // 替换原@Configuration，表示这是自动配置类
public class OperationLogConfig {

    /**
     * 注册操作日志处理器Bean
     * 
     * @return 默认的操作日志处理器，当没有自定义实现时生效
     */
    @Bean
    @ConditionalOnMissingBean(value = OperationLogHandler.class)
    public OperationLogHandler operationLogHandler() {
        return new DefaultOperationLogHandler();
    }
}
