package com.pyg.seckill.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.entity.PageResult;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pyg.mapper.TbSeckillGoodsMapper;
import com.pyg.mapper.TbSeckillOrderMapper;
import com.pyg.seckill.service.SeckillOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import pyg.utils.IdWorker;

import java.util.Date;
import java.util.List;

@Service
public class SecKillOrderServiceImpl implements SeckillOrderService {
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private TbSeckillGoodsMapper seckillGoodsMapper;
    @Autowired
    private TbSeckillOrderMapper seckillOrderMapper;
    @Autowired
    private IdWorker idWorker;

    @Override
    public List<TbSeckillOrder> findAll() {
        return null;
    }

    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        return null;
    }

    @Override
    public void add(TbSeckillOrder seckillOrder) {

    }

    @Override
    public void update(TbSeckillOrder seckillOrder) {

    }

    @Override
    public TbSeckillOrder findOne(Long id) {
        return null;
    }

    @Override
    public void delete(Long[] ids) {

    }

    @Override
    public PageResult findPage(TbSeckillOrder seckillOrder, int pageNum, int pageSize) {
        return null;
    }

    /**
     * 秒杀商品提交订单
     *
     * @param seckillId
     * @param userId
     */
    @Override
    public void submitOrder(Long seckillId, String userId) {
        //先查询缓存中的商品
        TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(seckillId);
        if (seckillGoods == null) {
            //说明缓存中没有该商品，秒杀是吧所有可以秒杀的商品列表放在缓存中，每次下单库存减少
            throw new RuntimeException("该商品不存在");
        }
        if (seckillGoods.getStockCount() <= 0) {
            //说明缓存中没有该商品，秒杀是吧所有可以秒杀的商品列表放在缓存中，每次下单库存减少
            throw new RuntimeException("商品已经被抢光");
        }
        //2.减少库存，也是在缓存中减少库存
        seckillGoods.setStockCount(seckillGoods.getStockCount() - 1);
        //存入缓存中
        redisTemplate.boundHashOps("seckillGoods").put(seckillId, seckillGoods);
        //如果库存为0说明商品已被抢光,修改数据库删除缓存
        if (seckillGoods.getStockCount() == 0) {
            seckillGoodsMapper.updateByPrimaryKey(seckillGoods);   //更新数据库
            redisTemplate.boundHashOps("seckillGoods").delete(seckillId);
            System.out.println("商品同步到数据库...");
        }
        //3.存储秒杀订单 (不向数据库存 ,只向缓存中存储 )，因为为了快速的查询出来，支付后才保存到数据库
        TbSeckillOrder seckillOrder = new TbSeckillOrder();
        seckillOrder.setId(idWorker.nextId());//秒杀主键id
        seckillOrder.setSeckillId(seckillId);//秒杀商品id
        seckillOrder.setMoney(seckillGoods.getCostPrice());//秒杀价格
        seckillOrder.setUserId(userId);//用户id
        seckillOrder.setSellerId(seckillGoods.getSellerId());//商家ID
        seckillOrder.setCreateTime(new Date());//订单创建时间
        seckillOrder.setStatus("0");//状态
        //存入缓存中，根据用户名来存储订单列表
        redisTemplate.boundHashOps("seckillOrder").put(userId, seckillOrder);
        System.out.println("保存订单成功(redis)");

    }

    /**
     * 从缓存中查询秒杀商品订单
     *
     * @param userId
     * @return
     */
    @Override
    public TbSeckillOrder searchOrderFromRedisByUserId(String userId) {
        return (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
    }

    //支付成功后保存订单
    @Override
    public void saveOrderFromRedisToDb(String userId, Long orderId, String transactionId) {
        //从缓存中读取商品订单
        TbSeckillOrder seckillOrder = searchOrderFromRedisByUserId(userId);
        if (seckillOrder == null) {
            throw new RuntimeException("不存在订单");
        }
        if (seckillOrder.getId().longValue() != orderId.longValue()) {
            throw new RuntimeException("订单号不符");
        }
        //2.修改订单实体的属性
        seckillOrder.setPayTime(new Date());//支付日期
        seckillOrder.setStatus("1");//已支付 状态
        seckillOrder.setTransactionId(transactionId);
        //3.将订单存入数据库
        seckillOrderMapper.insert(seckillOrder);
        //4.清除缓存中的订单
        redisTemplate.boundHashOps("seckillOrder").delete(userId);

    }

    //订单五分钟未支付应该删除缓存回复库存
    @Override
    public void deleteOrderFromRedis(String userId, Long orderId) {
        //1.查询出缓存中的订单
        TbSeckillOrder seckillOrder = searchOrderFromRedisByUserId(userId);
        if (seckillOrder != null) {

            //2.删除缓存中的订单
            redisTemplate.boundHashOps("seckillOrder").delete(userId);
            //3.库存回退
            TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(seckillOrder.getSeckillId());
            if (seckillGoods != null) {
                //缓存中还有
                seckillGoods.setStockCount(seckillGoods.getStockCount() + 1);
                redisTemplate.boundHashOps("seckillGoods").put(seckillOrder.getSeckillId(), seckillGoods);

            } else { //说明商品被抢完了 //应该重新从数据库获取并恢复库存为1，最后一件
                seckillGoods = seckillGoodsMapper.selectByPrimaryKey(seckillOrder.getSeckillId());
                seckillGoods.setStockCount(1);//数量为1
                redisTemplate.boundHashOps("seckillGoods").put(seckillOrder.getSeckillId(), seckillGoods);

            }
            System.out.println("订单取消：" + orderId);
        }

    }
}
