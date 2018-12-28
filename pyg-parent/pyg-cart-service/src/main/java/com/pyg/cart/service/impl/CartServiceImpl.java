package com.pyg.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.entityGroup.Cart;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbOrderItem;
import com.pyg.cart.service.CartService;
import com.pyg.mapper.TbItemMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class    CartServiceImpl implements CartService {
    @Autowired
    private TbItemMapper itemMapper;

    /**
     * 添加商品到购物车列表中
     *
     * @param cartList
     * @param itemId
     * @param num
     * @return
     */
    @Override
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) {
//        服务实现思路
        //1.根据商品SKU ID查询SKU商品信息
        TbItem item = itemMapper.selectByPrimaryKey(itemId);
        //判断商品是否存在，因为可能在一瞬间商品被删除了减少代码bug
        if (item == null) {
            throw new RuntimeException("商品不存在");
        }
        if (!item.getStatus().equals("1")) {
            throw new RuntimeException("商品状态无效");
        }
        //2.获取商家ID
        String sellerId = item.getSellerId();
        //3.根据商家ID判断购物车列表中是否存在该商家的购物车
        Cart cart = searchCartBySellerId(cartList, sellerId);
        //4.如果购物车列表中不存在该商家的购物车
        if (cart == null) {
            //4.1 新建购物车对象
            cart = new Cart();
            //4.2 将新建的购物车对象添加到购物车列表
            cart.setSellerId(sellerId);
            cart.setSellerName(item.getSeller());
            //创建购物车商品明细列表
            TbOrderItem orderItem = createOrderItem(item, num);
            //创建一个集合来存放商品信息的集合
            List orderItemList = new ArrayList();
            orderItemList.add(orderItem);
            cart.setOrderItemList(orderItemList);//设置购物车明细列表
            cartList.add(cart);
        } else {
            //5.如果购物车列表中存在该商家的购物车
            // 查询购物车明细列表中是否存在该商品
            TbOrderItem orderItem =
                    searchOrderItemByItemId(cart.getOrderItemList(), itemId);
            if (orderItem == null) {

                //5.1. 如果没有，新增购物车明细
                orderItem = createOrderItem(item, num);
                //并且给购物车设置明细列表
                cart.getOrderItemList().add(orderItem);
            } else {
                //5.2. 如果有，在原购物车明细上添加数量，更改金额
                orderItem.setNum(orderItem.getNum() + num);
                orderItem.setTotalFee(new BigDecimal(orderItem.getNum() * orderItem.getPrice().doubleValue()));
                //如果数量操作后小于等于0，则移除
                if (orderItem.getNum() <= 0) {
                    cart.getOrderItemList().remove(orderItem);
                }
                //如果移除后cart的明细数量为0，则将cart移除
                if (cart.getOrderItemList().size() <= 0) {
                    cartList.remove(cart);
                }
            }


        }

        return cartList;
    }

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 登录成功后把远程的购物车明细添加到缓存中
     *
     * @param username
     * @param cartList
     */
    @Override
    public void saveCartListToRedis(String username, List<Cart> cartList) {
        System.out.println("向redis中存入购物车"+username);
        redisTemplate.boundHashOps("cartList").put(username,cartList);


    }

    @Override
    public List<Cart> findCartListFromRedis(String username) {
        System.out.println("从redis中提取购物车"+username);
        //从缓存中取出
        List<Cart>  cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(username);
        //判断是否非空
        if(cartList==null){
            cartList=new ArrayList();
        }
        return cartList;


    }

    @Override
    public List<Cart> mergeCartList(List<Cart> cartList1, List<Cart> cartList2) {
        // cartList1.addAll(cartList2);  不能简单合并
        //遍历其中一个
        for (Cart cart : cartList2) {
            //得到里面的具体购物车商品信息列表
            for (TbOrderItem orderItem : cart.getOrderItemList()) {
                cartList1=addGoodsToCartList(cartList1,orderItem.getItemId(),orderItem.getNum());
            }
        }
        return  cartList1;
    }

    /**
     * 判断从购物车明细列表中是否含有这个商品了如果有直接添加数量否者创建一个
     *
     * @param orderItemList
     * @param itemId
     * @return
     */
    private TbOrderItem searchOrderItemByItemId(List<TbOrderItem> orderItemList, Long itemId) {
        for (TbOrderItem orderItem : orderItemList) {
            if (orderItem.getItemId().longValue() == itemId.longValue()) {
                //说明存在
                return orderItem;
            }
        }
        return null;
    }

    /**
     * 创建一个购物车明细列表并根据商家id查询到的信息封装到明细列表
     *
     * @param item
     * @param num
     * @return
     */
    private TbOrderItem createOrderItem(TbItem item, Integer num) {
        //判断优化数量不能为0
        if (num <= 0) {
            throw new RuntimeException("数量非法");
        }


        TbOrderItem orderItem = new TbOrderItem();
        orderItem.setItemId(item.getId());//设置sku的id
        orderItem.setGoodsId(item.getGoodsId());//设置spu id
        orderItem.setTitle(item.getTitle());  //设置sku商品标题
        orderItem.setPrice(item.getPrice());//设置商品价格
        orderItem.setNum(num);  //设置商品的数量
        orderItem.setPicPath(item.getImage()); //设置商品的图片信息
        //需要转换类型
        orderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue() * num)); //设置购物车总费用
        orderItem.setSellerId(item.getSellerId());  //设置商品的商家id
        return orderItem;
    }

    /**
     * 从购物车列表中判断是否有商品信息，如果有返回购物车对象
     *
     * @param cartList
     * @param sellerId
     * @return
     */
    private Cart searchCartBySellerId(List<Cart> cartList, String sellerId) {
        //1.0先遍历这个购物车列表
        for (Cart cart : cartList) {
            //从每个购物车信息中判断是否是商家信息
            if (cart.getSellerId().equals(sellerId)) {
                //相等的话就说明这个列表中有这个商家的信息直接返回这个对象
                return cart;
            }
        }
        //如果没有则说明购物车对象中没有这个信息
        return null;
    }
}
