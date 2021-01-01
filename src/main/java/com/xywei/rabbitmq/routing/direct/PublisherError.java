package com.xywei.rabbitmq.routing.direct;

import java.io.IOException;

import org.junit.Test;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.xywei.rabbitmq.common.CommonVariable;
import com.xywei.rabbitmq.utils.RabbitMQUtils;

public class PublisherError {
	
	@Test
	public void send() throws Exception {
		Connection connection = RabbitMQUtils.getRabbitMQConnection();
		Channel channel = connection.createChannel();
		channel.exchangeDeclare(CommonVariable.EXCHANGE_NAME_ERROR, CommonVariable.ROUTING_TYPE_DIRECT);
		channel.basicPublish(CommonVariable.EXCHANGE_NAME_ERROR, CommonVariable.ROUTING_KEY_ERROR, null, "error".getBytes());
		RabbitMQUtils.closeConnection(connection, channel);
	}

}
