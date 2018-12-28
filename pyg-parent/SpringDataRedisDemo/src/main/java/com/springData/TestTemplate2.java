package com.springData;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundListOperations;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring/applicationContext-redis.xml")
public class TestTemplate2 {
    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    /**
     * 操作list的方法,有重复，存入元素
     */
    @Test
    public void testSetList(){
        BoundListOperations<String, Object> ops = redisTemplate.boundListOps("list");
        ops.leftPushAll("老大","老二","老三");
    }

    /**
     * 存入元素
     */
    @Test
    public void testSetListRigth() {
        BoundListOperations<String,Object> ops = redisTemplate.boundListOps("list");
        ops.rightPushAll("老大","老二","老三");
    }
    @Test
    public void testGetList(){
        redisTemplate.delete("list");
        BoundListOperations<String, Object> ops = redisTemplate.boundListOps("list");
        List<Object> range = ops.range(0, 2);
        System.out.println(range);


    }
    @Test
    public void testSet1List() {
        BoundListOperations<String, Object> ops = redisTemplate.boundListOps("list");
        ops.set(2, "李贯");//把下标为2的值设置成"李贯"，如果没有这个下标报错
        Object index = ops.index(2);
        System.out.println(index);
    }

    /**
     * 删除整个list
     */
    @Test
    public void testDelList(){
        redisTemplate.delete("list");

    }

    /**
     * 移除指定位置指定个数的元素
     */
    @Test
    public void testRemoveList(){
        BoundListOperations<String, Object> ops = redisTemplate.boundListOps("list");
        ops.remove(1,"老大");

    }

}
