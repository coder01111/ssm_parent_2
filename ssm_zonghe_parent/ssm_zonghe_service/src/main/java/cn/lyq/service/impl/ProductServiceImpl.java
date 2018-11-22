package cn.lyq.service.impl;

import cn.lyq.dao.ProductDao;
import cn.lyq.domain.Product;
import cn.lyq.service.ProductService;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {
    @Autowired
    private ProductDao productDao;
    @Override
    public List<Product> findAll(int page,int size) throws Exception {
        //参数pageNum 是页码值   参数pageSize 代表是每页显示条数
        PageHelper.startPage(page, size);

        return productDao.findAll(page,size);
    }

    @Override
    public void save(Product product) {
        productDao.save(product);
    }
}
