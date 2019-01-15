package cn.itcast.controller;

import cn.itcast.bos.domain.base.Courier;
import cn.itcast.bos.domain.base.Standard;
import cn.itcast.bos.domain.common.ResponseResult;
import cn.itcast.repository.CourierRespostroy;
import cn.itcast.service.CourierService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/courier")
public class CourierController {
    @Autowired
    private CourierService courierService;

    /**
     * 收派标准的保存方法
     *
     * @param standard
     * @return
     */
    @RequestMapping("/save")
    public ResponseResult save(Courier standard) {
        //异常处理如果执行成功返回为true，返回一个对象
//        standard.setStatus_delete(0);
        try {
            standard.setDeltag('0');
            courierService.save(standard);
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
    public Map query(int page, int rows, Courier courier) {
        //增加代码严谨性
        if (page < 1) {
            page = 1;
        }
        if (rows < 1) {
            rows = 1;
        }
        //构造查询条件
        Specification<Courier> specification = new Specification<Courier>() {
            @Override
            public Predicate toPredicate(Root<Courier> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                //构建查询条件，单表查询
                //先判断条件的输入
                //定义一个集合用来封装每次多表查询条件
                ArrayList<Predicate> list = new ArrayList<Predicate>();
                //是否输入工号
                if (StringUtils.isNotBlank(courier.getCourierNum())) {
                    //构建查询条件,返回的是predicate对象
                    Predicate predicate1 = cb.equal(root.get("courierNum"), courier.getCourierNum());
                    list.add(predicate1);
                }
                //是否输入公司名称
                if (StringUtils.isNotBlank(courier.getCompany())) {
                    //构建查询条件,返回的是predicate对象
                    //root里面get得到的是object类型的需要进行类型转换，因为root.get得到的是一个泛型,模糊查询不是
                    //模糊查询需要确定泛型
                    Predicate predicate2 = cb.like(root.get("company").as(String.class), "%" + courier.getCompany() + "%");
                    list.add(predicate2);
                }
                //是否输入类型
                if (StringUtils.isNotBlank(courier.getType())) {
                    //构建查询条件,返回的是predicate对象
                    //root里面get得到的是object类型的需要进行类型转换，因为root.get得到的是一个泛型,模糊查询不是
                    //模糊查询需要确定泛型
                    Predicate predicate3 = cb.like(root.get("type").as(String.class), courier.getType());
                    list.add(predicate3);
                }
                //判断对收派标准的获取
                //多表查询,使用内连接进行查询
                //  Join<Standard, Courier>前面是从哪个表后面是喝哪个表进行连接
                Join<Standard, Courier> standard = root.join("standard", JoinType.INNER);
                if (courier.getStandard() != null && StringUtils.isNotBlank(courier.getStandard().getName())) {
                    //先判断里面是否有值

                    Predicate predicate4 = cb.like(standard.get("name").as(String.class), "%" + courier.getName() + "%");
                    list.add(predicate4);
                }

                return cb.and(list.toArray(new Predicate[0]));
            }
        };
        Pageable pageable = PageRequest.of(page - 1, rows);
        //jpa分页pagebean对象的page参数是从0开始的，不是1
        Page<Courier> couriers = courierService.query(specification, pageable);
        //获取总记录数
        long total = couriers.getTotalElements();
        //添加到map集合
        HashMap<String, Object> map = new HashMap<>();
        map.put("total", total);
        //获取所有的记录
        List<Courier> content = couriers.getContent();
        map.put("rows", content);
        return map;

    }

    /**
     * 作废快递员其实修改它的状态为1
     *
     * @param ids
     * @return
     */
    @RequestMapping("/delete")
    public ResponseResult dele(Integer[] ids) {
//        System.out.println(ids);
        try {
            for (Integer id : ids) {
                //批量作废
                courierService.uptade(id,'1');
            }
            return ResponseResult.SUCCESS();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResult.FAIL();
        }
    }

    /**
     * 收派员的还原操作
     * @param ids
     * @return
     */
    @RequestMapping("/restore")
    public ResponseResult restore(Integer[] ids) {
//        System.out.println(ids);
        try {
            for (Integer id : ids) {
                //批量作废
                courierService.uptade(id,'0');
            }
            return ResponseResult.SUCCESS();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResult.FAIL();
        }
    }

    /**
     * 收派员的还原操作
     * @param
     * @return
     */
    @Autowired
    private CourierRespostroy courierRespostroy;
    @RequestMapping("/query1")
    public ResponseResult qyery1() {
//        System.out.println(ids);
        try {
            Courier courier = courierRespostroy.getOne(3);
            String s = "小";
            char c = s.charAt(0);
            System.out.println(c);
            Character deltag = courier.getDeltag();
            System.out.println(deltag);
            return ResponseResult.SUCCESS();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResult.FAIL();
        }
    }

}
