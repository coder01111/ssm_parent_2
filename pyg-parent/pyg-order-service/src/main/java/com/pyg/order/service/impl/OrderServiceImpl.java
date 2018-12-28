package com.pyg.order.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.pinyougou.entity.PageResult;
import com.pinyougou.entityGroup.Cart;
import com.pinyougou.pojo.TbOrder;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojo.TbPayLog;
import com.pyg.mapper.TbOrderItemMapper;
import com.pyg.mapper.TbPayLogMapper;
import com.pyg.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pyg.mapper.TbOrderMapper;
import com.pinyougou.pojo.TbOrderExample;
import com.pinyougou.pojo.TbOrderExample.Criteria;
import org.springframework.data.redis.core.RedisTemplate;
import pyg.utils.IdWorker;


/**
 * 服务实现层
 *
 * @author Administrator
 */
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private TbOrderMapper orderMapper;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private IdWorker idWorker;
    @Autowired
    private TbOrderItemMapper orderItemMapper;
    @Autowired
    private TbPayLogMapper payLogMapper;

    /**
     * 查询全部
     */
    @Override
    public List<TbOrder> findAll() {
        return orderMapper.selectByExample(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<TbOrder> page = (Page<TbOrder>) orderMapper.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 增加
     */
    @Override
    public void add(TbOrder order) {
        //1.从redis中提取购物车列表
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(order.getUserId());
        List<String> orderIdList=new ArrayList();//订单ID集合
        double total_money=0;//总金额

        //2.循环购物车取出购物车列表添加订单,每个购物车对象对应一条订单
        for (Cart cart : cartList) {
            //id用推特雪花算法生成一个不会重复的id，不是自增
            TbOrder tbOrder=new TbOrder();
            long orderId = idWorker.nextId();	//获取ID
            order.setOrderId(orderId);//id
            order.setPaymentType(order.getPaymentType());//支付类型
            order.setStatus("1");//未付款
            order.setCreateTime(new Date());//创建时间
            order.setUpdateTime(new Date());//更改时间
            order.setUserId(order.getUserId());//用户登录名
            order.setReceiverAreaName(order.getReceiverAreaName());//收货人地址
            order.setReceiverMobile(order.getReceiverMobile());//收货人电话
            order.setReceiver(order.getReceiver());//收货人
            order.setSourceType(order.getSourceType());//订单来源
            order.setSellerId(order.getSellerId());//商家ID
            double money=0;//合计数
            //循环购物车中每条明细记录，然后添加到tborderItem表中订单详细表
            for (TbOrderItem orderItem : cart.getOrderItemList()) {
                orderItem.setId(idWorker.nextId());//主键
                orderItem.setOrderId(orderId);//订单编号
                orderItemMapper.insert(orderItem);//先添加到购物车明细
                money+=orderItem.getTotalFee().doubleValue();//合计
            }
            order.setPayment(new BigDecimal(money));
            orderMapper.insert(order);//
            //添加所有的订单号到支付日志表中
            orderIdList.add(orderId+"");
            total_money+=money;

        }
        //添加支付日志
        if ("1".equals(order.getPaymentType())){
            //微信支付
            TbPayLog payLog=new TbPayLog();
            payLog.setOutTradeNo(idWorker.nextId()+"");//支付订单号，交易流水号
            payLog.setCreateTime(new Date());
            payLog.setUserId(order.getUserId());//用户ID,用逗号隔开每个订单的id
            payLog.setOrderList(orderIdList.toString().replace("[","").replace("]",""));
            payLog.setTotalFee( (long)( total_money*100)   );//金额（分）
            payLog.setTradeState("0");//交易状态
            payLog.setPayType("1");//微信
            payLogMapper.insert(payLog);
            //添加到缓存中以至于支付的时候快速的查询出来
            redisTemplate.boundHashOps("payLog").put(order.getUserId(),payLog);
        }
        //3.清除redis中的购物车
        redisTemplate.boundHashOps("cartList").delete(order.getUserId());

    }
        /**
         * 修改
         */
        @Override
        public void update(TbOrder order){
            orderMapper.updateByPrimaryKey(order);
        }

        /**
         * 根据ID获取实体
         * @param id
         * @return
         */
        @Override
        public TbOrder findOne (Long id){
            return orderMapper.selectByPrimaryKey(id);
        }

        /**
         * 批量删除
         */
        @Override
        public void delete (Long[]ids){
            for (Long id : ids) {
                orderMapper.deleteByPrimaryKey(id);
            }
        }


        @Override
        public PageResult findPage (TbOrder order,int pageNum, int pageSize){
            PageHelper.startPage(pageNum, pageSize);

            TbOrderExample example = new TbOrderExample();
            Criteria criteria = example.createCriteria();

            if (order != null) {
                if (order.getPaymentType() != null && order.getPaymentType().length() > 0) {
                    criteria.andPaymentTypeLike("%" + order.getPaymentType() + "%");
                }
                if (order.getPostFee() != null && order.getPostFee().length() > 0) {
                    criteria.andPostFeeLike("%" + order.getPostFee() + "%");
                }
                if (order.getStatus() != null && order.getStatus().length() > 0) {
                    criteria.andStatusLike("%" + order.getStatus() + "%");
                }
                if (order.getShippingName() != null && order.getShippingName().length() > 0) {
                    criteria.andShippingNameLike("%" + order.getShippingName() + "%");
                }
                if (order.getShippingCode() != null && order.getShippingCode().length() > 0) {
                    criteria.andShippingCodeLike("%" + order.getShippingCode() + "%");
                }
                if (order.getUserId() != null && order.getUserId().length() > 0) {
                    criteria.andUserIdLike("%" + order.getUserId() + "%");
                }
                if (order.getBuyerMessage() != null && order.getBuyerMessage().length() > 0) {
                    criteria.andBuyerMessageLike("%" + order.getBuyerMessage() + "%");
                }
                if (order.getBuyerNick() != null && order.getBuyerNick().length() > 0) {
                    criteria.andBuyerNickLike("%" + order.getBuyerNick() + "%");
                }
                if (order.getBuyerRate() != null && order.getBuyerRate().length() > 0) {
                    criteria.andBuyerRateLike("%" + order.getBuyerRate() + "%");
                }
                if (order.getReceiverAreaName() != null && order.getReceiverAreaName().length() > 0) {
                    criteria.andReceiverAreaNameLike("%" + order.getReceiverAreaName() + "%");
                }
                if (order.getReceiverMobile() != null && order.getReceiverMobile().length() > 0) {
                    criteria.andReceiverMobileLike("%" + order.getReceiverMobile() + "%");
                }
                if (order.getReceiverZipCode() != null && order.getReceiverZipCode().length() > 0) {
                    criteria.andReceiverZipCodeLike("%" + order.getReceiverZipCode() + "%");
                }
                if (order.getReceiver() != null && order.getReceiver().length() > 0) {
                    criteria.andReceiverLike("%" + order.getReceiver() + "%");
                }
                if (order.getInvoiceType() != null && order.getInvoiceType().length() > 0) {
                    criteria.andInvoiceTypeLike("%" + order.getInvoiceType() + "%");
                }
                if (order.getSourceType() != null && order.getSourceType().length() > 0) {
                    criteria.andSourceTypeLike("%" + order.getSourceType() + "%");
                }
                if (order.getSellerId() != null && order.getSellerId().length() > 0) {
                    criteria.andSellerIdLike("%" + order.getSellerId() + "%");
                }

            }

            Page<TbOrder> page = (Page<TbOrder>) orderMapper.selectByExample(example);
            return new PageResult(page.getTotal(), page.getResult());
        }

    @Override
    public TbPayLog searchPayLogFromRedis(String userId) {
        return (TbPayLog) redisTemplate.boundHashOps("payLog").get(userId);
    }

    @Override
    public void updateOrderStatus(String out_trade_no, String transaction_id) {
//        1. 修改支付日志状态
//        2.	修改关联的订单的状态
//        3.	清除缓存中的支付日志对象
        //1.修改支付日志的状态及相关字段
        TbPayLog payLog = payLogMapper.selectByPrimaryKey(out_trade_no);
        payLog.setPayTime(new Date());//支付时间
        payLog.setTradeState("1");//交易成功
        payLog.setTransactionId(transaction_id);//微信的交易流水号
        payLogMapper.updateByPrimaryKey(payLog);//修改
        //2.修改订单表的状态
        String orderList = payLog.getOrderList();// 订单ID 串
        String[] orderIds = orderList.split(",");
        //修改每一个订单状态
        for (String orderId : orderIds) {
            TbOrder order = orderMapper.selectByPrimaryKey(Long.valueOf(orderId));
            order.setStatus("2");//已付款状态
            order.setPaymentTime(new Date());//支付时间
            orderMapper.updateByPrimaryKey(order);

        }
    }

}
