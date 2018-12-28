package com.pyg.search.service;

import java.util.List;
import java.util.Map;

/**
 * SKU搜索服务接口
 * @author Administrator
 *
 */

public interface ItemSearchService {
    /**
     * 根据关键字搜索，关键词是一个map集合
     * @param searchMap
     * @return
     */
    public Map<String,Object> search(Map searchMap);

    /**
     * 把运营商审核通过的商品添加到索引库中
     * @param itemList
     */
    void importItemList(List itemList);
    /**
     * 从索引库删除索引
     * @param goodsId
     */
    public void deleteByGoodsIds(List<Long> goodsId);
}
