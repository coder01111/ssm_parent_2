package com.springData;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Set;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring/applicationContext-redis.xml")
public class TestTemplate {
    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    @Test
    public void testSetString(){
        BoundValueOperations<String, Object> ops = redisTemplate.boundValueOps("name");
        ops.set("aaa");

    }
    @Test
    public void testGetString() {
        BoundValueOperations<String,Object> ops = redisTemplate.boundValueOps("name");
        Object obj = ops.get();
        System.out.println(obj);
    }
    @Test
    public void testDelString(){
        redisTemplate.delete("name");

    }

    /**
     * 使用springData操作redis中的set，不重复,
     * 存入值
     */
    @Test
    public void testSetSet() {
        BoundSetOperations<String, Object> ops = redisTemplate.boundSetOps("hello");
        ops.add("张三","李四","王五");
    }
    /**
     * 使用springData操作redis中的set,无序，不重复
     * 取出值
     */
    @Test
    public void testGetSet() {
        BoundSetOperations<String, Object> ops = redisTemplate.boundSetOps("hello");
        //获取set集合里面的元素
        Set<Object> members = ops.members();
        for (Object member : members) {
            System.out.println(member);
        }
    }

    /**
     * 删除值
     */
    @Test
    public void testDelSet(){
        redisTemplate.delete("hello");

    }

    /**
     * 弹出一个值
     */
    @Test
    public void testPopSet() {
        BoundSetOperations<String, Object> ops = redisTemplate.boundSetOps("hello");
        Object pop = ops.pop();
        System.out.println(pop);
    }

    /**
     *
     */
    @Test
    public void testRemoveSet() {
        BoundSetOperations<String, Object> ops = redisTemplate.boundSetOps("hello");
        ops.remove("张三");
    }

    }
