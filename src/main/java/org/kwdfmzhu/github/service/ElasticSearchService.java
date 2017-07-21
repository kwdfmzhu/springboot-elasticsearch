package org.kwdfmzhu.github.service;

import org.elasticsearch.action.search.SearchResponse;
import org.kwdfmzhu.github.bean.ESQueryBuildEntity;
import org.kwdfmzhu.github.bean.ESSortBuildEntity;
import org.kwdfmzhu.github.bean.ESSourceBuildEntity;

/**
 * Created by kwdfmzhu on 2017/7/5.
 */
public interface ElasticSearchService {
    /**
     * 判断index是否存在
     * @param index
     * @return index 存在返回true; index 不存在返回false
     */
    boolean isExistIndex(String index);

    /**
     * 判断index下的type是否存在
     * @param index
     * @param type
     * @return index/type不存在，返回false; type 存在，返回true
     */
    boolean isExistType(String index, String type);

    /**
     * 判断index下的type下的id是否存在
     * @param index
     * @param type
     * @param id
     * @return 不存在，返回false; type 存在，返回tru
     */
    boolean isExistId(String index, String type, String id);
    /**
     * 创建index
     * @param index
     * @return index存在不创建，返回true; 创建成功返回true; 创建失败返回false
     */
    boolean createIndex(String index);

    /**
     * 删除index
     * @param index
     */
    void deleteIndex(String index);

    /**
     * 删除index下的type
     * @param index
     * @param type
     */
    void deleteType(String index, String type);

    /**
     * 指定id，新加doc
     * @param index
     * @param type
     * @param id
     * @param data
     * @return id
     */
    String save(String index, String type, String id, String data);

    /**
     * 自增id，新加doc
     * @param index
     * @param type
     * @param data
     * @return id
     */
    String save(String index, String type, String data);

    /**
     * 修改指定id的doc
     * @param index
     * @param type
     * @param id
     * @param data
     */
    void update(String index, String type, String id, String data);

    /**
     * 返回index下的所有数据
     * @param indices
     * @param types
     * @return
     */
    SearchResponse searchAll(String[] indices, String[] types);

    /**
     * 根据entity中的条件搜索
     * @param index
     * @param type
     * @param entity
     * @return
     */
    SearchResponse search(String index, String type, ESQueryBuildEntity entity);

    /**
     * 带过滤的搜索
     * @param index
     * @param type
     * @param QBEntity
     * @return
     */
    SearchResponse search(String index, String type, ESQueryBuildEntity QBEntity, ESSourceBuildEntity SBEntity);

    SearchResponse scrollSearch(String[] indices, String[] types, ESQueryBuildEntity QBEntity);
    SearchResponse scrollSearch(String[] indices, String[] types, ESQueryBuildEntity QBEntity, ESSourceBuildEntity ScBEntity, ESSortBuildEntity StBEntity);
    SearchResponse scrollSearch(String scrollId);
}
