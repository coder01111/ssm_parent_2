package com.pyg.service.msg;

import com.pyg.page.service.ItemPageService;
import org.springframework.beans.factory.annotation.Autowired;
import pyg.utils.CommonConstant;

import javax.jms.*;
import java.util.List;

public class PageListener implements MessageListener {
    @Autowired
    private ItemPageService itemPageService;
    @Override
    public void onMessage(Message message) {
        System.out.println("接收到消息："+message);
        try {
            String jmsType = message.getJMSType();
            ObjectMessage objectMessage = (ObjectMessage) message;
            List<Long> goodsIds = (List<Long>) objectMessage.getObjectProperty("goodsIds");
            if(CommonConstant.MSG_TYPE_GEN_PAGE.equals(jmsType)){
                for (Long goodsId: goodsIds) {
                    itemPageService.genItemHtml(goodsId);
                }
                System.out.println("生成页面成功");
            } else if(CommonConstant.MSG_TPYE_DEL_PAGE.equals(jmsType)) {
                itemPageService.deleteItemHtml(goodsIds);
            }
        } catch (JMSException e) {
            e.printStackTrace();
            System.out.println("生成页面失败");
        }

    }
}
