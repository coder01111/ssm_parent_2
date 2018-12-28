package cn.itcast.query;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:applicationContext-jms-consumer.xml")
public class QueryConsumer {
    @Test
    public void testQueue(){
        try {
            System.in.read();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


