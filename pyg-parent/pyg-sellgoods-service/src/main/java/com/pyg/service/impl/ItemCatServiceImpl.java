package com.pyg.service.impl;
import java.util.List;
import java.util.Map;

import com.pinyougou.entity.PageResult;
import com.pinyougou.pojo.TbItemCat;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pyg.mapper.TbItemCatMapper;
import com.pinyougou.pojo.TbItemCatExample;
import com.pinyougou.pojo.TbItemCatExample.Criteria;
import com.pyg.service.ItemCatService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;


/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service(timeout = 8000)
public class ItemCatServiceImpl implements ItemCatService {

	@Autowired
	private TbItemCatMapper itemCatMapper;
	@Autowired
	private RedisTemplate redisTemplate;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbItemCat> findAll() {
		return itemCatMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbItemCat> page=   (Page<TbItemCat>) itemCatMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbItemCat itemCat) {
		itemCatMapper.insert(itemCat);		
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbItemCat itemCat){
		itemCatMapper.updateByPrimaryKey(itemCat);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbItemCat findOne(Long id){
		return itemCatMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			itemCatMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbItemCat itemCat, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbItemCatExample example=new TbItemCatExample();
		Criteria criteria = example.createCriteria();
		
		if(itemCat!=null){			
						if(itemCat.getName()!=null && itemCat.getName().length()>0){
				criteria.andNameLike("%"+itemCat.getName()+"%");
			}
	
		}
		
		Page<TbItemCat> page= (Page<TbItemCat>)itemCatMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 根据父id查询
	 * @param parentId
	 * @return
	 */

    @Override
    public List<TbItemCat> findByParentId(Long parentId) {
		TbItemCatExample tbItemCatExample = new TbItemCatExample();
		//创建一个条件
        Criteria criteria = tbItemCatExample.createCriteria();
        //拼接sql语句
        criteria.andParentIdEqualTo(parentId);
        //每次执行查询的时候，一次性读取缓存进行存储 (因为每次增删改都要执行此方法)
        //缓存中的大key是自定义的模板，小key时分类名称，值是分类id因为要记住分类id
        List<TbItemCat> list = findAll();
        //遍历添加到缓存中
        for (TbItemCat itemCat : list) {
            redisTemplate.boundHashOps("itemCat").put(itemCat.getName(),itemCat.getTypeId());
        }
        System.out.println("更新缓存:商品分类表");
        //执行sql语句
        return itemCatMapper.selectByExample(tbItemCatExample);
    }

	@Override
	public List<Map> selectOptionList() {
		return itemCatMapper.selectOptionList();
	}

}
