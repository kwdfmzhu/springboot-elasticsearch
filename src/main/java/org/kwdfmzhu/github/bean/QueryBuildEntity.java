package org.kwdfmzhu.github.bean;

import org.apache.lucene.search.join.ScoreMode;
import org.assertj.core.util.Lists;
import org.elasticsearch.index.query.*;
import org.kwdfmzhu.github.enums.ClausesEnum;
import org.kwdfmzhu.github.enums.QueryTypeEnum;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * Created by kwdfmzhu on 2017/7/5.
 */
public class QueryBuildEntity {
    private List<ClausesValue> mustCTVList;
    private List<ClausesValue> mustNotCTVList;
    private List<ClausesValue> shouldCTVList;

    private List<MatchQueryBuilder> matchQueryBuilderList;
    private List<RangeQueryBuilder> rangeQueryBuilderList;
    private List<WildcardQueryBuilder> wildcardQueryBuilderList;
    private List<NestedQueryBuilder> nestedQueryBuilderList;

    private BoolQueryBuilder boolQueryBuilder;
    private int searchFromPosition = 0;
    private int searchSize = 10000;

    public QueryBuildEntity() {
        this.mustCTVList = Lists.newArrayList();
        this.mustNotCTVList = Lists.newArrayList();
        this.shouldCTVList = Lists.newArrayList();
    }

    public boolean isEmpty() {
        return  CollectionUtils.isEmpty(this.mustCTVList)
                && CollectionUtils.isEmpty(this.mustNotCTVList)
                && CollectionUtils.isEmpty(this.shouldCTVList);
    }

    private void transform(List<ClausesValue> list) {
        this.matchQueryBuilderList = Lists.newArrayList();
        this.rangeQueryBuilderList = Lists.newArrayList();
        this.wildcardQueryBuilderList = Lists.newArrayList();
        this.nestedQueryBuilderList = Lists.newArrayList();
        list.forEach(esctv -> {
            QueryTypeEnum type = esctv.getType();
            if(type.equals(QueryTypeEnum.BASE)) {
                this.matchQueryBuilderList.add(QueryBuilders.matchQuery(esctv.getName(), esctv.getValue().toString()));
            }
            if(type.equals(QueryTypeEnum.RANGE)) {
                RangeQueryTypeEntity entity = (RangeQueryTypeEntity)esctv.getValue();
                RangeQueryBuilder queryBuilder = QueryBuilders.rangeQuery(esctv.getName());
                if(entity.getFrom() != null)
                    queryBuilder.from(entity.getFrom());
                if(entity.getTo() != null)
                    queryBuilder.to(entity.getTo());
                queryBuilder.includeLower(entity.isIncludeLower());
                queryBuilder.includeUpper(entity.isIncludeUpper());
                this.rangeQueryBuilderList.add(queryBuilder);
            }
            if(type.equals(QueryTypeEnum.WILDCARD)) {
                this.wildcardQueryBuilderList.add(QueryBuilders.wildcardQuery(esctv.getName(), esctv.toString()));
            }
            if(type.equals(QueryTypeEnum.NESTED)) {
                this.nestedQueryBuilderList.add(QueryBuilders.nestedQuery(
                        esctv.getName(),
                        ((QueryBuildEntity)esctv.getValue()).toBoolQueryBuilder(),
                        ScoreMode.None
                ));
            }
        });
    }

    private BoolQueryBuilder getSubBoolQueryBuilder(ClausesEnum clauses) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        if(clauses.equals(ClausesEnum.MUST)) {
            this.matchQueryBuilderList.forEach(matchQueryBuilder -> {
                boolQueryBuilder.must(matchQueryBuilder);
            });
            this.rangeQueryBuilderList.forEach(rangeQueryBuilder -> {
                boolQueryBuilder.must(rangeQueryBuilder);
            });
            this.wildcardQueryBuilderList.forEach(wildcardQueryBuilder -> {
                boolQueryBuilder.must(wildcardQueryBuilder);
            });
            this.nestedQueryBuilderList.forEach(nestedQueryBuilder -> {
                boolQueryBuilder.must(nestedQueryBuilder);
            });
        }
        if(clauses.equals(ClausesEnum.SHOULD)) {
            this.matchQueryBuilderList.forEach(matchQueryBuilder -> {
                boolQueryBuilder.should(matchQueryBuilder);
            });
            this.rangeQueryBuilderList.forEach(rangeQueryBuilder -> {
                boolQueryBuilder.should(rangeQueryBuilder);
            });
            this.wildcardQueryBuilderList.forEach(wildcardQueryBuilder -> {
                boolQueryBuilder.should(wildcardQueryBuilder);
            });
            this.nestedQueryBuilderList.forEach(nestedQueryBuilder -> {
                boolQueryBuilder.should(nestedQueryBuilder);
            });
        }
        if(clauses.equals(ClausesEnum.MUSTNOT)) {
            this.matchQueryBuilderList.forEach(matchQueryBuilder -> {
                boolQueryBuilder.mustNot(matchQueryBuilder);
            });
            this.rangeQueryBuilderList.forEach(rangeQueryBuilder -> {
                boolQueryBuilder.mustNot(rangeQueryBuilder);
            });
            this.wildcardQueryBuilderList.forEach(wildcardQueryBuilder -> {
                boolQueryBuilder.mustNot(wildcardQueryBuilder);
            });
            this.nestedQueryBuilderList.forEach(nestedQueryBuilder -> {
                boolQueryBuilder.mustNot(nestedQueryBuilder);
            });
        }
        System.out.println("==============================");
        System.out.println(this.boolQueryBuilder.toString());
        return boolQueryBuilder;
    }

    private QueryBuilder toBoolQueryBuilder() {
        this.boolQueryBuilder = QueryBuilders.boolQuery();
        BoolQueryBuilder subBoolQueryBuilder = null;

        this.transform(this.mustCTVList);
        subBoolQueryBuilder = this.getSubBoolQueryBuilder(ClausesEnum.MUST);
        this.boolQueryBuilder.must(subBoolQueryBuilder);

        this.transform(this.shouldCTVList);
        subBoolQueryBuilder = this.getSubBoolQueryBuilder(ClausesEnum.SHOULD);
        this.boolQueryBuilder.must(subBoolQueryBuilder);

        this.transform(this.mustNotCTVList);
        subBoolQueryBuilder = this.getSubBoolQueryBuilder(ClausesEnum.MUSTNOT);
        this.boolQueryBuilder.must(subBoolQueryBuilder);

        return this.boolQueryBuilder;
    }

    public BoolQueryBuilder getBoolQueryBuilder() {
        this.toBoolQueryBuilder();
        return this.boolQueryBuilder;
    }

    public void addOneMustCTV(ClausesValue csv) {
        this.mustCTVList.add(csv);
    }

    public void addOneShouldCTV(ClausesValue csv) {
        this.shouldCTVList.add(csv);
    }

    public void addOneMustNotCTV(ClausesValue csv) {
        this.mustNotCTVList.add(csv);
    }

    public List<ClausesValue> getMustCTVList() {
        return mustCTVList;
    }

    public void setMustCTVList(List<ClausesValue> mustCTVList) {
        this.mustCTVList = mustCTVList;
    }

    public List<ClausesValue> getMustNotCTVList() {
        return mustNotCTVList;
    }

    public void setMustNotCTVList(List<ClausesValue> mustNotCTVList) {
        this.mustNotCTVList = mustNotCTVList;
    }

    public List<ClausesValue> getShouldCTVList() {
        return shouldCTVList;
    }

    public void setShouldCTVList(List<ClausesValue> shouldCTVList) {
        this.shouldCTVList = shouldCTVList;
    }

    public int getSearchFromPosition() {
        return searchFromPosition;
    }

    public void setSearchFromPosition(int searchFromPosition) {
        this.searchFromPosition = searchFromPosition;
    }

    public int getSearchSize() {
        return searchSize;
    }

    public void setSearchSize(int searchSize) {
        this.searchSize = searchSize;
    }

    public void setBoolQueryBuilder(BoolQueryBuilder boolQueryBuilder) {
        this.boolQueryBuilder = boolQueryBuilder;
    }

}
