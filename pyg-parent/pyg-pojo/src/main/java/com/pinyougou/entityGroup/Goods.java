package com.pinyougou.entityGroup;

import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbGoodsDesc;
import com.pinyougou.pojo.TbItem;

import java.io.Serializable;
import java.util.List;

/**
 * 定义一个新增商品的组合实体类因为商品有三张表保存是要用组合实体类
 */
public class Goods implements Serializable {
    private TbGoods tbGoods;        //spu商品抽象描述
    private TbGoodsDesc tbGoodsDesc;    //商品扩展信息描述
    private List<TbItem> itemList;//商品SKU列表，具体商品

    public TbGoods getTbGoods() {
        return tbGoods;
    }

    public void setTbGoods(TbGoods tbGoods) {
        this.tbGoods = tbGoods;
    }

    public TbGoodsDesc getTbGoodsDesc() {
        return tbGoodsDesc;
    }

    public void setTbGoodsDesc(TbGoodsDesc tbGoodsDesc) {
        this.tbGoodsDesc = tbGoodsDesc;
    }

    public List<TbItem> getItemList() {
        return itemList;
    }

    public void setItemList(List<TbItem> itemList) {
        this.itemList = itemList;
    }
}
