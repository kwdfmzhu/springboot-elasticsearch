package org.kwdfmzhu.github.bean;

import org.kwdfmzhu.github.enums.ESQueryBuildClausesEnum;
import org.kwdfmzhu.github.enums.ESQueryBuildTypeEnum;

import java.util.Map;

/**
 * Created by kwdfmzhu on 2017/7/5.
 */
public class ESClausesTypeValue {
    private String name;
    private ESQueryBuildClausesEnum clauses;
    /**
     * type 和 value 对应类型如下
     *     type: base -> value: string， int， float 等基础类型
     *     type: range -> value: {"start": 1, "end": 100} || RangeQueryBuilderEntity, 默认include 上下限
     *     type: wildcard -> value: string, 比如查询消费标签时输入 0?0?00?0??0? 格式
     *     tyee: nested   -> name: bd_user_app & value: ESQueryBuildEntity对象
     */
    private ESQueryBuildTypeEnum type;
    private Object value;

    public ESClausesTypeValue(String name, ESQueryBuildClausesEnum clauses, ESQueryBuildTypeEnum type, Object value) {
        this.name = name;
        this.clauses = clauses;
        this.type = type;
        this.value = value;
    }

    public ESClausesTypeValue() {
    }

    public void transformRangeEntity() {
        if(!(value instanceof Map)) {return;}

        Map<String, Integer> map = (Map)this.value;
        if(!map.containsKey("start") || !map.containsKey("end")) {return;}

        RangeQueryBuilderEntity entity = new RangeQueryBuilderEntity(
                map.get("start"), map.get("end"), true, true
        );

        this.value = entity;
    }

    @Override
    public String toString() {
        return "ESClausesTypeValue{" +
                "name='" + name + '\'' +
                ", clauses=" + clauses +
                ", type=" + type +
                ", value=" + value +
                '}';
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ESQueryBuildClausesEnum getClauses() {
        return clauses;
    }

    public void setClauses(ESQueryBuildClausesEnum clauses) {
        this.clauses = clauses;
    }

    public ESQueryBuildTypeEnum getType() {
        return type;
    }

    public void setType(ESQueryBuildTypeEnum type) {
        this.type = type;
    }

    public Object getValue() {
        this.transformRangeEntity();
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
