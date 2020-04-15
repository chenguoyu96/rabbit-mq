package org.chenguoyu;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class RabbitProducer {
    private static final String EXCHANGE_NAME = "exchange_demo";
    private static final String ROUTING_KEY = "routingkey_demo";
    private static final String QUEUE_NAME = "queue_demo";
    private static final String IP_ADDRESS = "192.168.223.128";
    private static final int PORT = 5672;

    public static void main(String[] args) throws IOException, TimeoutException {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(IP_ADDRESS);
        connectionFactory.setPort(PORT);
        connectionFactory.setUsername("root");
        connectionFactory.setPassword("root123");
        // 创建连接
        Connection connection = connectionFactory.newConnection();
        // 创建信道
        Channel channel = connection.createChannel();
        // 创建一个type="direct",持久化，非自动删除的交换器
        channel.exchangeDeclare(
                EXCHANGE_NAME,  // 交换器的名称
                "direct", // 交换器的类型 常见的有fanout,direct,
                true,  // 设置是否持久化 true表示持久化 false表示非持久化
                false, // 设置是否自动删除 true表示自动删除 自动删除的前提是至少有一个队列或者交换器与这个交换器绑定，
                                // 之后所有与这个交换器绑定的队列或者交换器都与此解绑，不能错误的理解为：“当与此交换器连接的客户端都断开时，rabbitmq会自动删除本交换器”
                false,  // 设置时是否是内置的。如果设置为true，则表示是内置的交换器，客户端程序无法直接发送消息到这个交换器中，只能通过交换器到交换器这种方式
                null  // 其他一些结构化参数
        );
        // 创建一个持久化的，非排他的，非自动删除的队列
        channel.queueDeclare(
                QUEUE_NAME, // 队列的名称
                true, // 是否持久化
                false, // 是否排他 如果队列被声明为排他，那么该队列仅对首次声明它的连接可见，并且断开时删除。
                // 需要注意三点：
                // 1. 排他队列是基于连接可见的，同一个连接的不同信道是可以访问同一连接的排他队列
                // 2. “首次” 指的是如果一个连接已经声明了排他队列，其他连接是不允许建立同名的排他队列的, 这个与普通队列不同
                // 3. 即使该队列是持久化的， 一旦连接关闭或者客户端退出, 该排他队列还是会被自动删除， 这种队列适用于客户端同时发送与读取消息的应用场景
                false, // 设置是否自动删除. 自动删除的前提是至少有一个消费者连接到这个队列，之后所有与这个队列连接的消费者都断开时，才会自动删除。
                // 不能把这个参数错误的理解为：“当连接到此队列的所有客户端断开时，这个队列会自动删除”，因为生产者客户端创建这个队列，或者没有消费者客户的那与这个队列连接时，都不会删除这个队列
                null  // 设置队列的一些其他参数
        );
        // 将交换器与队列通过路由绑定
        channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, ROUTING_KEY);
        String message = "Hello World";
        channel.basicPublish(EXCHANGE_NAME, ROUTING_KEY, MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes());
        // 关闭资源
        channel.close();
        connection.close();
    }


}
