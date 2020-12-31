package com.xywei.rabbitmq.test;

import org.junit.Test;

import com.rabbitmq.client.Connection;
import com.xywei.rabbitmq.utils.RabbitMQUtils;

public class TestConnection {

	@Test
	public void testConnection() {
		Connection connection = RabbitMQUtils.getRabbitMQConnection();
		System.out.println(connection);
	}
}
