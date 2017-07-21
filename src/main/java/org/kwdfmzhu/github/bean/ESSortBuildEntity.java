package org.kwdfmzhu.github.bean;

import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kwdfmzhu on 2017/7/5.
 */
public class ESSortBuildEntity {
    private List<String> ascList;
    private List<String> descList;

    private List<SortBuilder> sortBuilderList;

    public ESSortBuildEntity() {
        this.ascList = new ArrayList<>();
        this.descList = new ArrayList<>();
    }

    public void addOneASC(String include) {
        this.ascList.add(include);
    }

    public void addOneDESC(String exclude) {
        this.descList.add(exclude);
    }

    public List<SortBuilder> getSortBuilderList() {
        this.toSortBuilder();
        return this.sortBuilderList;
    }

    public void setSortBuilderList(List<SortBuilder> sortBuilderList) {
        this.sortBuilderList = sortBuilderList;
    }

    private List<SortBuilder> toSortBuilder() {
        this.sortBuilderList = new ArrayList<>();
        ascList.forEach(field -> {
            this.sortBuilderList.add(SortBuilders.fieldSort(field).order(SortOrder.ASC));
        });

        descList.forEach(field -> {
            this.sortBuilderList.add(SortBuilders.fieldSort(field).order(SortOrder.DESC));
        });

        return this.sortBuilderList;
    }

    public boolean isEmpty() {
        return CollectionUtils.isEmpty(ascList) && CollectionUtils.isEmpty(descList);
    }

}

