package com.pyg.search.ms;

import com.alibaba.fastjson.JSON;
import com.pinyougou.entityGroup.Goods;
import com.pinyougou.pojo.TbItem;
import com.pyg.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.JmsException;
import pyg.utils.CommonConstant;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.List;

public class ItemSearchListener implements MessageListener {
    @Autowired
    private ItemSearchService itemSearchService;
    @Override
    public void onMessage(Message message) {
        try {
            //先判断是添加还是删除
            String jmsType = message.getJMSType();
            TextMessage textMessage = (TextMessage) message;
            System.out.println("监听到消息:"+textMessage);
            if(CommonConstant.MSG_TYPE_UPDATE_INDEX.equals(jmsType)) {
                String itemList = textMessage.getStringProperty("itemList");
                List<TbItem> items = JSON.parseArray(itemList, TbItem.class);
                itemSearchService.importItemList(items);
                System.out.println("导入到solr索引库");
            }
            if (CommonConstant.MSG_TYPE_DELETE_INDEX.equals(jmsType)){
                List<Long> goodsIds = (List<Long>) textMessage.getObjectProperty("goodsIds");

                itemSearchService.deleteByGoodsIds(goodsIds);
                System.out.println("执行索引库删除");
            }
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
