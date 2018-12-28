package com.pyg.search;

import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemExample;
import com.pyg.mapper.TbItemMapper;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.HighlightOptions;
import org.springframework.data.solr.core.query.SimpleHighlightQuery;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.result.HighlightEntry;
import org.springframework.data.solr.core.query.result.HighlightPage;
import org.springframework.data.solr.core.query.result.ScoredPage;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:spring/applicationContext-solr.xml","classpath:spring/applicationContext-dao.xml"})

public class TestTemplate {
    //加载配置文件放入spring容器中然后注入自动寻找
    @Autowired
    private SolrTemplate solrTemplate;
    @Autowired
    private TbItemMapper itemMapper;


    /**
     * 添加一条记录到索引库中
     */
    @Test
    public void add() {
        TbItem tbItem = new TbItem();
        tbItem.setId(1l);
        tbItem.setBrand("华为");
        tbItem.setCategory("手机");
        tbItem.setGoodsId(1L);
        tbItem.setSeller("华为2号专卖店");
        tbItem.setTitle("华为Mate9");
        tbItem.setPrice(new BigDecimal(2000));
        solrTemplate.saveBean(tbItem);
        solrTemplate.commit();
    }

    /**
     * 根据主键查询有一条索引记录
     */
    @Test
    public void testFindOne() {
        //使用原生的查询方法不用springDataSolr的封装模板
        HttpSolrServer solrServer = new HttpSolrServer("http://127.0.0.1:8080/solr");
        TbItem item = solrTemplate.getById("1", TbItem.class);
        System.out.println(item);

    }

    /**
     * 根据主键id删除一条主键记录
     */
    @Test
    public void testDelete() {
        solrTemplate.deleteById("1");
        solrTemplate.commit();
    }

    //删除索引库,谨慎使用
    @Test
    public void testDeleteAll() {

        solrTemplate.delete(new SimpleQuery("*:*"));
        solrTemplate.commit();
    }

    /**
     * 分页查询使用原生的slor查询方法不使用封装的spring框架
     */
    @Test
    public void testPageQuery() {
        SimpleQuery query = new SimpleQuery("*:*");
        query.setOffset(0);//开始索引
        query.setRows(10);//显示多少条每页
        ScoredPage<TbItem> items = solrTemplate.queryForPage(query, TbItem.class);
        System.out.println("总记录数：" + items.getTotalElements());
        //获取里面的内容
        List<TbItem> itemList = items.getContent();
        for (TbItem item : itemList) {
            System.out.println(item);
        }

    }

    /**
     * 使用原生的solr的HttpSolrServer进行查询,不进性封装里面是每一个文档就是封装饿数据；类型
     */
    @Test
    public void testPageQuery1() throws SolrServerException {
        HttpSolrServer solrServer = new HttpSolrServer("http://127.0.0.1:8080/solr");
        SolrQuery query = new SolrQuery("*:*");
        query.setStart(0);
        query.setRows(10);
        QueryResponse response = solrServer.query(query);
        List<TbItem> itemList = response.getBeans(TbItem.class);
        for (TbItem tbItem : itemList) {
            System.out.println(tbItem);
        }
//        SolrDocumentList solrDocuments = response.getResults();
//        for (SolrDocument solrDocument : solrDocuments) {
//            System.out.println(solrDocument);
//        }
    }

    /**
     * 条件查询
     */
    @Test
    public void testPageQueryMutil() {
        SimpleQuery query = new SimpleQuery("*:*");
        Criteria criteria = new Criteria("item_title").contains("2");
        criteria = criteria.and("item_title").contains("5");
        query.addCriteria(criteria);
//        query.setOffset(20);//开始索引（默认0）
//        query.setRows(20);//每页记录数(默认10)
        ScoredPage<TbItem> page = solrTemplate.queryForPage(query, TbItem.class);
        System.out.println("总记录数：" + page.getTotalElements());
        List<TbItem> list = page.getContent();
        for (TbItem item : list) {
            System.out.println(item);
        }

    }

    /**
     * 条件查询,不使用SpringData框架里面的内容,使用原生的
     */
    @Test
    public void testPageQueryMutil1() throws SolrServerException {
        HttpSolrServer solrServer = new HttpSolrServer("http://127.0.0.1:8080/solr");
        SolrQuery query = new SolrQuery("item_title:2");
        query.addFilterQuery("item_title:5");
//        query.setStart(20);
//        query.setRows(20);
        QueryResponse response = solrServer.query(query);
        SolrDocumentList list = response.getResults();
        long numFound = list.getNumFound();
        System.out.println("总记录数：" + numFound);
        for (SolrDocument document : list) {
            System.out.println(document);
        }
    }

    /**
     * 导入商品数据
     */
    @Test
    public void importItemData() {
        //获取审核通过的商品对应的商品项并且商品项的状态为启用状态
        TbItemExample itemExample = new TbItemExample();
        //itemExample.createCriteria().andGoodsIdIn(goodsId);//获取审核通过的商品对应的商品项
        itemExample.createCriteria().andStatusEqualTo("1");//商品项的状态为启用状态
        List<TbItem> items = itemMapper.selectByExample(itemExample);
        //SELECT COUNT(*) FROM tb_item;
        System.out.println("items--->"+items.size()+" ");

        for (TbItem item : items) {
            String spec = item.getSpec();
            Map map = JSON.parseObject(spec, Map.class);
            item.setSpecMap(map);
        }

        solrTemplate.saveBeans(items);
        solrTemplate.commit();
    }


    public Map<String, Object> search(Map<String, Object> searchMap) {
        Map<String, Object> map = new HashMap<String, Object>();
        /* Query query = new SimpleQuery(); */
        SimpleHighlightQuery query = new SimpleHighlightQuery();
        HighlightOptions hos = new HighlightOptions();
        hos.setSimplePostfix("</em>");
        hos.addField("item_title");
        hos.setSimplePrefix("<em style='color:red'>");
        query.setHighlightOptions(hos);

        // 添加查询条件
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);
        /* ScoredPage<TbItem> page = solrTemplate.queryForPage(query, TbItem.class); */
        HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query, TbItem.class);
        List<HighlightEntry<TbItem>> highlighted = page.getHighlighted();
        for (HighlightEntry<TbItem> h : highlighted) {
            TbItem item = h.getEntity();// 获取原实体类
            if (h.getHighlights().size() > 0 && h.getHighlights().get(0).getSnipplets().size() > 0) {
                item.setTitle(h.getHighlights().get(0).getSnipplets().get(0));// 设置高亮的结果
            }
        }
        map.put("rows", page.getContent());
        return map;
    }

}


