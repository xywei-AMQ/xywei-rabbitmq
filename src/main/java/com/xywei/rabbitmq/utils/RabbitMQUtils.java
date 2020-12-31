package com.xywei.rabbitmq.utils;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class RabbitMQUtils {

	private static ConnectionFactory connectionFactory = new ConnectionFactory();
	private static Connection connection = null;
	private static String host = "192.168.11.140";
	private static int port = 5672;
	private static String virtualHost = "rabbitmq";
	private static String username = "helloworld";
	private static String password = "helloworld";

	public static Connection getRabbitMQConnection() {
		connectionFactory.setHost(host);
		connectionFactory.setPort(port);
		connectionFactory.setVirtualHost(virtualHost);
		connectionFactory.setUsername(username);
		connectionFactory.setPassword(password);
		try {
			connection = connectionFactory.newConnection();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TimeoutException e) {
			e.printStackTrace();
		}
		return connection == null ? null : connection;
	}

	public static void closeConnection(Connection connection, Channel channel) {

		try {
			if (channel != null) {
				channel.close();
			}
			if (connection != null) {
				connection.close();
			}


		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
