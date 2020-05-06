package org.chenguoyu.util;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class ConnectionUtils {
    private static final String IP_ADDRESS = "192.168.223.128";
    private static final int PORT = 5672;

    public static Connection getConnection(){
        // 创建链接工厂
        ConnectionFactory connectionFactory = new ConnectionFactory();
        // 主机地址
        connectionFactory.setHost(IP_ADDRESS);
        // 连接端口 默认是 5672
        connectionFactory.setPort(PORT);
        // 设置虚拟主机 默认是 /
        connectionFactory.setVirtualHost("/default");
        // 用户名 默认是guest
        connectionFactory.setUsername("guest");
        // 密码 默认是guest
        connectionFactory.setPassword("guest");
        // 创建连接
        Connection connection = null;
        try {
            // 创建连接
            connection = connectionFactory.newConnection();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        return connection;
    }
}
