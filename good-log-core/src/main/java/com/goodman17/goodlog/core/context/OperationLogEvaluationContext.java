package com.goodman17.goodlog.core.context;

import java.lang.reflect.Method;
import java.util.Map;

import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.util.CollectionUtils;

/**
 * 操作日志表达式求值上下文
 * <p>
 * 继承Spring的MethodBasedEvaluationContext，扩展操作日志需要的上下文变量：
 * 1. 方法返回值(ret)
 * 2. 异常信息(errorMsg)
 * 3. 通过OperationLogContext添加的上下文变量
 * </p>
 */
public class OperationLogEvaluationContext extends MethodBasedEvaluationContext {

    /**
     * 构造方法
     *
     * @param rootObject              根对象，通常为被调用的方法所属对象
     * @param method                  目标方法
     * @param arguments               方法参数数组
     * @param parameterNameDiscoverer 参数名称发现器
     * @param ret                     方法返回值（可以为null）
     * @param errorMsg                异常信息（可以为null）
     */
    public OperationLogEvaluationContext(Object rootObject, Method method, Object[] arguments,
            ParameterNameDiscoverer parameterNameDiscoverer, Object ret, String errorMsg) {
        super(rootObject, method, arguments, parameterNameDiscoverer);

        // 初始化基础变量
        initRetAndErrorMsg(ret, errorMsg);

        // 初始化上下文变量
        initContextVariables();
    }

    /**
     * 初始化返回值和异常信息变量
     */
    private void initRetAndErrorMsg(Object ret, String errorMsg) {
        setVariable("ret", ret);
        setVariable("errorMsg", errorMsg);
    }

    /**
     * 初始化操作日志上下文变量
     */
    private void initContextVariables() {
        Map<String, Object> context = OperationLogContext.getVariableMap();
        if (!CollectionUtils.isEmpty(context)) {
            context.forEach(this::setVariable);
        }
    }
}
