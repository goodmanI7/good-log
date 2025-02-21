package com.goodman17.goodlog.core.spel;

import java.util.Map;

import org.springframework.context.expression.AnnotatedElementKey;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.Expression;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 基于模板的SpEL表达式缓存求值器抽象类
 * <p>
 * 提供表达式解析缓存功能，提升重复表达式求值性能。支持自定义表达式前缀后缀，
 * 默认使用 #{ 和 } 作为模板表达式界定符。
 * </p>
 * 
 * @see org.springframework.expression.spel.standard.SpelExpressionParser
 * @see TemplateParserContext
 */
public abstract class CachedTemplateExpressionEvaluator {

    // 使用final修饰确保线程安全
    private final SpelExpressionParser parser;
    private final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    // 模板表达式界定符配置
    private static final String EXPRESSION_PREFIX = "#{";
    private static final String EXPRESSION_SUFFIX = "}";

    /**
     * 构造方法（指定表达式解析器）
     * 
     * @param parser SpEL表达式解析器实例，不可为null
     * @throws IllegalArgumentException 如果parser为null
     */
    protected CachedTemplateExpressionEvaluator(SpelExpressionParser parser) {
        Assert.notNull(parser, "SpElExpressionParser must not be null");
        this.parser = parser;
    }

    /**
     * 构造方法（使用默认解析器）
     * <p>
     * 默认创建新的SpelExpressionParser实例
     * </p>
     */
    protected CachedTemplateExpressionEvaluator() {
        this(new SpelExpressionParser());
    }

    /**
     * 获取表达式解析器
     * 
     * @return SpEL表达式解析器实例
     */
    protected SpelExpressionParser getParser() {
        return this.parser;
    }

    /**
     * 获取参数名称发现器
     * 
     * @return 参数名称发现器实例（带缓存功能）
     */
    protected ParameterNameDiscoverer getParameterNameDiscoverer() {
        return this.parameterNameDiscoverer;
    }

    /**
     * 获取或创建表达式实例（带缓存功能）
     * 
     * @param cache      表达式缓存Map（线程安全需由调用方保证）
     * @param elementKey 注解元素键，用于标识方法/类等元信息
     * @param expression 原始表达式字符串
     * @return 解析后的Expression对象
     * 
     * @apiNote 使用模板解析上下文（#{...}格式），若缓存未命中则创建新表达式并缓存
     */
    protected Expression getExpression(Map<ExpressionKey, Expression> cache,
            AnnotatedElementKey elementKey,
            String expression) {
        ExpressionKey expressionKey = createKey(elementKey, expression);
        return cache.computeIfAbsent(expressionKey,
                key -> getParser().parseExpression(expression,
                        new TemplateParserContext(EXPRESSION_PREFIX, EXPRESSION_SUFFIX)));
    }

    // 创建缓存键（保持私有可见性）
    private ExpressionKey createKey(AnnotatedElementKey elementKey, String expression) {
        return new ExpressionKey(elementKey, expression);
    }

    /**
     * 表达式缓存键对象
     * <p>
     * 组合注解元素键和表达式字符串作为唯一标识，
     * 实现Comparable接口以支持有序存储
     * </p>
     */
    protected static class ExpressionKey implements Comparable<ExpressionKey> {

        private final AnnotatedElementKey element; // 不可变注解元素键
        private final String expression; // 不可变表达式字符串

        /**
         * 构造缓存键
         * 
         * @param element    注解元素键，不可为null
         * @param expression 表达式字符串，不可为null
         * @throws IllegalArgumentException 如果参数为null
         */
        protected ExpressionKey(AnnotatedElementKey element, String expression) {
            Assert.notNull(element, "AnnotatedElementKey must not be null");
            Assert.notNull(expression, "Expression must not be null");
            this.element = element;
            this.expression = expression;
        }

        // equals和hashCode方法保持高效实现
        @Override
        public boolean equals(@Nullable Object other) {
            if (this == other)
                return true;
            if (!(other instanceof ExpressionKey))
                return false;
            ExpressionKey otherKey = (ExpressionKey) other;
            return this.element.equals(otherKey.element)
                    && this.expression.equals(otherKey.expression);
        }

        @Override
        public int hashCode() {
            return 31 * this.element.hashCode() + this.expression.hashCode();
        }

        @Override
        public String toString() {
            return String.format("%s [expression=%s]", this.element, this.expression);
        }

        /**
         * 实现自然排序：先按元素键排序，再按表达式字符串排序
         */
        @Override
        public int compareTo(ExpressionKey other) {
            int elementCompare = this.element.toString().compareTo(other.element.toString());
            return (elementCompare != 0) ? elementCompare
                    : this.expression.compareTo(other.expression);
        }
    }
}
