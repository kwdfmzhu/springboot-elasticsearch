package org.kwdfmzhu.github.bean;

import org.kwdfmzhu.github.enums.ClausesEnum;
import org.kwdfmzhu.github.enums.QueryTypeEnum;

import java.util.Map;

/**
 * Created by kwdfmzhu on 2017/7/5.
 */
public class ClausesValue {
    private String name;
    private ClausesEnum clauses;
    /**
     * type 和 value 对应类型如下
     *     type: base -> value: string， int， float 等基础类型
     *     type: range -> value: {"start": 1, "end": 100} || RangeQueryTypeEntity, 默认include 上下限
     *     type: wildcard -> value: string, 比如查询消费标签时输入 0?0?00?0??0? 格式
     *     tyee: nested   -> name: bd_user_app & value: ESQueryBuildEntity对象
     */
    private QueryTypeEnum type;
    private Object value;

    public ClausesValue(String name, ClausesEnum clauses, QueryTypeEnum type, Object value) {
        this.name = name;
        this.clauses = clauses;
        this.type = type;
        this.value = value;
    }

    public ClausesValue() {
    }

    public void transformRangeEntity() {
        if(!(value instanceof Map)) {return;}

        Map<String, Integer> map = (Map)this.value;
        if(!map.containsKey("start") || !map.containsKey("end")) {return;}

        RangeQueryTypeEntity entity = new RangeQueryTypeEntity(
                map.get("start"), map.get("end"), true, true
        );

        this.value = entity;
    }

    @Override
    public String toString() {
        return "ClausesValue{" +
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

    public ClausesEnum getClauses() {
        return clauses;
    }

    public void setClauses(ClausesEnum clauses) {
        this.clauses = clauses;
    }

    public QueryTypeEnum getType() {
        return type;
    }

    public void setType(QueryTypeEnum type) {
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
