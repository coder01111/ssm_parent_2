package com.pyg.service.impl;

import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbGoodsDesc;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemExample;
import com.pyg.mapper.TbGoodsDescMapper;
import com.pyg.mapper.TbGoodsMapper;
import com.pyg.mapper.TbItemCatMapper;
import com.pyg.mapper.TbItemMapper;
import com.pyg.page.service.ItemPageService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ItemPageServiceImpl implements ItemPageService {
    @Value("${pagedir}")
    private String pagedir;
    @Autowired
    private FreeMarkerConfig freeMarkerConfig;
    @Autowired
    private TbGoodsMapper goodsMapper;
    @Autowired
    private TbGoodsDescMapper goodsDescMapper;
    @Autowired
    private TbItemCatMapper itemCatMapper;
    @Autowired
    private TbItemMapper itemMapper;

    @Override
    public boolean genItemHtml(Long goodsId) {

        try {
            //创建一个configuration独享并指定模板
            Configuration configuration = freeMarkerConfig.getConfiguration();
            Template template = configuration.getTemplate("item.ftl");
            //创建一个framework的静态模型所需要的数据
            Map dataModel = new HashMap<>();
            //1.加载商品表数据
            TbGoods goods = goodsMapper.selectByPrimaryKey(goodsId);
            dataModel.put("goods", goods);
            //2.加载商品扩展表数据
            TbGoodsDesc goodsDesc = goodsDescMapper.selectByPrimaryKey(goodsId);
            dataModel.put("goodsDesc", goodsDesc);
            //加载商品分类
            if(goods.getCategory1Id()!=null&&goods.getCategory2Id()!=null&&goods.getCategory3Id()!=null){
                String category1 = itemCatMapper.selectByPrimaryKey(goods.getCategory1Id()).getName();
                String category2 = itemCatMapper.selectByPrimaryKey(goods.getCategory2Id()).getName();
                String category3 = itemCatMapper.selectByPrimaryKey(goods.getCategory3Id()).getName();
                dataModel.put("category1", category1);
                dataModel.put("category2", category2);
                dataModel.put("category3", category3);
            }

            //读取sku列表
            TbItemExample example = new TbItemExample();
            TbItemExample.Criteria criteria = example.createCriteria();
            criteria.andGoodsIdEqualTo(goodsId);//SPU ID
            criteria.andStatusEqualTo("1");//状态有效
            example.setOrderByClause("is_default desc");
            //按是否默认字段进行降序排序，目的是返回的结果第一条为默认SKU
            List<TbItem> itemList = itemMapper.selectByExample(example);
            dataModel.put("itemList", itemList);
            //创建一个输出流模板输出位置
            String fileName=pagedir + goodsId + ".html";
//            Writer out = new FileWriter();
            Writer out = new OutputStreamWriter(new FileOutputStream(fileName), "UTF-8");
            //加载数据模型到模板中
            template.process(dataModel, out);
            out.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;

        }
    }

    /**
     *
     * @param goodsIds
     * @return
     */
    @Override
    public boolean deleteItemHtml(List<Long> goodsIds) {
        try {
            for (Long goodsId : goodsIds) {
                new File(pagedir + goodsId + ".html").delete();

            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
