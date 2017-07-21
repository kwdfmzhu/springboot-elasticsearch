package org.kwdfmzhu.github.service;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by kwdfmzhu on 2017/7/5.
 */
public class SearchResponseParser {
    private static final Logger logger = LoggerFactory.getLogger(SearchResponseParser.class);

    private Aggregations aggregations;

    private HitInfo hitInfo;

    private String scrollId;

    public static SearchResponseParser parse(SearchResponse response) {
        SearchResponseParser parser = new SearchResponseParser(response);
        return parser;
    }

    public Long getHitSourceTotal() {
        return this.hitInfo.getTotal();
    }

    public Float getHitMaxScore() {
        return this.hitInfo.getMaxScore();
    }

    public Integer getHitSourceLength() {return  this.hitInfo.getHitList().length;}

    public String getScrollId() {
        return this.scrollId;
    }

    public List<Map<String, Object>> getSourceAsMapList() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (SearchHit hit : this.hitInfo.getHitList()) {
            list.add(hit.getSource());
        }
        return list;
    }

//    public List<T> getSourceAsList(Class<T> clazz) {
//        List<T> list = new ArrayList<>();
//        for (SearchHit hit : this.hitInfo.getHitList()) {
//            JSON.parseObject(hit.getSourceAsString(), clazz);
//        }
//        return list;
//    }

    private SearchResponseParser(SearchResponse response) {
        this.init(response);
    }

    private void init(SearchResponse response) {
        this.scrollId = response.getScrollId();
        this.aggregations = response.getAggregations();
        this.hitInfo = new HitInfo(response.getHits());
    }

    class HitInfo {
        private Long total;
        private Float maxScore;
        private SearchHit[] hitList;

        public HitInfo(SearchHits searchHits) {
            this.total = searchHits.getTotalHits();
            this.maxScore = searchHits.getMaxScore();
            this.hitList = searchHits.getHits();
        }

        public Long getTotal() {
            return total;
        }

        public void setTotal(Long total) {
            this.total = total;
        }

        public Float getMaxScore() {
            return maxScore;
        }

        public void setMaxScore(Float maxScore) {
            this.maxScore = maxScore;
        }

        public SearchHit[] getHitList() {
            return hitList;
        }

        public void setHitList(SearchHit[] hitList) {
            this.hitList = hitList;
        }
    }

}

