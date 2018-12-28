package com.pyg.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.pinyougou.entity.PageResult;
import com.pinyougou.pojo.*;
import com.pyg.mapper.TbSpecificationMapper;
import com.pyg.mapper.TbSpecificationOptionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pyg.mapper.TbTypeTemplateMapper;
import com.pinyougou.pojo.TbTypeTemplateExample.Criteria;
import com.pyg.service.TypeTemplateService;
import org.springframework.data.redis.core.RedisTemplate;


/**
 * 服务实现层
 *
 * @author Administrator
 */
@Service(timeout = 8000)
public class TypeTemplateServiceImpl implements TypeTemplateService {

    @Autowired
    private TbTypeTemplateMapper typeTemplateMapper;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private TbSpecificationOptionMapper specificationOptionMapper;


    /**
     * 查询全部
     */
    @Override
    public List<TbTypeTemplate> findAll() {
        return typeTemplateMapper.selectByExample(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<TbTypeTemplate> page = (Page<TbTypeTemplate>) typeTemplateMapper.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 增加
     */
    @Override
    public void add(TbTypeTemplate typeTemplate) {
        typeTemplateMapper.insert(typeTemplate);
    }


    /**
     * 修改
     */
    @Override
    public void update(TbTypeTemplate typeTemplate) {
        typeTemplateMapper.updateByPrimaryKey(typeTemplate);
    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public TbTypeTemplate findOne(Long id) {
        //先查询根据模板id查询模板所有信息
        TbTypeTemplate tbTypeTemplate = typeTemplateMapper.selectByPrimaryKey(id);
        //[{"id":34,"text":"尺寸"},{"id":35,"text":"颜色"}],获取规格信息
        String specIds = tbTypeTemplate.getSpecIds();
        //转换为一个集合用json进行反序列化城一个集合对象
        List<Map> maps = JSONObject.parseArray(specIds, Map.class);
        //遍历这个集合，根据id查找规格选项的所有信息
        for (Map map : maps) {
            Integer specId = (Integer) map.get("id");
            //根据id 查询规格选项
            TbSpecificationOptionExample specificationOptionExample = new TbSpecificationOptionExample();
            TbSpecificationOptionExample.Criteria criteria = specificationOptionExample.createCriteria();
            criteria.andSpecIdEqualTo(Long.valueOf(specId));
            List<TbSpecificationOption> tbSpecificationOptions = specificationOptionMapper.selectByExample(specificationOptionExample);
            map.put("options", tbSpecificationOptions);
        }
        //最后再把这个集合序列化为接送串并且存到模板的规格属性中
        tbTypeTemplate.setSpecIds(JSON.toJSONString(maps));
        //最后再返回这个模板对象就是在查询模板基础上又查询了
        return tbTypeTemplate;

    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            typeTemplateMapper.deleteByPrimaryKey(id);
        }
    }


    @Override
    public PageResult findPage(TbTypeTemplate typeTemplate, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        TbTypeTemplateExample example = new TbTypeTemplateExample();
        Criteria criteria = example.createCriteria();

        if (typeTemplate != null) {
            if (typeTemplate.getName() != null && typeTemplate.getName().length() > 0) {
                criteria.andNameLike("%" + typeTemplate.getName() + "%");
            }
            if (typeTemplate.getSpecIds() != null && typeTemplate.getSpecIds().length() > 0) {
                criteria.andSpecIdsLike("%" + typeTemplate.getSpecIds() + "%");
            }
            if (typeTemplate.getBrandIds() != null && typeTemplate.getBrandIds().length() > 0) {
                criteria.andBrandIdsLike("%" + typeTemplate.getBrandIds() + "%");
            }
            if (typeTemplate.getCustomAttributeItems() != null && typeTemplate.getCustomAttributeItems().length() > 0) {
                criteria.andCustomAttributeItemsLike("%" + typeTemplate.getCustomAttributeItems() + "%");
            }

        }

        Page<TbTypeTemplate> page = (Page<TbTypeTemplate>) typeTemplateMapper.selectByExample(example);
        saveToRedis();
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public List<Map> selectOptionList() {

        return typeTemplateMapper.selectOptionList();
    }
    //将模板中的规格数据添加到缓存中

    /**
     * 将数据存入缓存
     */
    private void saveToRedis() {

    //获取模板数据
        //获取模板数据
        List<TbTypeTemplate> typeTemplateList = findAll();
        //循环模板
        for (TbTypeTemplate typeTemplate : typeTemplateList) {
            //存储品牌列表
            List<Map> brandList = JSON.parseArray(typeTemplate.getBrandIds(), Map.class);
            redisTemplate.boundHashOps("brandList").put(typeTemplate.getId(),brandList);
            //存储规格列表
            List<Map> specList = findSpecList(typeTemplate.getId());//根据模板ID查询规格列表
            redisTemplate.boundHashOps("specList").put(typeTemplate.getId(), specList);
        }
    }

    private List<Map> findSpecList(Long id) {
        //先查询根据模板id查询模板所有信息
        TbTypeTemplate tbTypeTemplate = typeTemplateMapper.selectByPrimaryKey(id);
        //[{"id":34,"text":"尺寸"},{"id":35,"text":"颜色"}],获取规格信息
        String specIds = tbTypeTemplate.getSpecIds();
        //转换为一个集合用json进行反序列化城一个集合对象
        List<Map> specList = JSONObject.parseArray(specIds, Map.class);
        //遍历这个集合，根据id查找规格选项的所有信息
        for (Map map : specList) {
            Integer specId = (Integer) map.get("id");
            //根据id 查询规格选项
            TbSpecificationOptionExample specificationOptionExample = new TbSpecificationOptionExample();
            TbSpecificationOptionExample.Criteria criteria = specificationOptionExample.createCriteria();
            criteria.andSpecIdEqualTo(Long.valueOf(specId));
            List<TbSpecificationOption> tbSpecificationOptions = specificationOptionMapper.selectByExample(specificationOptionExample);
            map.put("options", tbSpecificationOptions);
        }
        return specList;
    }
}
