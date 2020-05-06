package org.chenguoyu.simple;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import org.chenguoyu.util.ConnectionUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public class RabbitProducer {
    private static final String EXCHANGE_NAME = "exchange_demo";
    private static final String ROUTING_KEY = "routingkey_demo";
    private static final String QUEUE_NAME = "queue_demo";

    public static void main(String[] args) throws IOException, TimeoutException {
        Connection connection = ConnectionUtils.getConnection();
        // 创建信道
        Channel channel = connection.createChannel();
        // 创建一个type="direct",持久化，非自动删除的交换器
        Map<String, Object> channelSettings = new HashMap<>();
        channelSettings.put("alternate-exchage", "myAe"); // 声明备份交换器，如果生产者发送的消息没有匹配到队列中，那么会进入到备份交换器中匹配队列
        channel.exchangeDeclare(
                EXCHANGE_NAME,  // 交换器的名称
                "direct", // 交换器的类型 常见的有direct(发送到与routeKey完全匹配的队列中)，fanout(发送到所有与该交换器绑定的队列中),topic(发送到模式匹配的队列中),header(不根据路由键匹配,而是根据消息的header匹配)
                true,  // 设置是否持久化 true表示持久化 false表示非持久化
                false, // 设置是否自动删除 true表示自动删除 自动删除的前提是至少有一个队列或者交换器与这个交换器绑定，
                // 之后所有与这个交换器绑定的队列或者交换器都与此解绑，不能错误的理解为：“当与此交换器连接的客户端都断开时，rabbitmq会自动删除本交换器”
                false,  // 设置时是否是内置的。如果设置为true，则表示是内置的交换器，客户端程序无法直接发送消息到这个交换器中，只能通过交换器到交换器这种方式
                null  // 其他一些结构化参数
        );
        // 创建一个持久化的，非排他的，非自动删除的队列
        Map<String, Object> queueSettings = new HashMap<>();
        queueSettings.put("x-message-ttl", 6000); // 设置过期时间，不设置表明消息不过期，如果设置为0表示除非能直接将消息投递给消费者，否支该消息会立刻被丢弃
        queueSettings.put("x-expires", 1800000); // 如果队列不被使用，则删除
        queueSettings.put("x-dead-letter-exchange", "dlx_exchange"); //设置死信队列(过期的消息放在死信队列中)
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
                queueSettings  // 设置队列的一些其他参数
        );
        // 将交换器与队列通过路由绑定
        channel.queueBind(
                QUEUE_NAME,   // 队列名称
                EXCHANGE_NAME, // 交换器的名称
                ROUTING_KEY, // 用来绑定队列和交换器的路由键
                null//定义绑定的一些参数
        );
        String message = "Hello World";
        // 发送消息
        AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
                .contentType("text/plain")
                .deliveryMode(2) // 投递模式 2表示会被持久化
                .priority(1) // 优先级
                .userId("hidden")
                .expiration("60000") // 超时时间
                .build();
        channel.basicPublish(
                EXCHANGE_NAME, // 交换器名称，指明消息要被发送到哪个交换器中。如果设置为空字符串，则消息会被发送到RabbitMq默认的交换器中
                ROUTING_KEY, // 路由键
                true, // 当mandatory参数设置为true时，交换器无法根据自身的类型和路由键找到一个符合的队列，那么会将消息返回给生产者。如果设置为false，那么直接丢弃
                false, // (RabbitMQ 3.0已经移除支持)当immediate设置为true时，如果交换器在将消息路由到队列时发现队列上不存在任何消费者，那么这条消息不会存入到队列中，而会返回给生产者
                properties, // 消息的属性集
                message.getBytes() // 消息体
        );
        // 关闭资源
        channel.close();
        connection.close();
    }


}
