package cn.itcas.test;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.io.IOException;

public class QueryConsumer {
    public static void main(String[] args) throws JMSException, IOException {
        //1.创建连接工
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory("tcp://192.168.25.168:61616");
        //2.创建连接
        Connection connection = factory.createConnection();
        //3.启动连接
        connection.start();
        //4.获取session(会话对象)  参数1：是否启动事务  参数2：消息确认方式
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        //5.创建队列对象
        Queue queue = session.createQueue("test-query");
        //6.创建消息消费者对象
        MessageConsumer consumer = session.createConsumer(queue);
        //7.设置监听
        consumer.setMessageListener(new MessageListener() {
            @Override
            public void onMessage(Message message) {
                //接收到的生产者的message
                TextMessage textMessage = (TextMessage) message;
                try {
                    System.out.println("提取的消息：" + textMessage.getText());
                } catch (JMSException e) {
                    e.printStackTrace();
                }

            }
        });
        //8.等待键盘输入,是为了能够监听到队列方式可以不用因为队列消息的方式会把
//        消息放入队列中而广播方式并不会这样，即时广播
        //System.in.read();
        //9.关闭资源
        consumer.close();
        session.close();
        connection.close();

    }
}
