package org.chenguoyu.simple;

import com.rabbitmq.client.*;
import org.chenguoyu.util.ConnectionUtils;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class RabbitConsumer {
    private static final String QUEUE_NAME = "queue_demo";

    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
        Connection connection = ConnectionUtils.getConnection();
        final Channel channel = connection.createChannel();
        // 设置客户端最多接收未被ack的消息个数
        channel.basicQos(64);
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                System.out.println("recv message: " + new String(body));
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                channel.basicAck(envelope.getDeliveryTag(), false);
            }

            /**
             * Channel或者Connection关闭的时候会调用。
             * @param consumerTag
             * @param sig
             */
            @Override
            public void handleShutdownSignal(String consumerTag, ShutdownSignalException sig) {
                super.handleShutdownSignal(consumerTag, sig);
            }

            /**
             *会在其他方法之前调用
             * @param consumerTag
             */
            @Override
            public void handleConsumeOk(String consumerTag) {
                super.handleConsumeOk(consumerTag);
            }

            /**
             * 取消订阅的时候调用
             * @param consumerTag
             */
            @Override
            public void handleCancelOk(String consumerTag) {
                super.handleCancelOk(consumerTag);
            }

            /**
             * 取消订阅的时候调用
             * @param consumerTag
             */
            @Override
            public void handleCancel(String consumerTag) throws IOException {
                super.handleCancel(consumerTag);
            }
        };
        channel.basicConsume(
                QUEUE_NAME, // 队列名称
                false, // 是否自动确认 如果是true，RabbitMQ会自动把发送出去的消息设置为确认，然后从内存中删除，而不管消费者是否真正消费了这些信息
                "myConsumerTag", // 消费者标签，用来区分多个消费者
                false,  // 设置为true表示不能将同一个Connection中生产者发送的消息传送给这个Connection中的消费者
                false,  // 设置消费者的排他参数
                null, // 设置消费者的其他参数
                consumer // 设置消费者的回调函数。用来处理RabbitMQ推送过来的消息，比如DefaultConsumer，使用时需要客户端重写其中的方法
        );
        // 等待函数回调后关闭资源
        TimeUnit.SECONDS.sleep(5);
        channel.close();
        connection.close();
    }


}
