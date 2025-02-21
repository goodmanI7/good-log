package com.goodman17.goodlog.core.context;

import java.util.HashMap;
import java.util.Map;

/**
 * 操作日志上下文管理类（基于 InheritableThreadLocal 实现线程间变量传递）
 * 用于存储和管理操作日志相关的上下文变量
 */
public class OperationLogContext {

    // 使用 InheritableThreadLocal 实现线程间变量传递，适用于父子线程场景
    private static final InheritableThreadLocal<Map<String, Object>> variableMap = new InheritableThreadLocal<>();

    /**
     * 添加日志上下文变量
     * 
     * @param key   变量键（不可为空）
     * @param value 变量值（可为null，但建议保持非空）
     * @throws IllegalArgumentException 如果key为null或空字符串
     */
    public static void putVariable(String key, Object value) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("Key cannot be null or empty");
        }

        // 使用 computeIfAbsent 优化初始化逻辑
        Map<String, Object> map = variableMap.get();
        if (map == null) {
            map = new HashMap<>();
            variableMap.set(map);
        }
        map.computeIfAbsent(key, k -> value);
    }

    /**
     * 获取当前线程的上下文变量集合
     * 
     * @return 不可修改的变量集合（可能返回null）
     * @apiNote 返回的Map是线程安全的，但修改操作请通过putVariable方法
     */
    public static Map<String, Object> getVariableMap() {
        Map<String, Object> map = variableMap.get();
        return map != null ? new HashMap<>(map) : null;
    }

    /**
     * 清除当前线程的上下文变量
     * 
     * @warning 在父子线程场景中，清除操作不会影响父线程的上下文
     * @recommended 建议在finally块中调用以确保上下文清理
     */
    public static void clear() {
        variableMap.remove();
    }
}
