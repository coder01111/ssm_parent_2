package cn.itcast.controller;

import cn.itcast.bos.domain.base.Standard;
import cn.itcast.bos.domain.common.ResponseResult;
import cn.itcast.service.StandardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/standard")
public class StandardController {
    @Autowired
    private StandardService standardService;

    /**
     * 收派标准的保存方法
     *
     * @param standard
     * @return
     */
    @RequestMapping("/save")
    public ResponseResult save(Standard standard) {
        //异常处理如果执行成功返回为true，返回一个对象
        standard.setStatus_delete('0');
        try {
            standardService.save(standard);
            return ResponseResult.SUCCESS();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResult.FAIL();
        }
    }

    /**
     * 收派标准的分页查询
     *
     * @param page
     * @param rows
     * @return
     */
    @RequestMapping("/query")
    public Map query(int page, int rows) {
        //增加代码严谨性
        if (page < 1) {
            page = 1;
        }
        if (rows < 1) {
            rows = 1;
        }
        Pageable pageable = PageRequest.of(page - 1, rows);
        //jpa分页pagebean对象的page参数是从0开始的，不是1
        //创建查询过滤条件
        Specification<Standard> specification = new Specification<Standard>() {
            @Override
            public Predicate toPredicate(Root<Standard> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                Predicate predicate = criteriaBuilder.equal(root.get("status_delete"), '0');
                return predicate;
            }
        };
        Page<Standard> standards = standardService.query(specification, pageable);
        //获取总记录数
        long total = standards.getTotalElements();
        //添加到map集合
        HashMap<String, Object> map = new HashMap<>();
        map.put("total", total);
        //获取所有的记录
        List<Standard> content = standards.getContent();
        map.put("rows", content);
        return map;

    }

    /**
     * 收派标准的删除指的是业务的删除修改字段属性
     *
     * @param ids
     * @return
     */
    @RequestMapping("/delete")
    public ResponseResult dele(Integer[] ids) {
//        System.out.println(ids);
        try {
            for (Integer id : ids) {
                standardService.delete(id);
            }
            return ResponseResult.SUCCESS();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResult.FAIL();
        }
    }

    /**
     * 收派标准的查询所有的方法
     * 下拉列表的显示是一个json数组
     *
     * @param
     * @return
     */
    @RequestMapping("/findAll")
    public List<Standard> findAll() {
        return standardService.findAll();
    }
}
