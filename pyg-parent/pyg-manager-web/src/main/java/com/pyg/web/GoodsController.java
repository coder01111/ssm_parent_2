package com.pyg.web;

import java.util.Arrays;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.pinyougou.entity.PageResult;
import com.pinyougou.entity.Result;
import com.pinyougou.entityGroup.Goods;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbItem;
import com.pyg.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.dubbo.config.annotation.Reference;
import pyg.utils.CommonConstant;

import javax.jms.*;

/**
 * controller
 *
 * @author Administrator
 */
@RestController
@Transactional
@RequestMapping("/goods")
public class GoodsController {

    @Reference
    private GoodsService goodsService;
    //引入搜索服务，导入索引库
//    @Reference(timeout = 100000)
//    private ItemSearchService searchService;
    @Autowired
    private JmsTemplate jmsTemplate;
    @Autowired
    private Destination queueSolrDestination;//用于导入solr索引库的消息目标（点对点）
    @Autowired
    private Destination topicPageDestination;//用于导入solr索引库的消息目标（点对点）

//    @Reference(timeout=40000)
//    private ItemPageService itemPageService;

    /**
     * 返回全部列表
     *
     * @return
     */
    @RequestMapping("/findAll")
    public List<TbGoods> findAll() {
        return goodsService.findAll();
    }


    /**
     * 返回全部列表
     *
     * @return
     */
    @RequestMapping("/findPage")
    public PageResult findPage(int page, int rows) {
        return goodsService.findPage(page, rows);
    }

    /**
     * 增加
     *
     * @param goods
     * @return
     */
    @RequestMapping("/add")
    public Result add(@RequestBody Goods goods) {
        try {
            goodsService.add(goods);
            return new Result(true, "增加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "增加失败");
        }
    }

    /**
     * 修改
     *
     * @param goods
     * @return
     */
    @RequestMapping("/update")
    public Result update(@RequestBody Goods goods) {
        try {
            goodsService.update(goods);
            return new Result(true, "修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "修改失败");
        }
    }

    /**
     * 获取实体
     *
     * @param id
     * @return
     */
    @RequestMapping("/findOne")
    public Goods findOne(Long id) {
        return goodsService.findOne(id);
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @RequestMapping("/delete")
    public Result delete(Long[] ids) {
        try {
            goodsService.delete(ids);
            //删除索引库
//            searchService.deleteByGoodsIds(Arrays.asList(ids));
            //发消息更新索引库
            jmsTemplate.send(queueSolrDestination, new MessageCreator() {
                @Override
                public Message createMessage(Session session) throws JMSException {
//                        return session.createTextMessage(jsonString);
                    //老师的做法
                    TextMessage textMessage = session.createTextMessage();
                    textMessage.setJMSType(CommonConstant.MSG_TYPE_DELETE_INDEX);
                    textMessage.setObjectProperty("goodsIds",  Arrays.asList(ids));
                    return textMessage;
                }
            });
            //删除每个服务器上的商品详细页
            jmsTemplate.send(topicPageDestination, new MessageCreator() {
                @Override
                public Message createMessage(Session session) throws JMSException {
                    ObjectMessage objectMessage = session.createObjectMessage();
                    objectMessage.setJMSType(CommonConstant.MSG_TPYE_DEL_PAGE);
                    objectMessage.setObjectProperty("goodsIds", Arrays.asList(ids));
                    return objectMessage;
                    //return session.createObjectMessage(ids);
                }
            });
            return new Result(true, "删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "删除失败");
        }


    }

    /**
     * 查询+分页
     *
     * @param goods
     * @param page
     * @param rows
     * @return
     */
    @RequestMapping("/search")
    public PageResult search(@RequestBody TbGoods goods, int page, int rows) {
        return goodsService.findPage(goods, page, rows);
    }
    /**
     * 更新状态
     * @param ids
     * @param status
     */
    @RequestMapping("/updateStatus")
    public Result updateStatus(Long[] ids, String status){
        Result result = new Result();
        try {
            goodsService.updateStatus(ids, status);
            result.setSuccess(true);
            result.setMessage("提交成功");
        } catch (Exception e) {
            e.printStackTrace();
            result.setMessage("提交失败");
        }
        //如果商品审核通过把对应的商品项添加到索引库
        try {
            //审核通过
            if ("1".equals(status)){
                List<TbItem> itemList = goodsService.findAuditedStatusItems(ids);
                //导入到索引库
                //    //得到需要导入的SKU列表
              final   String jsonString = JSON.toJSONString(itemList);
                //发消息更新索引库
                jmsTemplate.send(queueSolrDestination, new MessageCreator() {
                    @Override
                    public Message createMessage(Session session) throws JMSException {
//                        return session.createTextMessage(jsonString);
                        //老师的做法
                        TextMessage textMessage = session.createTextMessage();
                        textMessage.setJMSType(CommonConstant.MSG_TYPE_UPDATE_INDEX);
                        textMessage.setStringProperty("itemList", jsonString);
                        return textMessage;
                    }
                });
//                searchService.importItemList(itemList);
                //****生成商品详细页
//                    genHtml(goodsId);
                    jmsTemplate.send(topicPageDestination, new MessageCreator() {
                        @Override
                        public Message createMessage(Session session) throws JMSException {
//                            return session.createTextMessage(goodsId+"");
                            ObjectMessage objectMessage = session.createObjectMessage();
                            objectMessage.setJMSType(CommonConstant.MSG_TYPE_GEN_PAGE);
                            objectMessage.setObjectProperty("goodsIds", Arrays.asList(ids));
                            return objectMessage;
                        }
                    });


            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("更新索引库失败");

        }
        return result;
    }

    /**
     * 商品详情静态网页的java对象数据
     * @param goodsId
     */

//    @RequestMapping("/genHtml")
//    public void genHtml(Long goodsId){
//        itemPageService.genItemHtml(goodsId);
//    }
}
