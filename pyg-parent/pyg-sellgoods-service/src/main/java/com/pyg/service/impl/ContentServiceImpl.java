package com.pyg.service.impl;

import java.util.List;

import com.pinyougou.entity.PageResult;
import com.pinyougou.pojo.TbContent;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pyg.mapper.TbContentMapper;
import com.pinyougou.pojo.TbContentExample;
import com.pinyougou.pojo.TbContentExample.Criteria;
import com.pyg.service.ContentService;
import org.springframework.data.redis.core.RedisTemplate;


/**
 * 服务实现层
 *
 * @author Administrator
 */
@Service(timeout = 8000)
public class ContentServiceImpl implements ContentService {

    @Autowired
    private TbContentMapper contentMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 查询全部
     */
    @Override
    public List<TbContent> findAll() {
        return contentMapper.selectByExample(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<TbContent> page = (Page<TbContent>) contentMapper.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 增加
     */
    @Override
    public void add(TbContent content) {
        //新增广告清除缓存
        contentMapper.insert(content);
        redisTemplate.boundHashOps("content").delete(content.getCategoryId());

    }


    /**
     * 修改,修改了之后要清除缓存
     */
    @Override
    public void update(TbContent content) {

        //查询修改前的分类Id
        Long categoryId = contentMapper.selectByPrimaryKey(content.getId()).getCategoryId();
        //清除指定修改的缓存
        redisTemplate.boundHashOps("content").delete(categoryId);
        contentMapper.updateByPrimaryKey(content);
        //判断id是否发生改变
        if (categoryId.longValue()!=content.getCategoryId().longValue()){
            //清除缓存
            redisTemplate.boundHashOps("content").delete(content.getCategoryId());
        }

    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public TbContent findOne(Long id) {
        return contentMapper.selectByPrimaryKey(id);
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            Long categoryId = contentMapper.selectByPrimaryKey(id).getCategoryId();
            //广告分类ID
            redisTemplate.boundHashOps("content").delete(categoryId);

            contentMapper.deleteByPrimaryKey(id);
        }
    }


    @Override
    public PageResult findPage(TbContent content, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        TbContentExample example = new TbContentExample();
        Criteria criteria = example.createCriteria();

        if (content != null) {
            if (content.getTitle() != null && content.getTitle().length() > 0) {
                criteria.andTitleLike("%" + content.getTitle() + "%");
            }
            if (content.getUrl() != null && content.getUrl().length() > 0) {
                criteria.andUrlLike("%" + content.getUrl() + "%");
            }
            if (content.getPic() != null && content.getPic().length() > 0) {
                criteria.andPicLike("%" + content.getPic() + "%");
            }
            if (content.getStatus() != null && content.getStatus().length() > 0) {
                criteria.andStatusLike("%" + content.getStatus() + "%");
            }

        }

        Page<TbContent> page = (Page<TbContent>) contentMapper.selectByExample(example);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 使用缓存技术读入内存中下次不用重新查询,
     *
     * @param categoryId
     * @return
     */
    @Override
    public List<TbContent> findByCategoryId(Long categoryId) {
        //先重缓存中查询如果没有再查询数据把每一个广告类别的id和整条广告对应起来放入hash
        //也就是一个map中
        List<TbContent> contents = (List<TbContent>) redisTemplate.boundHashOps("content").get("categoryId");
        //判断是否为空
        if (contents == null) {
            //根据广告分类ID查询广告列表
            TbContentExample contentExample = new TbContentExample();
            Criteria criteria = contentExample.createCriteria();
            criteria.andCategoryIdEqualTo(categoryId);
            criteria.andStatusEqualTo("1");//开启状态
            contentExample.setOrderByClause("sort_order");//排序
            contents = contentMapper.selectByExample(contentExample);
            //存入缓存
            redisTemplate.boundHashOps("content").put(categoryId, contents);
        } else {
            System.out.println("从缓存读取数据");
        }
        return contents;


    }

}
