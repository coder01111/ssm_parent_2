package com.pyg.service.impl;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.pinyougou.entity.PageResult;
import com.pinyougou.entityGroup.Goods;
import com.pinyougou.pojo.*;
import com.pyg.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.pojo.TbGoodsExample.Criteria;
import com.pyg.service.GoodsService;
import org.springframework.transaction.annotation.Transactional;


/**
 * 服务实现层
 *
 * @author Administrator
 */
@Service(timeout = 8000)
@Transactional
public class GoodsServiceImpl implements GoodsService {

    @Autowired
    private TbGoodsMapper goodsMapper;
    @Autowired
    private TbGoodsDescMapper goodsDescMapper;
    @Autowired
    private TbItemCatMapper itemCatMapper;
    @Autowired
    private TbBrandMapper brandMapper;
    @Autowired
    private TbSellerMapper sellerMapper;
    @Autowired
    private TbItemMapper itemMapper;


    /**
     * 查询全部
     */
    @Override
    public List<TbGoods> findAll() {
        return goodsMapper.selectByExample(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<TbGoods> page = (Page<TbGoods>) goodsMapper.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 增加
     */
    @Override
    public void add(Goods goods) {
        //设置新增商品状态为审核
        goods.getTbGoods().setAuditStatus("0");
        //保存spu
        goodsMapper.insert(goods.getTbGoods());
        //获取刚才插入Tb_goods表中的主键自增长的
        goods.getTbGoodsDesc().setGoodsId(goods.getTbGoods().getId());
        //保存到扩展信息表
        goodsDescMapper.insert(goods.getTbGoodsDesc());
        //插入之前先判断是否启用规格
        if ("1".equals(goods.getTbGoods().getIsEnableSpec())) {
            for (TbItem item : goods.getItemList()) {
                setItemVaklues(goods, item);
                itemMapper.insert(item);
            }

        } else {
            for (TbItem item : goods.getItemList()) {
                item.setPrice(goods.getTbGoods().getPrice());
                setItemVaklues(goods, item);
                itemMapper.insert(item);
            }
        }


    }

    //定义一个给sku属性赋值的私有方法封装
    private void setItemVaklues(Goods goods, TbItem tbItem) {
        //title是商品名称和他的每一个sku里面的spec拼接组成,定义一个titile
        String title = goods.getTbGoods().getGoodsName();
        //设置goods_id
        tbItem.setGoodsId(goods.getTbGoods().getId());
        //遍历每一个sku，给他的的成员变量赋值
        //给title赋值，
        //需要先把每个sku列表里面的spec的字符串转换为map对象然后遍历取出里面的值
        //{"机身内存":"16G","网络":"双卡"}
        //先判断规格是否为空
        if (tbItem.getSpec() != null && !tbItem.getSpec().isEmpty()) {
            Map<String, Object> map = JSON.parseObject((tbItem.getSpec()), Map.class);
            for (String s : map.keySet()) {
                //拼接上每一个map的值
                title += "" + map.get(s);
            }
        }
        tbItem.setTitle(title);
        //给分类id设置值,就是第三级分类的id
        tbItem.setCategoryid(goods.getTbGoods().getCategory3Id());
        //还要设置分类的中文名称，根据分类的id去分类表查询分类的名称
        TbItemCat tbItemCat = itemCatMapper.selectByPrimaryKey(tbItem.getCategoryid());
        tbItem.setCategory(tbItemCat.getName());
        //设置品牌名称
        tbItem.setBrand(brandMapper.selectByPrimaryKey(goods.getTbGoods().getBrandId()).getName());
        //设置商家id和商家店铺名称,在调用这个方法之前以及获取用户名然后赋值进去
        tbItem.setSellerId(goods.getTbGoods().getSellerId());
        tbItem.setSeller(sellerMapper.selectByPrimaryKey(tbItem.getSellerId()).getNickName());
        //获取图片，先把扩展商品表中的图片属性集合字符串转换为一个list集合里面是每一个map对象
        //[{"color":"红色","url":"http://192.168.25.133/group1/M00/00/01/wKgZhVmHINKADo__AAjlKdWCzvg874.jpg"},{"color":"黑色","url":"http://192.168.25.133/group1/M00/00/01/wKgZhVmHINyAQAXHAAgawLS1G5Y136.jpg"}]
        List<Map> itemImages = JSON.parseArray(goods.getTbGoodsDesc().getItemImages(), Map.class);
        //遍历这个集合取出第一张图片作为sku的图片属性
        //先判断这个集合是否为空
        if (itemImages != null && itemImages.size() > 0) {
            String url = (String) itemImages.get(0).get("url");
            tbItem.setImage(url);
        }
        tbItem.setCreateTime(new Date());
        tbItem.setUpdateTime(new Date());

    }

    /**
     * 修改
     */
    @Override
    public void update(Goods goods) {
        goods.getTbGoods().setAuditStatus("0");//设置未申请状态:如果是经过修改的商品，需要重新设置状态
        goodsMapper.updateByPrimaryKey(goods.getTbGoods());//保存商品表
        goodsDescMapper.updateByPrimaryKey(goods.getTbGoodsDesc());//保存商品扩展表
        //删除原有的sku列表数据
        TbItemExample example = new TbItemExample();
        com.pinyougou.pojo.TbItemExample.Criteria criteria = example.createCriteria();
        criteria.andGoodsIdEqualTo(goods.getTbGoods().getId());
        itemMapper.deleteByExample(example);
//添加新的sku列表数据
        saveItemList(goods);//插入商品SKU列表数据

    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public Goods findOne(Long id) {
        //创建一个组合实体类
        Goods goods = new Goods();
        goods.setTbGoods(goodsMapper.selectByPrimaryKey(id));
        goods.setTbGoodsDesc(goodsDescMapper.selectByPrimaryKey(id));
        //查询sku列表根据商品id查询
        TbItemExample tbItemExample = new TbItemExample();
        TbItemExample.Criteria criteria = tbItemExample.createCriteria();
        criteria.andGoodsIdEqualTo(id);
        goods.setItemList(itemMapper.selectByExample(tbItemExample));
        return goods;
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            TbGoods goods = goodsMapper.selectByPrimaryKey(id);
            goods.setIsDelete("1");//代表已删除
            goodsMapper.updateByPrimaryKey(goods);
        }
    }


    @Override
    public PageResult findPage(TbGoods goods, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        TbGoodsExample example = new TbGoodsExample();
        Criteria criteria = example.createCriteria();

        if (goods != null) {
            if (goods.getSellerId() != null && goods.getSellerId().length() > 0) {
                criteria.andSellerIdEqualTo(goods.getSellerId());
            }
            if (goods.getGoodsName() != null && goods.getGoodsName().length() > 0) {
                criteria.andGoodsNameLike("%" + goods.getGoodsName() + "%");
            }
            if (goods.getAuditStatus() != null && goods.getAuditStatus().length() > 0) {
                criteria.andAuditStatusLike("%" + goods.getAuditStatus() + "%");
            }
            if (goods.getIsMarketable() != null && goods.getIsMarketable().length() > 0) {
                criteria.andIsMarketableLike("%" + goods.getIsMarketable() + "%");
            }
            if (goods.getCaption() != null && goods.getCaption().length() > 0) {
                criteria.andCaptionLike("%" + goods.getCaption() + "%");
            }
            if (goods.getSmallPic() != null && goods.getSmallPic().length() > 0) {
                criteria.andSmallPicLike("%" + goods.getSmallPic() + "%");
            }
            if (goods.getIsEnableSpec() != null && goods.getIsEnableSpec().length() > 0) {
                criteria.andIsEnableSpecLike("%" + goods.getIsEnableSpec() + "%");
            }
            if (goods.getIsDelete() != null && goods.getIsDelete().length() > 0) {
                criteria.andIsDeleteLike("%" + goods.getIsDelete() + "%");
            }
            criteria.andIsDeleteIsNull();//非删除状态,排除以删除的

        }

        Page<TbGoods> page = (Page<TbGoods>) goodsMapper.selectByExample(example);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 改变商品的审核状态
     * @param ids
     * @param status
     */
    @Override
    public void updateStatus(Long[] ids, String status) {
        //遍历id数组
        for (Long id : ids) {
            TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
            tbGoods.setAuditStatus(status);
            goodsMapper.updateByPrimaryKey(tbGoods);
        }
    }

    /**
     * 改变商品上下架的状态
     * @param id
     * @param status
     */
    @Override
    public void updateMrketableStatus(Long id, String status) {
        TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
        tbGoods.setIsMarketable(status);
        goodsMapper.updateByPrimaryKey(tbGoods);
    }

    /**
     * 根据商品的spuid查询所有的sku列表
     * @param ids
     * @return
     */
    @Override
    public List<TbItem> findAuditedStatusItems(Long[] ids) {

        //根据商品的spuID查询到所有的sku列表，审核通过后添加到索引库中
        TbItemExample tbItemExample = new TbItemExample();
        TbItemExample.Criteria criteria = tbItemExample.createCriteria();
        //根据商品的
        criteria.andGoodsIdIn(Arrays.asList(ids));
        criteria.andStatusEqualTo("1");
        List<TbItem> itemList = itemMapper.selectByExample(tbItemExample);
        return itemList;

    }

    /**
     * 保存sku列表
     * @param goods
     */
    private void saveItemList(Goods goods) {
        //插入之前先判断是否启用规格
        if ("1".equals(goods.getTbGoods().getIsEnableSpec())) {
            for (TbItem item : goods.getItemList()) {
                setItemVaklues(goods, item);
                itemMapper.insert(item);
            }

        } else {
            for (TbItem item : goods.getItemList()) {
                item.setPrice(goods.getTbGoods().getPrice());
                setItemVaklues(goods, item);
                itemMapper.insert(item);
            }
        }
    }
}
