package cn.itcast.test;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

public class QueryProducer {
    public static void main(String[] args) throws JMSException {
        //1.创建连接工厂
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory("tcp://192.168.25.168:61616");
        //2.创建连接
        Connection connection = factory.createConnection();
        //3.启动连接
        connection.start();
        //4.获取session(会话对象)  参数1：是否启动事务  参数2：消息确认方式
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        //5.创建队列对象
        Queue queue = session.createQueue("test-query");
        //6.创建消息生产者对象
        MessageProducer producer = session.createProducer(queue);
        //7.创建消息对象（文本消息）
        TextMessage message = session.createTextMessage("欢迎来到峡谷世界");
        //8.发送消息
        producer.send(message);
        //9.关闭资源
        producer.close();
        session.close();
        connection.close();
    }

}
