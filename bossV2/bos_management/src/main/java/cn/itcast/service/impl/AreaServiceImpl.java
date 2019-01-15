package cn.itcast.service.impl;

import cn.itcast.bos.domain.base.Area;
import cn.itcast.bos.domain.base.Courier;
import cn.itcast.repository.AreaRepsitory;
import cn.itcast.service.AreaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@Service
@Transactional
public class AreaServiceImpl implements AreaService {
    @Autowired
    private AreaRepsitory areaRepsitory;
    @Override
    public void importBatch(List<Area> areas) {
        areaRepsitory.saveAll(areas);
    }

    @Override
    public Page<Area> query(Specification<Area> specification, Pageable pageableq) {
        //        因为easyui默认分页使用的是从1开始的
        Page<Area> courierPage = areaRepsitory.findAll(specification,pageableq);
        return courierPage;
    }

    @Override
    public void save(Area area) {
        areaRepsitory.save(area);
    }

    @Override
    public void delete(String[] idArray) {
        for (String id : idArray) {
            areaRepsitory.deleteById(id);
        }
    }
}
