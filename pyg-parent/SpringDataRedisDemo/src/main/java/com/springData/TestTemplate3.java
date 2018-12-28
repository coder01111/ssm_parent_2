package com.springData;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring/applicationContext-redis.xml")
public class TestTemplate3 {
    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    @Test
    public void testSetHash(){
        BoundHashOperations<String, Object, Object> ops = redisTemplate.boundHashOps("nihao");
        HashMap<String, Object> map = new HashMap<>();
        map.put("GJ", "少女朝代");
        map.put("SZ", "苏颇朱你儿");
        map.put("DS", "西方神起");
        ops.putAll(map);
    }

    /**
     * 取出值
     */
    @Test
    public void testGetHash() {
        BoundHashOperations<String, Object, Object> ops = redisTemplate.boundHashOps("nihao");
        Set<Object> keys = ops.keys();
        for (Object object : keys) {
            System.out.println(object);
        }

    }
    @Test
    public void testDelHash(){
        BoundHashOperations<String, Object, Object> ops = redisTemplate.boundHashOps("nihao");
        ops.delete("nihao");
    }

    /**
     * 使用springData操作redis中的set，不重复,
     * 存入值
     */
    @Test
    public void testGetHash1() {
        BoundHashOperations<String, Object, Object> ops = redisTemplate.boundHashOps("nihao");
        Map<Object, Object> map = ops.entries();
        for (Map.Entry<Object, Object> entry : map.entrySet()) {
            System.out.println(entry.getKey()+"===="+entry.getValue());
        }
    }
    /**
     *
     */
    @Test
    public void testRemoveSet() {
        BoundHashOperations<String, Object, Object> ops = redisTemplate.boundHashOps("nihao");
        ops.delete("DS");
    }

    }
