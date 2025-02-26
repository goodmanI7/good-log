package com.goodman17.goodlog.core.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.goodman17.goodlog.core.spel.SpelExpressionParser;

public class SpelParseUtils {

    private static final Logger log = LoggerFactory.getLogger(SpelParseUtils.class);

    /**
     * 安全解析SpEL表达式
     * 
     * @param parser     SpEL表达式解析器
     * @param expression 表达式字符串
     * @return 解析结果，解析失败返回错误信息
     */
    public static String parseExpressionSafely(SpelExpressionParser parser, String expression) {
        if (expression == null || expression.trim().isEmpty()) {
            return null;
        }

        try {
            return parser.safeParseExpression(expression);
        } catch (Exception e) {
            log.warn("[SpEL解析] 表达式解析失败: {} | 错误: {}", expression, e.getMessage());
            return "PARSE_ERROR: " + e.getMessage();
        }
    }
}
