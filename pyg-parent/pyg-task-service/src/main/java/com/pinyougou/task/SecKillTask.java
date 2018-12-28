package com.pinyougou.task;

import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillGoodsExample;
import com.pyg.mapper.TbSeckillGoodsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Component
public class SecKillTask {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private TbSeckillGoodsMapper seckillGoodsMapper;

    /**
     * 刷新秒杀商品
     */
    @Scheduled(cron = "0 * * * * ?")
    public void refreshSeckillGoods() {
        System.out.println("执行了秒杀商品增量更新 任务调度" + new Date());

        //查询缓存中的秒杀商品ID集合
        List goodsIdList = new ArrayList(redisTemplate.boundHashOps("seckillGoods").keys());
        System.out.println(goodsIdList);

        TbSeckillGoodsExample example = new TbSeckillGoodsExample();
        TbSeckillGoodsExample.Criteria criteria = example.createCriteria();
        criteria.andStatusEqualTo("1");
        criteria.andStockCountGreaterThan(0);//库存数大于0
        criteria.andStartTimeLessThanOrEqualTo(new Date());//开始日期小于等于当前日期
        criteria.andEndTimeGreaterThanOrEqualTo(new Date());//截止日期大于等于当前日期
        //判断缓存中是否有商品
        if (goodsIdList.size() > 0) {
            //缓存中如果有商品拍出缓存中的商品
            criteria.andIdNotIn(goodsIdList);//排除缓存中已经存在的商品，不能再新增
        }
        List<TbSeckillGoods> tbSeckillGoods = seckillGoodsMapper.selectByExample(example);
        //将列表数据装入缓存
        for (TbSeckillGoods tbSeckillGood : tbSeckillGoods) {
            redisTemplate.boundHashOps("seckillGoods").put(tbSeckillGood.getId(), tbSeckillGood);
            System.out.println("增量更新秒杀商品ID:" + tbSeckillGood.getId());
        }
        System.out.println("end");

    }

    /**
     * 时间到了还没有卖完要从缓存中移除该商品
     */
    @Scheduled(cron = "* * * * * ?")
    public void removeSeckillGoods() {
        //查询出缓存中的数据，扫描每条记录，判断时间，如果当前时间超过了截止时间，移除此记录
        List<TbSeckillGoods> seckillGoodsList = redisTemplate.boundHashOps("seckillGoods").values();
        System.out.println("执行了清除秒杀商品的任务" + new Date());
        //遍历判断是否含有超出时间的
        for (TbSeckillGoods tbSeckillGoods : seckillGoodsList) {
            if (tbSeckillGoods.getEndTime().getTime()<new Date().getTime()){
                //同步到数据库
                seckillGoodsMapper.updateByPrimaryKey(tbSeckillGoods);
                //从缓存中删除
                redisTemplate.boundHashOps("seckillGoods").delete(tbSeckillGoods.getId());
                System.out.println("秒杀商品" + tbSeckillGoods.getId() + "已过期");

            }
        }
        System.out.println("执行了清除秒杀商品的任务...end");
    }

}
