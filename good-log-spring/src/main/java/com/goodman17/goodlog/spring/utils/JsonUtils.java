package com.goodman17.goodlog.spring.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

/**
 * JSON工具类
 * <p>
 * 提供JSON序列化和反序列化的工具方法，使用Fastjson作为底层实现。
 * 该工具类提供了异常安全的JSON转换功能，确保序列化过程中的异常被妥善处理。
 * </p>
 */
public class JsonUtils {

    private static final Logger log = LoggerFactory.getLogger(JsonUtils.class);

    /**
     * 安全地将对象转换为JSON字符串
     * <p>
     * 此方法会捕获序列化过程中的所有异常，确保调用者不需要进行异常处理。
     * 当序列化失败时，会返回包含错误信息的字符串，而不是抛出异常。
     * </p>
     * 
     * @param obj 要转换的对象
     * @return JSON字符串，如果对象为null则返回null，转换失败则返回包含错误信息的字符串
     */
    public static String safeJson(Object obj) {
        if (obj == null) {
            return null;
        }

        try {
            return JSON.toJSONString(obj);
        } catch (Exception e) {
            log.warn("[JSON序列化] 转换失败 | 类型: {} | 错误: {}",
                    obj.getClass().getSimpleName(), e.getMessage());
            return "JSON_SERIALIZE_FAILED: " + e.getMessage();
        }
    }
}
