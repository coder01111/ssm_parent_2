package com.pyg.cart.service;

import com.pinyougou.entityGroup.Cart;

import java.util.List;

public interface CartService {
    /**
     * 添加商品到购物车列表中
     */
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num);

    /**
     * 登录后存取到redis中,根据登录的用户名来判断是否是登录后的购物车明细还是未登录使用
     * 匿名登录的
     * @param username
     * @param cartList
     */
    public void saveCartListToRedis(String username,List<Cart> cartList);

    /**
     * 从缓存中提取购物车列表
     * @param username
     * @return
     */
    public List<Cart> findCartListFromRedis(String username);

    /**
     * 合并远程和本地也就是登录和未登录的购物车商品具体信息列表
     * @param cartList1
     * @param cartList2
     * @return
     */
    public List<Cart> mergeCartList(List<Cart> cartList1,List<Cart> cartList2);

}
