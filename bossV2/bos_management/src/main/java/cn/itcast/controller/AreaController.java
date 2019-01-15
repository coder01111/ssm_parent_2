package cn.itcast.controller;

import cn.itcast.bos.domain.base.Area;
import cn.itcast.bos.domain.base.Courier;
import cn.itcast.bos.domain.common.ResponseResult;
import cn.itcast.service.AreaService;
import cn.itcast.utils.PinYin4jUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/area")
public class AreaController {
    @Autowired
    private AreaService areaService;

    /**
     * 使用文件上传b必须传递一个文件参数
     *
     * @param file
     */
    @RequestMapping("/import")
    public ResponseResult batchImport(MultipartFile file) throws IOException {
        //创建一个集合用来存储每一个区域对象
        List<Area> areas = new ArrayList<>();
        //加载execl文件对象
        HSSFWorkbook hssfWorkbook = new HSSFWorkbook(file.getInputStream());
        //读取一个sheet
        HSSFSheet hssfSheet = hssfWorkbook.getSheetAt(0);
        try {
            //读取sheet中每一行
            for (Row row : hssfSheet) {
                //一行数据对应一个区域对象
                if (row.getRowNum() == 0) {
                    //第一行跳过
                    continue;
                }
                //跳过空行主键为空，或者空字符串
                if (row.getCell(0) == null || StringUtils.isBlank(row.getCell(0).getStringCellValue())) {
                    continue;
                }
                //创建一个区域对象
                Area area = new Area();
                //得到每一行中的每一格给实体类对应成员变量以及数据库字段赋值
                area.setId(row.getCell(0).getStringCellValue());
                area.setProvince(row.getCell(1).getStringCellValue());
                area.setCity(row.getCell(2).getStringCellValue());
                area.setDistrict(row.getCell(3).getStringCellValue());
                area.setPostcode(row.getCell(4).getStringCellValue());
                areas.add(area);
//                //设置中文简体
//                String province = area.getProvince();
//                String city = area.getCity();
//                String district = area.getDistrict();
//                //转换为拼音
//                //先截取每个字段的前两个
//                province = province.substring(0, province.length() - 1);
//                city = city.substring(0, city.length() - 1);
//                district = district.substring(0, district.length() - 1);
//                //调用工具类的方法,得到一个首字母字符数组
//                String[] headArray = PinYin4jUtils.getHeadByString(province + city + district);
//                //遍历拼接这些首字母,使用stringbBuffer线程安全
//                StringBuffer sb = new StringBuffer();
//
//                for (String headStr : headArray) {
//                    sb.append(headStr);
//                }
//                //设置简码
//                area.setShortcode(sb.toString());
//                //获取城市编码,对每一个单音字进行分割然后把汉字转换为拼音
//                String cityCodde = PinYin4jUtils.hanziToPinyin(city, "");
//                area.setCitycode(cityCodde);
                setCityCode(area);
            }
            areaService.importBatch(areas);
            return ResponseResult.SUCCESS();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResult.FAIL();
        }

    }

    /**
     * 查询所有方法
     *
     * @param page
     * @param rows
     * @return
     */
    @RequestMapping("/query")
    public Map query(int page, int rows, Area area) {
        //增加代码严谨性
        if (page < 1) {
            page = 1;
        }
        if (rows < 1) {
            rows = 1;
        }
        Pageable pageable = PageRequest.of(page - 1, rows);
        //jpa分页pagebean对象的page参数是从0开始的，不是1
        Specification<Area> specification = new Specification<Area>() {
            @Override
            public Predicate toPredicate(Root<Area> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                //先判断是否有输入了省份
                List<Predicate> list = new ArrayList<>();
                if (StringUtils.isNotBlank(area.getProvince())) {
                    //使用模糊查询
                    Predicate p1 = criteriaBuilder.like(root.get("province"), area.getProvince());
                    list.add(p1);
                }
                if (StringUtils.isNotBlank(area.getCity())) {
                    Predicate p2 = criteriaBuilder.like(root.get("city").as(String.class), area.getCity());
                    list.add(p2);
                }
                if (StringUtils.isNotBlank(area.getDistrict())) {
                    Predicate p3 = criteriaBuilder.like(root.get("district").as(String.class), area.getDistrict());
                    list.add(p3);
                }
                //构建一个多条件查询构造一个空数组
                Predicate predicate = criteriaBuilder.and(list.toArray(new Predicate[0]));
                return predicate;
            }
        };
        Page<Area> areas = areaService.query(specification, pageable);
        //获取总记录数
        long total = areas.getTotalElements();
        //添加到map集合
        HashMap<String, Object> map = new HashMap<>();
        map.put("total", total);
        //获取所有的记录
        List<Area> content = areas.getContent();
        map.put("rows", content);
        return map;

    }

    /**
     * 收派标准的保存方法
     *
     * @param area
     * @return
     */
    @RequestMapping("/save")
    public ResponseResult save(Area area) {
        //异常处理如果执行成功返回为true，返回一个对象
//        standard.setStatus_delete(0);
        try {
            //需要设置简码和城市编码
            setCityCode(area);
            areaService.save(area);
            return ResponseResult.SUCCESS();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResult.FAIL();
        }
    }


    public void setCityCode(Area area) {
        //设置中文简体
        String province = area.getProvince();
        String city = area.getCity();
        String district = area.getDistrict();
        //转换为拼音
        //先截取每个字段的前两个
        province = province.substring(0, province.length() - 1);
        city = city.substring(0, city.length() - 1);
        district = district.substring(0, district.length() - 1);
        //调用工具类的方法,得到一个首字母字符数组
        String[] headArray = PinYin4jUtils.getHeadByString(province + city + district);
        //遍历拼接这些首字母,使用stringbBuffer线程安全
        StringBuffer sb = new StringBuffer();

        for (String headStr : headArray) {
            sb.append(headStr);
        }
        //设置简码
        area.setShortcode(sb.toString());
        //获取城市编码,对每一个单音字进行分割然后把汉字转换为拼音
        String cityCodde = PinYin4jUtils.hanziToPinyin(city, "");
        area.setCitycode(cityCodde);
    }

    /**
     * 收派员的还原操作
     * @param ids
     * @return
     */
    @RequestMapping("/delete")
    public ResponseResult restore(String ids) {
//        System.out.println(ids);
        try {
            String[] idArray = ids.split(",");
                //批量作废
                areaService.delete(idArray);
            return ResponseResult.SUCCESS();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResult.FAIL();
        }
    }
}
