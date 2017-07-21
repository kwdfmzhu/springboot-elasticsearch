package org.kwdfmzhu.github.bean;

import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kwdfmzhu on 2017/7/5.
 */
public class SourceBuildEntity {
    private List<String> includes;
    private List<String> excludes;
    private SearchSourceBuilder sourceBuilder;

    public SourceBuildEntity() {
        this.includes = new ArrayList<>();
        this.excludes = new ArrayList<>();
    }

    public SearchSourceBuilder getSourceBuilder() {
        this.toSourceBuilder();
        return sourceBuilder;
    }

    public void addOneInclude(String include) {
        this.includes.add(include);
    }

    public void addOneExclude(String exclude) {
        this.excludes.add(exclude);
    }

    public boolean isEmpty() {
        return CollectionUtils.isEmpty(this.includes) && CollectionUtils.isEmpty(this.excludes);
    }

    private SearchSourceBuilder toSourceBuilder() {
        this.sourceBuilder = SearchSourceBuilder.searchSource();
        this.sourceBuilder.fetchSource(this.includes.toArray(new String[this.includes.size()]),
                this.excludes.toArray(new String[this.excludes.size()]));
        return this.sourceBuilder;
    }


    public List<String> getIncludes() {
        return includes;
    }

    public void setIncludes(List<String> includes) {
        this.includes = includes;
    }

    public List<String> getExcludes() {
        return excludes;
    }

    public void setExcludes(List<String> excludes) {
        this.excludes = excludes;
    }
}

