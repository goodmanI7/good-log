package com.goodman17.goodlog.core.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.goodman17.goodlog.core.annotation.CompareField;
import com.goodman17.goodlog.core.dto.CompareFieldInfo;

/**
 * 通用对象比较工具类，用于比较任意两个相同类型对象的字段差异
 * <p>
 * 主要功能：
 * 1. 可比较任意两个相同类型的对象之间的字段差异
 * 2. 支持通过 {@link CompareField} 注解定制比较行为
 * 3. 支持字段别名、忽略特定字段以及自定义差异消息格式
 * 4. 自动处理继承关系中的字段
 * </p>
 * 
 * @author goodman17
 */
public class ObjectComparator {
    
    // 缓存最大容量，防止缓存无限增长
    private static final int MAX_CACHE_SIZE = 500;
    
    // 字段缓存，使用LRU策略，保留最近使用的类
    private static final Map<Class<?>, List<Field>> FIELD_CACHE = Collections.synchronizedMap(
        new LinkedHashMap<Class<?>, List<Field>>(MAX_CACHE_SIZE + 1, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Class<?>, List<Field>> eldest) {
                return size() > MAX_CACHE_SIZE;
            }
        }
    );

    /**
     * 比较两个对象并返回格式化的差异消息字符串
     * 
     * @param oldValue 旧对象
     * @param newValue 新对象
     * @return 格式化的差异消息，如果没有差异则返回空字符串
     */
    public static String diffMsg(Object oldValue, Object newValue) {
        if (oldValue == null && newValue == null) {
            return "";
        }
        if (oldValue == null || newValue == null) {
            return "[对象] 旧值：" + oldValue + "，新值：" + newValue;
        }
        
        Map<String, CompareFieldInfo> differences = compareObjects(oldValue, newValue);
        if (differences.isEmpty()) {
            return "";
        }
        return differences.values().stream()
                .map(CompareFieldInfo::getDifferenceMsg)
                .collect(Collectors.joining("\n"));
    }

    /**
     * 比较两个相同类型的对象，返回字段差异
     * 
     * @param oldObj 旧对象
     * @param newObj 新对象
     * @param <T>    对象类型
     * @return 包含差异字段的Map，key为字段名，value为字段比较信息
     * @throws IllegalArgumentException 如果比较对象为空或类型不一致
     */
    public static <T> Map<String, CompareFieldInfo> compareObjects(T oldObj, T newObj) {
        // 参数校验
        if (oldObj == null || newObj == null) {
            throw new IllegalArgumentException("比较对象不能为空");
        }

        if (!oldObj.getClass().equals(newObj.getClass())) {
            throw new IllegalArgumentException("比较的对象必须是相同类型");
        }

        Map<String, CompareFieldInfo> differences = new HashMap<>();

        // 获取所有字段（包括继承的字段）
        List<Field> fields = getAllFields(oldObj.getClass());

        for (Field field : fields) {
            compareField(oldObj, newObj, field, differences);
        }

        return differences;
    }
    
    /**
     * 比较单个字段并记录差异
     * 
     * @param oldObj 旧对象
     * @param newObj 新对象
     * @param field 要比较的字段
     * @param differences 差异记录Map
     * @param <T> 对象类型
     */
    private static <T> void compareField(T oldObj, T newObj, Field field, Map<String, CompareFieldInfo> differences) {
        try {
            // 确保可以访问私有字段
            field.setAccessible(true);
            String fieldName = field.getName();
            
            // 处理字段注解
            CompareField compareField = field.getAnnotation(CompareField.class);
            // 如果字段被标记为忽略，则跳过
            if (compareField != null && compareField.ignore()) {
                return;
            }
            
            // 获取字段别名和差异方法
            String alias = compareField != null && !compareField.alias().isEmpty() 
                    ? compareField.alias() : fieldName;
            String diffMethod = compareField != null ? compareField.diffMethod() : null;

            // 获取字段值
            Object oldValue = field.get(oldObj);
            Object newValue = field.get(newObj);

            // 比较字段值
            if (!Objects.equals(oldValue, newValue)) {
                CompareFieldInfo compareFieldInfo = new CompareFieldInfo();
                compareFieldInfo.setField(fieldName);
                compareFieldInfo.setAlias(alias);
                compareFieldInfo.setOldValue(oldValue);
                compareFieldInfo.setNewValue(newValue);
                
                // 设置差异消息
                setDifferenceMessage(oldObj, oldValue, newValue, field, diffMethod, compareFieldInfo);
                
                differences.put(fieldName, compareFieldInfo);
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException("无法访问字段: " + field.getName(), e);
        }
    }
    
    /**
     * 设置差异消息，尝试使用自定义方法，如果失败则使用默认格式
     * 
     * @param obj 源对象
     * @param oldValue 旧值
     * @param newValue 新值
     * @param field 字段
     * @param diffMethod 自定义差异方法名
     * @param compareFieldInfo 比较信息对象
     */
    private static void setDifferenceMessage(Object obj, Object oldValue, Object newValue, 
            Field field, String diffMethod, CompareFieldInfo compareFieldInfo) {
        if (diffMethod != null && !diffMethod.isEmpty()) {
            try {
                // 尝试调用指定的比较方法
                Method method = obj.getClass().getDeclaredMethod(diffMethod, field.getType(), field.getType());
                method.setAccessible(true);
                Object result = method.invoke(obj, oldValue, newValue);
                compareFieldInfo.setDifferenceMsg(result != null ? result.toString() : "");
                return;
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                // 方法调用失败，将使用默认格式
            }
        }
        
        // 默认差异消息格式
        compareFieldInfo.setDifferenceMsg("[" + compareFieldInfo.getAlias() + "]旧值：" 
                + oldValue + "，新值：" + newValue);
    }

    /**
     * 获取类的所有字段，包括继承的字段
     * <p>
     * 使用LRU缓存提高性能，避免重复反射获取字段
     * 当缓存达到最大容量时，会自动移除最近最少使用的类信息
     * </p>
     * 
     * @param type 类
     * @return 所有字段列表
     */
    private static List<Field> getAllFields(Class<?> type) {
        // 尝试从缓存获取，如果不存在则计算并添加到缓存
        // LinkedHashMap的访问顺序特性会自动更新使用顺序
        List<Field> fields = FIELD_CACHE.get(type);
        if (fields == null) {
            synchronized (FIELD_CACHE) {
                // 双重检查，避免并发计算
                fields = FIELD_CACHE.get(type);
                if (fields == null) {
                    fields = computeFields(type);
                    FIELD_CACHE.put(type, fields);
                }
            }
        }
        return fields;
    }
    
    /**
     * 计算类的所有字段（包括继承的字段）
     * 
     * @param type 类
     * @return 字段列表
     */
    private static List<Field> computeFields(Class<?> type) {
        List<Field> fieldList = new ArrayList<>();
        // 获取当前类的所有字段
        fieldList.addAll(Arrays.asList(type.getDeclaredFields()));
        
        // 递归获取父类的所有字段，直到Object类
        Class<?> superClass = type.getSuperclass();
        if (superClass != null && superClass != Object.class) {
            fieldList.addAll(getAllFields(superClass));
        }
        
        return fieldList;
    }
    
    /**
     * 检查两个对象是否有差异
     * 
     * @param oldObj 旧对象
     * @param newObj 新对象
     * @return 如果有差异返回true，否则返回false
     */
    public static boolean hasDifference(Object oldObj, Object newObj) {
        if (oldObj == null && newObj == null) {
            return false;
        }
        if (oldObj == null || newObj == null) {
            return true;
        }
        
        return !compareObjects(oldObj, newObj).isEmpty();
    }
}
