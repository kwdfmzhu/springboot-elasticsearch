package org.kwdfmzhu.github.bean;

import org.apache.lucene.search.join.ScoreMode;
import org.assertj.core.util.Lists;
import org.elasticsearch.index.query.*;
import org.kwdfmzhu.github.enums.ESQueryBuildClausesEnum;
import org.kwdfmzhu.github.enums.ESQueryBuildTypeEnum;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * Created by kwdfmzhu on 2017/7/5.
 */
public class ESQueryBuildEntity {
    private List<ESClausesTypeValue> mustCTVList;
    private List<ESClausesTypeValue> mustNotCTVList;
    private List<ESClausesTypeValue> shouldCTVList;

    private List<MatchQueryBuilder> matchQueryBuilderList;
    private List<RangeQueryBuilder> rangeQueryBuilderList;
    private List<WildcardQueryBuilder> wildcardQueryBuilderList;
    private List<NestedQueryBuilder> nestedQueryBuilderList;

    private BoolQueryBuilder boolQueryBuilder;
    private int searchFromPosition = 0;
    private int searchSize = 10000;

    public ESQueryBuildEntity() {
        this.mustCTVList = Lists.newArrayList();
        this.mustNotCTVList = Lists.newArrayList();
        this.shouldCTVList = Lists.newArrayList();
    }

    public boolean isEmpty() {
        return  CollectionUtils.isEmpty(this.mustCTVList)
                && CollectionUtils.isEmpty(this.mustNotCTVList)
                && CollectionUtils.isEmpty(this.shouldCTVList);
    }

    private void transform(List<ESClausesTypeValue> list) {
        this.matchQueryBuilderList = Lists.newArrayList();
        this.rangeQueryBuilderList = Lists.newArrayList();
        this.wildcardQueryBuilderList = Lists.newArrayList();
        this.nestedQueryBuilderList = Lists.newArrayList();
        list.forEach(esctv -> {
            ESQueryBuildTypeEnum type = esctv.getType();
            if(type.equals(ESQueryBuildTypeEnum.BASE)) {
                this.matchQueryBuilderList.add(QueryBuilders.matchQuery(esctv.getName(), esctv.getValue().toString()));
            }
            if(type.equals(ESQueryBuildTypeEnum.RANGE)) {
                RangeQueryBuilderEntity entity = (RangeQueryBuilderEntity)esctv.getValue();
                RangeQueryBuilder queryBuilder = QueryBuilders.rangeQuery(esctv.getName());
                if(entity.getFrom() != null)
                    queryBuilder.from(entity.getFrom());
                if(entity.getTo() != null)
                    queryBuilder.to(entity.getTo());
                queryBuilder.includeLower(entity.isIncludeLower());
                queryBuilder.includeUpper(entity.isIncludeUpper());
                this.rangeQueryBuilderList.add(queryBuilder);
            }
            if(type.equals(ESQueryBuildTypeEnum.WILDCARD)) {
                this.wildcardQueryBuilderList.add(QueryBuilders.wildcardQuery(esctv.getName(), esctv.toString()));
            }
            if(type.equals(ESQueryBuildTypeEnum.NESTED)) {
                this.nestedQueryBuilderList.add(QueryBuilders.nestedQuery(
                        esctv.getName(),
                        ((ESQueryBuildEntity)esctv.getValue()).toBoolQueryBuilder(),
                        ScoreMode.None
                ));
            }
        });
    }

    private BoolQueryBuilder getSubBoolQueryBuilder(ESQueryBuildClausesEnum clauses) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        if(clauses.equals(ESQueryBuildClausesEnum.MUST)) {
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
        if(clauses.equals(ESQueryBuildClausesEnum.SHOULD)) {
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
        if(clauses.equals(ESQueryBuildClausesEnum.MUSTNOT)) {
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
        subBoolQueryBuilder = this.getSubBoolQueryBuilder(ESQueryBuildClausesEnum.MUST);
        this.boolQueryBuilder.must(subBoolQueryBuilder);

        this.transform(this.shouldCTVList);
        subBoolQueryBuilder = this.getSubBoolQueryBuilder(ESQueryBuildClausesEnum.SHOULD);
        this.boolQueryBuilder.must(subBoolQueryBuilder);

        this.transform(this.mustNotCTVList);
        subBoolQueryBuilder = this.getSubBoolQueryBuilder(ESQueryBuildClausesEnum.MUSTNOT);
        this.boolQueryBuilder.must(subBoolQueryBuilder);

        return this.boolQueryBuilder;
    }

    public BoolQueryBuilder getBoolQueryBuilder() {
        this.toBoolQueryBuilder();
        return this.boolQueryBuilder;
    }

    public void addOneMustCTV(ESClausesTypeValue csv) {
        this.mustCTVList.add(csv);
    }

    public void addOneShouldCTV(ESClausesTypeValue csv) {
        this.shouldCTVList.add(csv);
    }

    public void addOneMustNotCTV(ESClausesTypeValue csv) {
        this.mustNotCTVList.add(csv);
    }

    public List<ESClausesTypeValue> getMustCTVList() {
        return mustCTVList;
    }

    public void setMustCTVList(List<ESClausesTypeValue> mustCTVList) {
        this.mustCTVList = mustCTVList;
    }

    public List<ESClausesTypeValue> getMustNotCTVList() {
        return mustNotCTVList;
    }

    public void setMustNotCTVList(List<ESClausesTypeValue> mustNotCTVList) {
        this.mustNotCTVList = mustNotCTVList;
    }

    public List<ESClausesTypeValue> getShouldCTVList() {
        return shouldCTVList;
    }

    public void setShouldCTVList(List<ESClausesTypeValue> shouldCTVList) {
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
