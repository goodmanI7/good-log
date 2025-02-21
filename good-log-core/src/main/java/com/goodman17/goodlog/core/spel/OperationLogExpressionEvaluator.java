package com.goodman17.goodlog.core.spel;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.expression.AnnotatedElementKey;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.Expression;
import org.springframework.lang.Nullable;

import com.goodman17.goodlog.core.context.OperationLogEvaluationContext;

/**
 * 操作日志表达式求值器，继承自模板表达式求值缓存基类
 * <p>
 * 提供基于SpEL表达式的解析能力，支持：
 * 1. 表达式缓存提升性能
 * 2. 方法级上下文信息获取
 * 3. 安全表达式解析
 * 4. 动态评估上下文构建
 * </p>
 */
public class OperationLogExpressionEvaluator extends CachedTemplateExpressionEvaluator {

    private static final Logger log = LoggerFactory.getLogger(OperationLogExpressionEvaluator.class);

    /**
     * 表达式缓存（线程安全）
     * Key: 由方法签名和表达式内容组成的复合键
     * Value: 已解析的Expression对象
     */
    private final Map<ExpressionKey, Expression> expressionCache = new ConcurrentHashMap<>(64);

    /**
     * 目标方法缓存（线程安全）
     * Key: 方法签名和所在类的组合键
     * Value: 实际执行的目标方法（处理AOP代理情况）
     */
    private final Map<AnnotatedElementKey, Method> targetMethodCache = new ConcurrentHashMap<>(64);

    /**
     * 获取或创建表达式实例（带缓存机制）
     *
     * @param elementKey 方法/类标识键，用于区分不同的上下文
     * @param expression 需要解析的SpEL表达式
     * @return 解析后的Expression对象
     */
    public Expression getExpression(AnnotatedElementKey elementKey, String expression) {
        return getExpression(this.expressionCache, elementKey, expression);
    }

    /**
     * 解析表达式并获取结果（强类型版本）
     *
     * @param methodKey   方法标识键
     * @param expression  SpEL表达式
     * @param evalContext 评估上下文
     * @param clazz       期望的返回类型
     * @param <T>         返回类型泛型参数
     * @return 解析结果，可能为null
     * @throws EvaluationException 当表达式解析失败时抛出
     */
    public <T> T parseExpression(AnnotatedElementKey methodKey, String expression, EvaluationContext evalContext,
            Class<T> clazz) {
        return getExpression(methodKey, expression).getValue(evalContext, clazz);
    }

    /**
     * 安全解析表达式（异常处理版本）
     *
     * @param methodKey   方法标识键
     * @param expression  SpEL表达式
     * @param evalContext 评估上下文
     * @param clazz       期望的返回类型
     * @param <T>         返回类型泛型参数
     * @return 解析结果，解析失败时返回null并记录错误日志
     */
    public <T> T safeParseExpression(AnnotatedElementKey methodKey, String expression, EvaluationContext evalContext,
            Class<T> clazz) {
        try {
            return parseExpression(methodKey, expression, evalContext, clazz);
        } catch (Exception e) {
            log.error("解析表达式出错: 方法[{}] 表达式[{}]", expression, e);
            return null;
        }
    }

    /**
     * 创建表达式评估上下文
     *
     * @param method      目标方法
     * @param args        方法参数
     * @param target      目标对象实例
     * @param targetClass 目标类类型
     * @param result      方法执行结果（成功时）
     * @param errorMsg    错误信息（异常时）
     * @param beanFactory Spring Bean工厂（可选）
     * @return 配置完整的评估上下文
     */
    public EvaluationContext createEvaluationContext(Method method, Object[] args, Object target, Class<?> targetClass,
            @Nullable Object result, @Nullable String errorMsg, @Nullable BeanFactory beanFactory) {
        // 创建根对象，封装方法上下文信息
        ExpressionRootObject rootObject = new ExpressionRootObject(method, args, target, targetClass);

        // 构建评估上下文
        OperationLogEvaluationContext context = new OperationLogEvaluationContext(
                rootObject,
                getTargetMethod(targetClass, method),
                args,
                getParameterNameDiscoverer(),
                result,
                errorMsg);

        // 设置Bean解析器（如果存在BeanFactory）
        if (beanFactory != null) {
            context.setBeanResolver(new BeanFactoryResolver(beanFactory));
        }
        return context;
    }

    /**
     * 获取目标方法（处理AOP代理情况）
     * <p>
     * 使用缓存机制提升性能，避免重复反射调用
     * </p>
     *
     * @param targetClass 实际目标类
     * @param method      接口声明的方法
     * @return 目标类中的具体实现方法
     */
    private Method getTargetMethod(Class<?> targetClass, Method method) {
        AnnotatedElementKey methodKey = new AnnotatedElementKey(method, targetClass);
        return targetMethodCache.computeIfAbsent(methodKey,
                key -> AopUtils.getMostSpecificMethod(method, targetClass));
    }

    /**
     * 表达式根对象，提供SpEL表达式中的#root访问能力
     * <p>
     * 在SpEL表达式中可以通过以下方式访问：
     * - #{#root.methodName} 获取方法名
     * - #{#root.args} 获取方法参数数组
     * - #{#root.target} 获取目标对象实例
     * </p>
     */
    public static class ExpressionRootObject {
        private final Method method;
        private final Object[] args;
        private final Object target;
        private final Class<?> targetClass;

        /**
         * 构造根对象
         * 
         * @param method      执行的方法
         * @param args        方法参数数组
         * @param target      目标对象实例
         * @param targetClass 目标类类型
         */
        public ExpressionRootObject(Method method, Object[] args, Object target, Class<?> targetClass) {
            this.method = method;
            this.args = args;
            this.target = target;
            this.targetClass = targetClass;
        }

        /**
         * 获取方法名称（SpEL快捷访问）
         * 
         * @return 当前执行的方法名称
         */
        public String getMethodName() {
            return this.method.getName();
        }

        public Method getMethod() {
            return this.method;
        }

        public Object[] getArgs() {
            return this.args;
        }

        public Object getTarget() {
            return this.target;
        }

        public Class<?> getTargetClass() {
            return this.targetClass;
        }
    }
}
