package com.pyg.seckill.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.entity.PageResult;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillGoodsExample;
import com.pyg.mapper.TbSeckillGoodsMapper;
import com.pyg.seckill.service.SeckillGoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Date;
import java.util.List;
@Service
public class SecKillGoodsServiceImpl implements SeckillGoodsService {
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private TbSeckillGoodsMapper seckillGoodsMapper;

    @Override
    public List<TbSeckillGoods> findList() {
        List<TbSeckillGoods> seckillGoodsList =	redisTemplate.boundHashOps("seckillGoods").values();
        //把每一个秒杀商品存入缓存中，是每一个按商品id存储
        if(seckillGoodsList==null || seckillGoodsList.size()==0){
            TbSeckillGoodsExample example=new TbSeckillGoodsExample();
            TbSeckillGoodsExample.Criteria criteria = example.createCriteria();
            criteria.andStatusEqualTo("1");// 审核通过的商品
            criteria.andStockCountGreaterThan(0);//库存数大于0
            criteria.andStartTimeLessThanOrEqualTo(new Date());//开始日期小于等于当前日期
            criteria.andEndTimeGreaterThanOrEqualTo(new Date());//截止日期大于等于当前日期
            seckillGoodsList = seckillGoodsMapper.selectByExample(example);
            //将列表数据装入缓存
            saveToRedis(seckillGoodsList);
        }else{
            System.out.println("从缓存中读取数据");

        }
        return seckillGoodsList;

    }
    private void saveToRedis(List<TbSeckillGoods> seckillGoodsList) {
        for(TbSeckillGoods seckillGoods:seckillGoodsList){
            redisTemplate.boundHashOps("seckillGoods").put(seckillGoods.getId(), seckillGoods);
        }
        System.out.println("从数据库中读取数据装入缓存");
    }

    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        return null;
    }

    @Override
    public void add(TbSeckillGoods seckillGoods) {

    }

    @Override
    public void update(TbSeckillGoods seckillGoods) {

    }

    @Override
    public TbSeckillGoods findOne(Long id) {
        return null;
    }

    @Override
    public void delete(Long[] ids) {

    }

    @Override
    public PageResult findPage(TbSeckillGoods seckillGoods, int pageNum, int pageSize) {
        return null;
    }

    @Override
    public TbSeckillGoods findOneFromRedis(Long id) {
        TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(id);
        return seckillGoods;
    }

}
