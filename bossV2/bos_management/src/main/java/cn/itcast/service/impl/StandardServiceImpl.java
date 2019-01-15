package cn.itcast.service.impl;


import cn.itcast.bos.domain.base.Standard;
import cn.itcast.repository.StandardRespostroy;
import cn.itcast.service.StandardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class StandardServiceImpl implements StandardService {
    //注入到
    @Autowired
    private StandardRespostroy standardRespostroy;

    //保存方法
    @Override
    public void save(Standard standard) {
        standardRespostroy.save(standard);
    }

    @Override
    public Page<Standard> query(Specification specification, Pageable pageable) {
//        因为easyui默认分页使用的是从1开始的
        Page<Standard> standardPage = standardRespostroy.findAll(specification, pageable);
        return standardPage;
    }

    @Override
    public void delete(Integer id) {
        //删除数据，修改状态码
        Standard standard = standardRespostroy.getOne(id);
        standard.setStatus_delete('1');
        standardRespostroy.save(standard);

    }

    @Override
    public List<Standard> findAll() {
        return standardRespostroy.findAll();
    }
}
