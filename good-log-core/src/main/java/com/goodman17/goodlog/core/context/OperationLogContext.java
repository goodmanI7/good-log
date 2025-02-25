package com.goodman17.goodlog.core.context;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 操作日志上下文管理类（基于 InheritableThreadLocal 实现线程间变量传递）
 * 用于存储和管理操作日志相关的上下文变量
 * 
 * <p>此类提供了在整个线程执行过程中存储和共享操作日志相关信息的能力，
 * 特别适用于在请求处理流程中跟踪用户操作、系统状态等信息。</p>
 * 
 * <p>使用示例：</p>
 * <pre>
 * try {
 *     // 设置请求相关信息
 *     OperationLogContext.setRequestId("req-123456");
 *     OperationLogContext.setIp("192.168.1.1");
 *     OperationLogContext.putVariable("userId", "10001");
 *     
 *     // 业务处理...
 *     
 * } finally {
 *     // 清理上下文，防止内存泄漏
 *     OperationLogContext.clear();
 * }
 * </pre>
 */
public class OperationLogContext {

    /**
     * 使用 InheritableThreadLocal 实现线程间变量传递，适用于父子线程场景
     * 当主线程创建子线程时，子线程会继承主线程的上下文变量
     */
    private static final InheritableThreadLocal<Map<String, Object>> VARIABLE_MAP = new InheritableThreadLocal<>();
    
    /**
     * 常用上下文变量键名常量
     */
    public static final String REQUEST_ID_KEY = "request_id";
    public static final String IP_KEY = "ip";
    public static final String USER_ID_KEY = "user_id";
    public static final String OPERATOR_KEY = "operator";
    public static final String OPERATION_TIME_KEY = "operation_time";

    /**
     * 添加或更新日志上下文变量
     * 
     * @param key   变量键（不可为空）
     * @param value 变量值（可为null）
     * @throws IllegalArgumentException 如果key为null或空字符串
     * @return 返回之前关联的值，如果没有则返回null
     */
    public static Object putVariable(String key, Object value) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("Key cannot be null or empty");
        }

        Map<String, Object> map = VARIABLE_MAP.get();
        if (map == null) {
            map = new HashMap<>();
            VARIABLE_MAP.set(map);
        }
        return map.put(key, value);
    }

    /**
     * 获取指定键的上下文变量值
     * 
     * @param <T> 返回值类型
     * @param key 变量键
     * @return 对应的变量值，如果不存在或类型不匹配则返回null
     */
    @SuppressWarnings("unchecked")
    public static <T> T getVariable(String key) {
        Map<String, Object> map = VARIABLE_MAP.get();
        if (map == null) {
            return null;
        }
        try {
            return (T) map.get(key);
        } catch (ClassCastException e) {
            return null;
        }
    }
    
    /**
     * 获取指定键的上下文变量值，如果不存在则返回默认值
     * 
     * @param <T> 返回值类型
     * @param key 变量键
     * @param defaultValue 默认值
     * @return 对应的变量值，如果不存在或类型不匹配则返回默认值
     */
    @SuppressWarnings("unchecked")
    public static <T> T getVariable(String key, T defaultValue) {
        Map<String, Object> map = VARIABLE_MAP.get();
        if (map == null || !map.containsKey(key)) {
            return defaultValue;
        }
        try {
            return (T) map.get(key);
        } catch (ClassCastException e) {
            return defaultValue;
        }
    }

    /**
     * 移除指定键的上下文变量
     * 
     * @param key 变量键
     * @return 被移除的变量值，如果不存在则返回null
     */
    public static Object removeVariable(String key) {
        Map<String, Object> map = VARIABLE_MAP.get();
        if (map == null) {
            return null;
        }
        return map.remove(key);
    }

    /**
     * 获取当前线程的上下文变量集合
     * 
     * @return 不可修改的变量集合，如果没有设置则返回空Map而非null
     */
    public static Map<String, Object> getVariableMap() {
        Map<String, Object> map = VARIABLE_MAP.get();
        return map != null ? Collections.unmodifiableMap(new HashMap<>(map)) : Collections.emptyMap();
    }

    /**
     * 清除当前线程的上下文变量
     * 
     * <p><strong>警告：</strong> 在父子线程场景中，清除操作不会影响父线程的上下文</p>
     * <p><strong>建议：</strong> 在finally块中调用以确保上下文清理，防止内存泄漏</p>
     */
    public static void clear() {
        VARIABLE_MAP.remove();
    }

    /**
     * 获取请求ID
     * 
     * @return 请求ID，如果不存在则返回null
     */
    public static String getRequestId() {
        return getVariable(REQUEST_ID_KEY);
    }

    /**
     * 设置请求ID
     * 
     * @param requestId 请求ID
     */
    public static void setRequestId(String requestId) {
        putVariable(REQUEST_ID_KEY, requestId);
    }

    /**
     * 获取IP地址
     * 
     * @return IP地址，如果不存在则返回null
     */
    public static String getIp() {
        return getVariable(IP_KEY);
    }

    /**
     * 设置IP地址
     * 
     * @param ip IP地址
     */
    public static void setIp(String ip) {
        putVariable(IP_KEY, ip);
    }
    
    /**
     * 获取用户ID
     * 
     * @return 用户ID，如果不存在则返回null
     */
    public static String getUserId() {
        return getVariable(USER_ID_KEY);
    }
    
    /**
     * 设置用户ID
     * 
     * @param userId 用户ID
     */
    public static void setUserId(String userId) {
        putVariable(USER_ID_KEY, userId);
    }
    
    /**
     * 获取操作人信息
     * 
     * @return 操作人信息，如果不存在则返回null
     */
    public static String getOperator() {
        return getVariable(OPERATOR_KEY);
    }
    
    /**
     * 设置操作人信息
     * 
     * @param operator 操作人信息
     */
    public static void setOperator(String operator) {
        putVariable(OPERATOR_KEY, operator);
    }
    
    /**
     * 批量设置上下文变量
     * 
     * @param variables 变量Map
     */
    public static void putAll(Map<String, Object> variables) {
        if (variables == null || variables.isEmpty()) {
            return;
        }
        
        Map<String, Object> map = VARIABLE_MAP.get();
        if (map == null) {
            map = new HashMap<>();
            VARIABLE_MAP.set(map);
        }
        map.putAll(variables);
    }
    
    /**
     * 检查上下文中是否包含指定键
     * 
     * @param key 键名
     * @return 如果存在返回true，否则返回false
     */
    public static boolean containsKey(String key) {
        Map<String, Object> map = VARIABLE_MAP.get();
        return map != null && map.containsKey(key);
    }
}
