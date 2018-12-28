package com.pyg.seckill.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.entity.Result;
import com.pinyougou.pojo.TbPayLog;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pyg.pay.service.WeiXinPayService;
import com.pyg.seckill.service.SeckillOrderService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 生辰二维码控制层，引入微信生成的服务接口
 */
@RestController
@RequestMapping("/pay")
public class PayController {
    @Reference(timeout = 5000)
    private WeiXinPayService weixinPayService;
    @Reference(timeout = 8000)
    private SeckillOrderService seckillOrderService;

    /**
     * 需要传入参数订单号和订单金额，暂时写死，会返回一个我么需要的二维码链接
     *
     * @return
     */
    @RequestMapping("/createNative")
    public Map createNative() {
        //1.获取当前登录用户
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        //2.提取秒杀订单（从缓存 ）
        TbSeckillOrder seckillOrder = seckillOrderService.searchOrderFromRedisByUserId(username);
        //3.调用微信支付接口
        if (seckillOrder != null) {
            return weixinPayService.createNative(seckillOrder.getId() + "",
                    (long) (seckillOrder.getMoney().doubleValue() * 100) + "");
        } else {
            return new HashMap<>();
        }
    }


    /**
     * 查询订单的状态，使用死循环不断查询状态，设施一个时间如果超过了二维码就超时了
     *
     * @param out_trade_no
     * @return
     */
    @RequestMapping("/queryPayStatus")
    public Result queryPayStatus(String out_trade_no) {
        //1.获取当前登录用户
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        int x = 0;
        Result result = null;

        while (true) {
            Map<String, String> map = weixinPayService.queryPayStatus(out_trade_no);//调用查询
            if (map == null) {
                result = new Result(false, "支付发生错误");
                break;
            }
            if (map.get("trade_state").equals("SUCCESS")) {//支付成功
                result = new Result(true, "支付成功");
                //保存订单
                seckillOrderService.saveOrderFromRedisToDb(username, Long.valueOf(out_trade_no), map.get("transaction_id"));
                break;
            }
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            x++;
            System.out.println(x);
            if (x >= 20) {
                result = new Result(false, "二维码超时");
                //关闭制服订单
                Map payResult = weixinPayService.closePay(out_trade_no);
                if (payResult != null && "FAIL".equals(payResult.get("return_code"))) {
                    if ("ORDERPAID".equals(payResult.get("error_code"))) {
                        result = new Result(true, "支付成功");
                        //在超时那一瞬间完成了支付也要保存订单
                        seckillOrderService.saveOrderFromRedisToDb(username, Long.valueOf(out_trade_no), map.get("transaction_id"));
                    } //删除订单
                    if (result.isSuccess() == false) {
                        seckillOrderService.deleteOrderFromRedis(username, Long.valueOf(out_trade_no));
                    }
                    break;

                }
            }
        }
        return result;

    }

}
