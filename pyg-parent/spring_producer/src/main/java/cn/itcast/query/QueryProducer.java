package cn.itcast.query;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:applicationContext-jms-producer.xml")
public class QueryProducer {
    //注入spring整合的jms模板
    @Autowired
    private JmsTemplate jmsTemplate;
    //注入目标对象
    @Autowired
    private Destination queueTextDestination;
    /**
     * 发送文本消息
     */
    @Test
    public void sendTextMessage() {
        String text = "spring JMS 点对点";
         jmsTemplate.send(queueTextDestination,(new MessageCreator() {
            //返回发送的信息，然后在发送
            @Override
            public Message createMessage(Session session) throws JMSException {
                return session.createTextMessage(text);
            }
        }));
    }

    }
