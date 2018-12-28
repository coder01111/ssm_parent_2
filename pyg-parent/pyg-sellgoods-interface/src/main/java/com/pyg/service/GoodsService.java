package com.pyg.service;
import java.util.List;

import com.pinyougou.entity.PageResult;
import com.pinyougou.entityGroup.Goods;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbItem;

/**
 * 服务层接口
 * @author Administrator
 *
 */
public interface GoodsService {

	/**
	 * 返回全部列表
	 * @return
	 */
	public List<TbGoods> findAll();
	
	
	/**
	 * 返回分页列表
	 * @return
	 */
	public PageResult findPage(int pageNum, int pageSize);
	
	
	/**
	 * 增加
	*/
	public void add(Goods goods);
	
	
	/**
	 * 修改
	 */
	public void update(Goods goods);
	

	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	public Goods findOne(Long id);
	
	
	/**
	 * 批量删除
	 * @param ids
	 */
	public void delete(Long[] ids);

	/**
	 * 分页
	 * @param pageNum 当前页 码
	 * @param pageSize 每页记录数
	 * @return
	 */
	public PageResult findPage(TbGoods goods, int pageNum, int pageSize);

	/**
	 * 批量修改状态
	 * @param ids
	 * @param status
	 */
	public void updateStatus(Long []ids,String status);

	/**
	 *改变商品上下架状态的方法
	 * @param status
	 */
	public void updateMrketableStatus(Long id, String status);

	/**
	 * 查询所有已启用的所有的sku列表，批量查询然后修改添加到索引库中
	 * @param ids
	 * @return
	 */
	List<TbItem> findAuditedStatusItems(Long ids[]);
}
