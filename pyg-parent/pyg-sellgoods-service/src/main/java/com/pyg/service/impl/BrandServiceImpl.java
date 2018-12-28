package com.pyg.service.impl;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.entity.PageResult;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.pojo.TbBrandExample;
import com.pyg.mapper.TbBrandMapper;
import com.pyg.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

@Service(timeout = 8000)
public class BrandServiceImpl implements BrandService {
    @Autowired
    private TbBrandMapper tbrandMapper;

    @Override
    public List<TbBrand> findAll() {
        return tbrandMapper.selectByExample(null);
    }

    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        //强转这个page继承了ArayList
        Page<TbBrand> page = (Page<TbBrand>) tbrandMapper.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public void add(TbBrand tbBrand) {
        //执行插入的方法
        tbrandMapper.insert(tbBrand);
    }

    /**
     * 根据id查询品牌的方法
     *
     * @param id
     * @return
     */
    @Override
    public TbBrand findOne(long id) {
        return tbrandMapper.selectByPrimaryKey(id);
    }

    /**
     * 根据修改品牌的方法
     *
     * @param tbBrand
     */
    @Override
    public void update(TbBrand tbBrand) {
        tbrandMapper.updateByPrimaryKey(tbBrand);
    }

    @Override
    public void delete(long[] ids) {
        for (long id : ids) {
            tbrandMapper.deleteByPrimaryKey(id);
        }
    }

    @Override
    public PageResult findPage( @RequestBody TbBrand tbBrand, int pageNum, int pageSize) {

        PageHelper.startPage(pageNum, pageSize);//分页

        TbBrandExample example=new TbBrandExample();

        TbBrandExample.Criteria criteria = example.createCriteria();
        //判断条件的姓名是否为空
        if (tbBrand!=null){
            if (StringUtils.isNotEmpty(tbBrand.getName())){
                criteria.andNameLike("%"+tbBrand.getName()+"%");
            }
            if (StringUtils.isNotEmpty(tbBrand.getFirstChar())){
                //精准条件判断，是否等于
                criteria.andFirstCharEqualTo(tbBrand.getFirstChar());
            }
        }
        //使用github里面的page继承了ArrayList封装了pageInfo里面的所有消息
        Page<TbBrand> tbBrands = (Page<TbBrand>) tbrandMapper.selectByExample(example);
        return new PageResult(tbBrands.getTotal(),tbBrands.getResult());
    }

    @Override
    public List<Map> selectOptionList() {
        return tbrandMapper.selectOptionList();
    }

}
