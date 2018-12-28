//package com.pyg.test;
//
//import com.alibaba.fastjson.JSON;
//import com.pinyougou.pojo.TbGoods;
//import com.pinyougou.pojo.TbGoodsDesc;
//import com.pinyougou.pojo.TbItem;
//import com.pinyougou.pojo.TbItemExample;
//import com.pyg.mapper.TbGoodsDescMapper;
//import com.pyg.mapper.TbGoodsMapper;
//import com.pyg.mapper.TbItemCatMapper;
//import com.pyg.mapper.TbItemMapper;
//import freemarker.template.Template;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
//import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
//
//import java.io.File;
//import java.io.FileWriter;
//import java.io.Writer;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
///**
// * Created by crowndint on 2018/12/13.
// */
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(locations =
//        {"classpath:spring/applicationContext-service.xml",
//                "classpath:spring/applicationContext-dao.xml"})
//public class GeneratorItemTest {
//
//    @Autowired
//    private FreeMarkerConfigurer freeMarkerConfigurer;
//    @Autowired
//    private TbGoodsMapper goodsMapper;
//    @Autowired
//    private TbGoodsDescMapper goodsDescMapper;
//    @Autowired
//    private TbItemCatMapper itemCatMapper;
//    @Autowired
//    private TbItemMapper itemMapper;
//
//    @Test
//    public void generate() throws Exception {
//
//        Template template = freeMarkerConfigurer.getConfiguration().getTemplate("item.html");
//        //创建数据模型
//        Map dataModel = new HashMap<>();
//        //1.商品主表数据
//        Long goodsId = 149187842867968L;
//        TbGoods goods = goodsMapper.selectByPrimaryKey(goodsId);
//        dataModel.put("goods", goods);
//        //2.商品扩展表数据
//        TbGoodsDesc goodsDesc = goodsDescMapper.selectByPrimaryKey(goodsId);
//        dataModel.put("goodsDesc", goodsDesc);
//        //3.读取商品分类
//        String itemCat1 = itemCatMapper.selectByPrimaryKey(goods.getCategory1Id()).getName();
//        String itemCat2 = itemCatMapper.selectByPrimaryKey(goods.getCategory2Id()).getName();
//        String itemCat3 = itemCatMapper.selectByPrimaryKey(goods.getCategory3Id()).getName();
//        dataModel.put("itemCat1", itemCat1);
//        dataModel.put("itemCat2", itemCat2);
//        dataModel.put("itemCat3", itemCat3);
//
//        //4.读取SKU列表
//        TbItemExample example = new TbItemExample();
//        TbItemExample.Criteria criteria = example.createCriteria();
//        criteria.andGoodsIdEqualTo(goodsId);//SPU ID
//        criteria.andStatusEqualTo("1");//状态有效
//        example.setOrderByClause("is_default desc");
//        //按是否默认字段进行降序排序，目的是返回的结果第一条为默认SKU
//
//        List<TbItem> itemList = itemMapper.selectByExample(example);
//        dataModel.put("itemList", itemList);
//        System.out.printf(JSON.toJSONString(itemList));
//        Writer out = new FileWriter(new File("D:\\item", "item.html"));
//
//        template.process(dataModel, out);//输出
//        out.close();
//    }
//
//
//
//
//}
