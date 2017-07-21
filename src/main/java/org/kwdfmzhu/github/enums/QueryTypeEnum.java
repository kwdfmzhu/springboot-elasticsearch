package org.kwdfmzhu.github.enums;

/**
 * Created by kwdfmzhu on 2017/7/5.
 */
public enum QueryTypeEnum {
    /**
     * 基础类型，如姓名，住址等，包含number, string
     */
    BASE,
    /**
     * 范围类型，如查询年龄范围，生日范围，包含number，date
     */
    RANGE,
    /**
     * 通配符类型
     */
    WILDCARD,
    /**
     * 嵌套类型，暂时放这边 TODO
     */
    NESTED
    ;
}