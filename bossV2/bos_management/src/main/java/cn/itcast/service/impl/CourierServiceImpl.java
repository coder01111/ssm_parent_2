package cn.itcast.service.impl;

import cn.itcast.bos.domain.base.Courier;
import cn.itcast.repository.CourierRespostroy;
import cn.itcast.service.CourierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class CourierServiceImpl implements CourierService {
    @Autowired
    private CourierRespostroy courierRespostroy;

    @Override
    public void save(Courier courier) {
        courierRespostroy.save(courier);
    }

    @Override
    public Page<Courier> query(Specification<Courier> specification, Pageable pageableq) {
//        因为easyui默认分页使用的是从1开始的
        Page<Courier> courierPage = courierRespostroy.findAll(specification,pageableq);
        return courierPage;
    }

    /**
     * 修改删除状态为1
     * @param id
     */
    @Override
    public void uptade(Integer id,Character status) {
       courierRespostroy.updateDel(id,status);

    }


}
