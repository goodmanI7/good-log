package com.goodman17.goodlog.core.spel;

import org.springframework.context.expression.AnnotatedElementKey;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.SpelEvaluationException;

/**
 * SpEL 表达式解析器，封装表达式解析逻辑并提供安全解析方法
 * <p>
 * 该类主要功能：
 * 1. 提供类型安全的表达式解析方法
 * 2. 支持安全解析（解析失败返回null）
 * 3. 通过方法级缓存提升解析性能
 * </p>
 *
 */
public class SpelExpressionParser {

    /**
     * 表达式评估器，负责实际的表达式解析工作
     */
    private final OperationLogExpressionEvaluator expressionEvaluator;

    /**
     * 方法级别缓存键，用于优化重复方法的表达式解析性能
     */
    private final AnnotatedElementKey methodKey;

    /**
     * 评估上下文，包含表达式解析所需的变量和函数
     */
    private final EvaluationContext evaluationContext;

    /**
     * 构造方法初始化解析器组件
     * 
     * @param expressionEvaluator 表达式评估器实例
     * @param methodKey           方法元数据键，用于缓存优化
     * @param evaluationContext   表达式解析上下文环境
     */
    public SpelExpressionParser(OperationLogExpressionEvaluator expressionEvaluator,
            AnnotatedElementKey methodKey, EvaluationContext evaluationContext) {
        this.expressionEvaluator = expressionEvaluator;
        this.methodKey = methodKey;
        this.evaluationContext = evaluationContext;
    }

    /**
     * 解析SpEL表达式并返回指定类型结果
     * 
     * @param expression 要解析的SpEL表达式
     * @param type       期望的返回类型
     * @param <T>        返回类型泛型参数
     * @return 解析后的结果对象
     * @throws SpelEvaluationException 当表达式解析失败时抛出
     */
    public <T> T parseExpression(String expression, Class<T> type) {
        return expressionEvaluator.parseExpression(methodKey, expression, evaluationContext, type);
    }

    /**
     * 安全解析SpEL表达式（解析失败返回null）
     * 
     * @param expression 要解析的SpEL表达式
     * @param type       期望的返回类型
     * @param <T>        返回类型泛型参数
     * @return 解析成功返回结果对象，失败返回null
     */
    public <T> T safeParseExpression(String expression, Class<T> type) {
        return expressionEvaluator.safeParseExpression(methodKey, expression, evaluationContext, type);
    }

    /**
     * 解析SpEL表达式并返回字符串结果（便捷方法）
     * 
     * @param expression 要解析的SpEL表达式
     * @return 解析后的字符串结果
     */
    public String parseExpression(String expression) {
        return parseExpression(expression, String.class);
    }

    /**
     * 安全解析SpEL表达式并返回字符串结果（便捷方法）
     * 
     * @param expression 要解析的SpEL表达式
     * @return 解析成功返回字符串结果，失败返回null
     */
    public String safeParseExpression(String expression) {
        return safeParseExpression(expression, String.class);
    }
}
