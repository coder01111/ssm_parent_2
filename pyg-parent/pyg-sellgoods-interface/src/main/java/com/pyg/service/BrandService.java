package com.pyg.service;

import com.pinyougou.entity.PageResult;
import com.pinyougou.pojo.TbBrand;

import java.util.List;
import java.util.Map;

public interface BrandService {
    /**
     * 查询所有的方法
     * @return
     */
    public List<TbBrand> findAll();

    /**
     * 分页查询所有的方法
     * @param pageNum
     * @param pageSize
     * @return
     */
    public PageResult findPage(int pageNum,int pageSize);

    /**
     * 添加品牌的方法
     * @param tbBrand
     */
    void add(TbBrand tbBrand);

    /**
     * 查询品牌的方法
     * @param id
     * @return
     */
    TbBrand findOne(long id);

    /**
     * 修改品牌的方法
     * @param tbBrand
     */
    void  update(TbBrand tbBrand);

    /**
     * 删除方法
     * @param ids
     */
    void delete(long[] ids);

    /**
     * 按条件分页查询
     * @param tbBrand
     * @param pageNum
     * @param pageSize
     * @return
     */
    public PageResult findPage(TbBrand tbBrand, int pageNum,int pageSize);
    /**
     * 品牌下拉框数据
     */
    List<Map> selectOptionList();

}
