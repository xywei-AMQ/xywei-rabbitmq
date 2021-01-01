package com.xywei.rabbitmq.routing.topics;

import org.junit.Test;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.xywei.rabbitmq.utils.RabbitMQUtils;

/**
 * 全国用户的天气预报
 * 
 * @author future
 * @Datetime 2021年1月1日 下午4:14:29<br/>
 * @Description
 */
public class ProviderUserWeather {

	@Test
	public void send() throws Exception {

		String exchange = "user.topics";
		String type = "topic";
		String routingKey = "user.weather";

		Connection connection = RabbitMQUtils.getRabbitMQConnection();
		Channel channel = connection.createChannel();
		channel.exchangeDeclare(exchange, type);
		channel.basicPublish(exchange, routingKey, null, "全国天气通知".getBytes());

		RabbitMQUtils.closeConnection(connection, channel);
	}
}
