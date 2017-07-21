package org.kwdfmzhu.github.bean;

/**
 * Created by kwdfmzhu on 2017/7/5.
 */
public class RangeQueryBuilderEntity {
    private Object from;
    private Object to;
    private boolean includeLower;
    private boolean includeUpper;

    public RangeQueryBuilderEntity(Object from, Object to, boolean includeLower, boolean includeUpper) {
        this.from = from;
        this.to = to;
        this.includeLower = includeLower;
        this.includeUpper = includeUpper;
    }

    public RangeQueryBuilderEntity(Object from, Object to) {
        this.from = from;
        this.to = to;
        this.includeLower = true;
        this.includeUpper = true;
    }

    public Object getFrom() {
        return from;
    }

    public void setFrom(Object from) {
        this.from = from;
    }

    public Object getTo() {
        return to;
    }

    public void setTo(Object to) {
        this.to = to;
    }

    public boolean isIncludeLower() {
        return includeLower;
    }

    public void setIncludeLower(boolean includeLower) {
        this.includeLower = includeLower;
    }

    public boolean isIncludeUpper() {
        return includeUpper;
    }

    public void setIncludeUpper(boolean includeUpper) {
        this.includeUpper = includeUpper;
    }
}

