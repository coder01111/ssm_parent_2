package com.pyg.search.serviceImpl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import com.pyg.search.service.ItemSearchService;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.Group;
import org.apache.solr.client.solrj.response.GroupCommand;
import org.apache.solr.client.solrj.response.GroupResponse;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.SimpleQuery;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service(timeout = 100000)
public class ItemSearchServiceImpl implements ItemSearchService {
    @Autowired
    private SolrTemplate solrTemplate;
    @Autowired
    @Qualifier("httpsolrServer")
    private HttpSolrServer httpSolrServer;
    @Autowired
    private RedisTemplate redisTemplate;


    @Override
    public Map<String, Object> search(Map searchMap) {
        HashMap<String, Object> resultMap = new HashMap<>();
        try {
            String keywords = searchMap.get("keywords").toString().replace(" ", "");
            if (StringUtils.isEmpty(keywords)) {
                keywords = "*";
            }
            //        SimpleQuery query = new SimpleQuery();

            //创建一个高亮显示查询条件
//        SimpleHighlightQuery query = new SimpleHighlightQuery();
//        HighlightOptions options = new HighlightOptions();
            SolrQuery query = new SolrQuery("item_keywords:" + keywords);
            //执行高亮显示的方法
            searchHighLightAndPage(resultMap, searchMap, query);
            //执行分组查询分类的的方法
            groupByKeywords(resultMap, keywords);
            //执行查询品牌和规格的方法
            searchBrandAndSpecList(resultMap, searchMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultMap;

    }

    /**
     * 商品审核后批量导入索引库
     *
     * @param itemList
     */
    @Override
    public void importItemList(List itemList) {
        System.out.println(itemList);
        solrTemplate.saveBeans(itemList);
        solrTemplate.commit();
    }

    @Override
    public void deleteByGoodsIds(List<Long> goodsId) {
        SimpleQuery query = new SimpleQuery();
        //添加删除条件在item_goodsId域里面的这些id
        Criteria criteria = new Criteria("item_goodsid").in(goodsId);
        query.addCriteria(criteria);
        solrTemplate.delete(query);
        solrTemplate.commit();

    }

    private void groupByKeywords(Map<String, Object> resultMap, String keywords) throws SolrServerException {
        //分类分组结果不需要分页
        SolrQuery query = new SolrQuery("item_keywords:" + keywords);
        //分组有多个分组在页面上显示也是有多个分类
        ArrayList<String> list = new ArrayList<>();
        //开启设置分组
        query.setParam("group", true);
        //设置分组的域在那里
        query.setParam("group.field", "item_category");
        //执行查询，拼接sql条件
        QueryResponse queryResponse = httpSolrServer.query(query);
        //执行分组查询
        //对查询后的条件在进行分组查询
        GroupResponse groupResponse = queryResponse.getGroupResponse();
        //获取分组后的查询列表
        List<GroupCommand> values = groupResponse.getValues();
        //遍历查询
        if (values != null) {
            for (GroupCommand groupCommand : values) {
                for (Group group : groupCommand.getValues()) {
                    SolrDocumentList solrDocuments = group.getResult();
                    for (SolrDocument solrDocument : solrDocuments) {
                        String category = (String) solrDocument.getFieldValue("item_category");
                        list.add(category);
                    }
                }
            }

        }
        //把分类列表添加到搜索的结果中
        resultMap.put("categoryList", list);

    }

    public void searchHighLightAndPage(Map<String, Object> searchResult, Map<String, Object> searchMap, SolrQuery query) throws SolrServerException {
        //分类过滤
        String category = (String) searchMap.get("category");
        if (StringUtils.isNotBlank(category)) {
            query.addFilterQuery("item_category:" + category);
        }
        //品牌过滤
        String brand = (String) searchMap.get("brand");
        if (StringUtils.isNotBlank(brand)) {
            query.addFilterQuery("item_brand:" + brand);
        }
        //规格过滤
        Map<String, String> spec = (Map<String, String>) searchMap.get("spec");
        if (spec != null && !spec.isEmpty()) {
            for (Map.Entry<String, String> entry : spec.entrySet()) {
                query.addFilterQuery("item_spec_" + entry.getKey() + ":" + entry.getValue());
            }
        }
        //价格区间过滤
        String item_price = (String) searchMap.get("price");
        if (StringUtils.isNotBlank(item_price)) {
            //1000-1500
            String[] split = item_price.split("-");
            query.addFilterQuery("item_price:[" + split[0] + " TO " + split[1] + "]");
        }
        //分页显示
        Integer pageNo = (Integer) searchMap.get("pageNo");
        Integer pageSize = (Integer) searchMap.get("pageSize");
        if (pageNo == null || pageNo < 1) {
            pageNo = 1;
        }
        if (pageSize == null) {
            pageSize = 20;
        }
        query.setStart((pageNo - 1) * pageSize);
        query.setRows(pageSize);
        //字段排序对应
        String sortField = (String) searchMap.get("sortField");
        String sort = (String) searchMap.get("sort");
        if (StringUtils.isNotBlank(sortField)&&StringUtils.isNotBlank(sort)) {
            query.addSort(sortField, "1".equals(sort)? SolrQuery.ORDER.asc: SolrQuery.ORDER.desc);
        }

        //设置高亮显示的样式
        //高亮显示
        query.setHighlight(true);
        //高亮显示的域
        query.addHighlightField("item_title");
        //高亮显示的前缀
        query.setHighlightSimplePre("<em style='color:red'>");
        //高亮显示的后缀
        query.setHighlightSimplePost("</em>");
//            //添加查询条件
//            Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
//            query.addCriteria(criteria);

        QueryResponse queryResponse = httpSolrServer.query(query);
        SolrDocumentList results = queryResponse.getResults();
        Long numFound = results.getNumFound();
        ArrayList<TbItem> itemList = new ArrayList<>();
        for (SolrDocument solrDocument : results) {
            TbItem item = new TbItem();
            String id = (String) solrDocument.get("id");
            String title = (String) solrDocument.get("item_title");
            Double price = (Double) solrDocument.get("item_price");
            String image = (String) solrDocument.get("item_image");
            String item_category = (String) solrDocument.get("item_category");
            String item_brand = (String) solrDocument.get("item_brand");
            //取高亮显示
            Map<String, Map<String, List<String>>> highlighting = queryResponse.getHighlighting();
            List<String> list = highlighting.get(solrDocument.get("id")).get("item_title");
            //判断是否有高亮内容
            if (null != list) {
                title = list.get(0);
            }


            item.setId(Long.parseLong(id));
            item.setTitle(title);
            item.setPrice(new BigDecimal(price));
            item.setImage(image);
            item.setCategory(item_category);
            item.setBrand(item_brand);

            itemList.add(item);
        }
        searchResult.put("total", numFound);
        searchResult.put("rows", itemList);
        //计算页数
        int totalPages = (int) Math.ceil(numFound * 1.0 / pageSize);
        searchResult.put("totalPages", totalPages);

    }

    /**
     * 查询品牌和规格列表
     *
     * @param resultMap
     * @return
     */
    private void searchBrandAndSpecList(Map<String, Object> resultMap, Map<String, Object> searchMap) {
        //判断规格显示，如果分类列表不为空的话就显示
        String category = (String) searchMap.get("category");
        if (StringUtils.isEmpty(category)) {
            //先查询根据分类分组的结果是否为空，每个分类对应一个模板id
            List<String> categoryList = (List<String>) resultMap.get("categoryList");
            if (categoryList != null && !categoryList.isEmpty()) {
                category = categoryList.get(0);
            }
        }
        Long typeId = (Long) redisTemplate.boundHashOps("itemCat").get(category);
        System.out.println(typeId);
        if (typeId != null) {
            // 根据模板ID查询品牌列表
            List brandList = (List) redisTemplate.boundHashOps("brandList").get(typeId);
            resultMap.put("brandList", brandList);// 返回值添加品牌列表
            // 根据模板ID查询规格列表
            List specList = (List) redisTemplate.boundHashOps("specList").get(typeId);
            resultMap.put("specList", specList);
        }


    }

}



