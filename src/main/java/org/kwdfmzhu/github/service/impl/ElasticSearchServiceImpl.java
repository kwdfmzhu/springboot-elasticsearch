package org.kwdfmzhu.github.service.impl;

import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequestBuilder;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.kwdfmzhu.github.bean.QueryBuildEntity;
import org.kwdfmzhu.github.bean.SortBuildEntity;
import org.kwdfmzhu.github.bean.SourceBuildEntity;
import org.kwdfmzhu.github.service.ElasticSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

/**
 * Created by kwdfmzhu on 2017/7/5.
 */
@Repository
public class ElasticSearchServiceImpl implements ElasticSearchService{
    private static final Logger logger = LoggerFactory.getLogger(ElasticSearchServiceImpl.class);

    private static final Integer UPDATE_CONFLICT_RETY_TIME = 3;

    @Value("${elasticsearch.host}")
    private String esHost;

    @Value("${elasticsearch.port}")
    private Integer esPort;

    @Value("${elasticsearch.cluster.name}")
    private String esClusterName;

    private TransportClient client;

    @PostConstruct
    public void init() throws UnknownHostException {
        Settings settings = Settings.builder().put("cluster.name", esClusterName).build();
        this.client = new PreBuiltTransportClient(settings)
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(esHost), esPort));
    }

    @PreDestroy
    public void destroy() {
        this.client.close();
    }

    @Override
    public boolean isExistIndex(String index) {
        return this.client.admin().indices().prepareExists(index).get().isExists();
    }

    @Override
    public boolean isExistType(String index, String type) {
        if (!this.isExistIndex(index))
            return false;

        GetMappingsResponse response = this.client.admin().indices().prepareGetMappings().get();
        if(response == null) {
            logger.error("index:{}, type:{} 获取到的GetMappingsResponse is null", index, type);
            return false;
        }

        ImmutableOpenMap<String, ImmutableOpenMap<String, MappingMetaData>> immutableParentOpenMap = response.getMappings();
        if(immutableParentOpenMap == null || immutableParentOpenMap.isEmpty()) {
            logger.error("index:{}, type:{} 获取到的 immutableParentOpenMap is null", index, type);
            return false;
        }

        //TODO 一下方式如果index设置了别名，那么返回的是null，无法判断type是否存在.
        ImmutableOpenMap<String, MappingMetaData> immutableChildOpenMap = immutableParentOpenMap.get(index);
        if(immutableChildOpenMap == null || immutableChildOpenMap.isEmpty()) {
            logger.error("index:{}, type:{} 获取到的 immutableChildOpenMap is null", index, type);
            return false;
        }
        return immutableChildOpenMap.getOrDefault(type, null) != null;
    }

    @Override
    public boolean isExistId(String index, String type, String id) {
        return this.client.prepareGet(index, type, id).get().isExists();
    }

    @Override
    public boolean createIndex(String index) {
        if (this.isExistIndex(index))
            return true;
        this.client.admin().indices().prepareCreate(index).execute().actionGet();
        return this.isExistIndex(index);
    }

    @Override
    public void deleteIndex(String index) {
        if (!this.isExistIndex(index))
            return;
        this.client.admin().indices().prepareDelete(index).execute().actionGet();
    }

    @Override
    public void deleteType(String index, String type) {
        this.client.prepareDelete().setIndex(index).setType(type).execute().actionGet();
    }

    //PUT /{index}/{type}/{id}
    @Override
    public String save(String index, String type, String id, String data) {
        IndexResponse response = this.client.prepareIndex(index, type, id)
                .setOpType(IndexRequest.OpType.CREATE)
                .setSource(data)
                .get();
        return response.getId();
    }

    //POST /{index}/{type}
    @Override
    public String save(String index, String type, String data) {
        IndexResponse response = this.client.prepareIndex(index, type)
                .setOpType(IndexRequest.OpType.INDEX)
                .setSource(data)
                .get();
        return response.getId();
    }

    @Override
    public void update(String index, String type, String id, String data) {
        if (!this.isExistId(index, type, id)) {
            return;
        }
        this.client.prepareUpdate(index, type, id).setDoc(data).setRetryOnConflict(UPDATE_CONFLICT_RETY_TIME).get();
        return;
    }

    private SearchResponse search(String[] indices, String[] types, QueryBuildEntity entity) {
        QueryBuilder queryBuilder = this.getQueryBuilderWithEntity(entity);
        int from = entity.getSearchFromPosition();
        int size = entity.getSearchSize();
        return this.getSearchResponse(indices, types, queryBuilder, null, null, from, size);
    }

    @Override
    public SearchResponse search(String index, String type, QueryBuildEntity entity) {
        String[] indices = {index};
        String[] types = {type};
        return this.search(indices, types, entity);
    }


    private SearchResponse search(String[] indices, String[] types, QueryBuildEntity QBEntity, SourceBuildEntity SBEntity) {
        QueryBuilder queryBuilder = this.getQueryBuilderWithEntity(QBEntity);
        SearchSourceBuilder sourceBuilder = this.getSourceBuilderWithEntity(SBEntity);
        int from = QBEntity.getSearchFromPosition();
        int size = QBEntity.getSearchSize();
        return this.getSearchResponse(indices, types, queryBuilder, sourceBuilder, null, from, size);
    }

    @Override
    public SearchResponse search(String index, String type, QueryBuildEntity QBEntity, SourceBuildEntity SBEntity) {
        String[] indices = {index};
        String[] types = {type};
        return this.search(indices, types, QBEntity, SBEntity);
    }

    @Override
    public SearchResponse scrollSearch(String[] indices, String[] types, QueryBuildEntity QBEntity) {
        SourceBuildEntity ScBEntity = new SourceBuildEntity();
        SortBuildEntity StBEntity = new SortBuildEntity();
        return this.getScrollSearchResponse(indices, types, QBEntity, ScBEntity, StBEntity);
    }

    @Override
    public SearchResponse scrollSearch(String[] indices, String[] types, QueryBuildEntity QBEntity, SourceBuildEntity ScBEntity, SortBuildEntity StBEntity) {
        return this.getScrollSearchResponse(indices, types, QBEntity, ScBEntity, StBEntity);
    }

    @Override
    public SearchResponse scrollSearch(String scrollId) {
        return this.getScrollSearchResponse(scrollId);
    }

    @Override
    public SearchResponse searchAll(String[] indices, String[] types) {
        return this.getSearchResponse(indices, types, QueryBuilders.matchAllQuery(), null, null, 0, -1);
    }

    private SearchResponse getSearchResponse(String[] indices, String[] types,
                                             QueryBuilder queryBuilder, SearchSourceBuilder sourceBuilder,
                                             AggregationBuilder aggregationBuilder, int from, int size) {
        SearchRequestBuilder request = this.client.prepareSearch(indices).setTypes(types)
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH);

        if(sourceBuilder != null)
            request.setSource(sourceBuilder);

        if(aggregationBuilder != null)
            request.addAggregation(aggregationBuilder);

        request.setQuery(queryBuilder).setFrom(from).setSize(size).setExplain(true);
        return request.get();

    }

    private SearchResponse getScrollSearchResponse(String[] indices, String[] types,
                                                   QueryBuildEntity QBEntity, SourceBuildEntity ScBEntity, SortBuildEntity StBEntity) {

        QueryBuilder queryBuilder = this.getQueryBuilderWithEntity(QBEntity);
        SearchSourceBuilder sourceBuilder = this.getSourceBuilderWithEntity(ScBEntity);
        List<SortBuilder> sortBuilderList = this.getSortBuildListWithEntity(StBEntity);
        int size = QBEntity.getSearchSize();


        SearchRequestBuilder request = this.client.prepareSearch(indices)
                .setScroll(new TimeValue(6000));

        if(sourceBuilder != null)
            request.setSource(sourceBuilder);

        request.setQuery(queryBuilder);
        sortBuilderList.forEach(sortBuilder -> request.addSort(sortBuilder));
        request.setSize(size);
        return request.get();
    }

    private SearchResponse getScrollSearchResponse(String scrollId) {
        SearchScrollRequestBuilder requestBuilder = this.client.prepareSearchScroll(scrollId).setScroll(new TimeValue(6000));
        return requestBuilder.get();
    }

    private QueryBuilder getQueryBuilderWithEntity(QueryBuildEntity entity) {
        return entity.getBoolQueryBuilder();
    }

    private SearchSourceBuilder getSourceBuilderWithEntity(SourceBuildEntity entity) {
        return entity.getSourceBuilder();
    }

    private List<SortBuilder> getSortBuildListWithEntity(SortBuildEntity entity) {
        return entity.getSortBuilderList();
    }
}