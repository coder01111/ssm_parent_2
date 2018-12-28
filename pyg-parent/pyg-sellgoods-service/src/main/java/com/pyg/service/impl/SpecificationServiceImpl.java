package com.pyg.service.impl;

import java.util.List;
import java.util.Map;

import com.pinyougou.entity.PageResult;
import com.pinyougou.entityGroup.Specification;
import com.pinyougou.pojo.TbSpecification;
import com.pinyougou.pojo.TbSpecificationOption;
import com.pinyougou.pojo.TbSpecificationOptionExample;
import com.pyg.mapper.TbSpecificationOptionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pyg.mapper.TbSpecificationMapper;;
import com.pinyougou.pojo.TbSpecificationExample;
import com.pinyougou.pojo.TbSpecificationExample.Criteria;
import com.pyg.service.SpecificationService;


/**
 * 服务实现层
 *
 * @author Administrator
 */
@Service(timeout = 8000)
public class SpecificationServiceImpl implements SpecificationService {

    @Autowired
    private TbSpecificationMapper specificationMapper;

    @Autowired
    private TbSpecificationOptionMapper tbSpecificationOptionMapper;

    /**
     * 查询全部
     */
    @Override
    public List<TbSpecification> findAll() {
        return specificationMapper.selectByExample(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<TbSpecification> page = (Page<TbSpecification>) specificationMapper.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 增加
     */
    @Override
    public void add(Specification specification) {
        //插入规格
        specificationMapper.insert(specification.getSpecification());
        //循环插入规格选项
        for (TbSpecificationOption specificationOption : specification.getSpecificationOptionList()) {
            //需要根据规格表的id插入到规格选项的表所以我们需要把规格表中自增的id查出来然后插入规格选项表
            specificationOption.setSpecId(specification.getSpecification().getId());
            //根据得到的id插入规格选项表
                tbSpecificationOptionMapper.insert(specificationOption);
        }
    }


    /**
     * 修改
     */
    @Override
    public void update(Specification specification) {

        //保存修改的规格
        specificationMapper.updateByPrimaryKey(specification.getSpecification());
        //因为不知道怎么修改的是删除了还是添加了所以全部删除了再添加
        //删除原有的规格选项
        TbSpecificationOptionExample example=new TbSpecificationOptionExample();
        com.pinyougou.pojo.TbSpecificationOptionExample.Criteria criteria = example.createCriteria();
        criteria.andSpecIdEqualTo(specification.getSpecification().getId());//指定规格ID为条件
        tbSpecificationOptionMapper.deleteByExample(example);
        //循环插入规格选项就是在保存一篇
        for(TbSpecificationOption specificationOption:specification.getSpecificationOptionList()){
            specificationOption.setSpecId(specification.getSpecification().getId());
            tbSpecificationOptionMapper.insert(specificationOption);
        }


    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public Specification findOne(Long id) {
                    //先根据修改带来的Id查询出来规格
        TbSpecification tbSpecification = specificationMapper.selectByPrimaryKey(id);
        //	//查询规格选项列表
        //创建查询的模板根据id查询
        TbSpecificationOptionExample example = new TbSpecificationOptionExample();
        TbSpecificationOptionExample.Criteria criteria = example.createCriteria();
        criteria.andSpecIdEqualTo(id);
        List<TbSpecificationOption> specificationOptions = tbSpecificationOptionMapper.selectByExample(example);
        Specification specification = new Specification();
        specification.setSpecification(tbSpecification);
        specification.setSpecificationOptionList(specificationOptions);
        return specification;
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            //先删除规格
            specificationMapper.deleteByPrimaryKey(id);
            //	//删除原有的规格选项
            //删除原有的规格选项
            TbSpecificationOptionExample example=new TbSpecificationOptionExample();
            com.pinyougou.pojo.TbSpecificationOptionExample.Criteria criteria = example.createCriteria();
            //根据指定id为删除条件
            criteria.andIdEqualTo(id);
            tbSpecificationOptionMapper.deleteByExample(example);
        }
    }


    @Override
    public PageResult findPage(TbSpecification specification, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        TbSpecificationExample example = new TbSpecificationExample();
        Criteria criteria = example.createCriteria();

        if (specification != null) {
            if (specification.getSpecName() != null && specification.getSpecName().length() > 0) {
                criteria.andSpecNameLike("%" + specification.getSpecName() + "%");
            }

        }

        Page<TbSpecification> page = (Page<TbSpecification>) specificationMapper.selectByExample(example);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public List<Map> selectOptionList() {
        return specificationMapper.selectOptionList();
    }

}
