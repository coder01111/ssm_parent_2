package com.pyg.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.entity.Result;
import com.pinyougou.pojo.TbPayLog;
import com.pyg.order.service.OrderService;
import com.pyg.pay.service.WeiXinPayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pyg.utils.IdWorker;

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
    private OrderService orderService;

    /**
     * 需要传入参数订单号和订单金额，暂时写死，会返回一个我么需要的二维码链接
     *
     * @return
     */
    @RequestMapping("/createNative")
    public Map createNative() {
        //1.获取当前登录用户
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        //2.提取支付日志（从缓存 ）
        TbPayLog payLog = orderService.searchPayLogFromRedis(username);
//        //写死的交易流水号和订单总额不在写死动态获取从上层
//        IdWorker idworker = new IdWorker();
//        return weixinPayService.createNative(idworker.nextId() + "", "1");
        //3.调用微信支付接口
        if (payLog != null) {
            return weixinPayService.createNative(payLog.getOutTradeNo(), payLog.getTotalFee() + "");
        } else {
            return new HashMap();
        }

    }

    /**
     * 查询订单的状态，使用死循环不断查询状态，设施一个时间如果超过了二维码就超时了
     * @param out_trade_no
     * @return
     */
    @RequestMapping("/queryPayStatus")
    public Result queryPayStatus(String out_trade_no) {
        Result result = null;
        int x = 0;
        while (true){
            Map<String, String> map = weixinPayService.queryPayStatus(out_trade_no);//调用查询
            if (map==null){
                result = new Result(false, "支付发生错误");
                break;

            }
            if (map!=null&& map.get("trade_state").contains("SUCCESS")){
                result = new Result(true, "支付成功");
                //修改订单状态
                orderService.updateOrderStatus(out_trade_no, map.get("transaction_id"));//修改订单状态

                break;

            }
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            x++;
            if (x >= 20) {
                result = new Result(false, "二维码超时");
                break;
            }

        }
        return  result;
    }

}
