package com.goodman17.goodlog.core.dto;

/**
 * 字段比较信息
 * 用于存储对象字段比较的结果信息
 */
public class CompareFieldInfo {

    /**
     * 字段名称
     */
    private String field;

    /**
     * 字段别名
     */
    private String alias;

    /**
     * 旧值
     */
    private Object oldValue;

    /**
     * 新值
     */
    private Object newValue;

    /**
     * 差异描述信息
     */
    private String differenceMsg;

    /**
     * 获取字段名称
     * 
     * @return 字段名称
     */
    public String getField() {
        return field;
    }

    /**
     * 设置字段名称
     * 
     * @param field 字段名称
     */
    public void setField(String field) {
        this.field = field;
    }

    /**
     * 获取字段别名
     * 
     * @return 字段别名
     */
    public String getAlias() {
        return alias;
    }

    /**
     * 设置字段别名
     * 
     * @param alias 字段别名
     */
    public void setAlias(String alias) {
        this.alias = alias;
    }

    /**
     * 获取旧值
     * 
     * @return 旧值
     */
    public Object getOldValue() {
        return oldValue;
    }

    /**
     * 设置旧值
     * 
     * @param oldValue 旧值
     */
    public void setOldValue(Object oldValue) {
        this.oldValue = oldValue;
    }

    /**
     * 获取新值
     * 
     * @return 新值
     */
    public Object getNewValue() {
        return newValue;
    }

    /**
     * 设置新值
     * 
     * @param newValue 新值
     */
    public void setNewValue(Object newValue) {
        this.newValue = newValue;
    }

    /**
     * 获取差异描述信息
     * 
     * @return 差异描述信息
     */
    public String getDifferenceMsg() {
        return differenceMsg;
    }

    /**
     * 设置差异描述信息
     * 
     * @param differenceMsg 差异描述信息
     */
    public void setDifferenceMsg(String differenceMsg) {
        this.differenceMsg = differenceMsg;
    }
}
