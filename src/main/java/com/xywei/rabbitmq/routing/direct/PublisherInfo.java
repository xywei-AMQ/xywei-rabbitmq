package com.xywei.rabbitmq.routing.direct;

import org.junit.Test;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.xywei.rabbitmq.common.CommonVariable;
import com.xywei.rabbitmq.utils.RabbitMQUtils;

/**
 * 路由直接模式，生产者与通道通过routingkey绑定来发布消息
 * 
 * @author future
 * @Datetime 2021年1月1日 下午1:41:50<br/>
 * @Description
 */
public class PublisherInfo {

	@Test
	public void send() throws Exception {

		Connection connection = RabbitMQUtils.getRabbitMQConnection();
		Channel channel = connection.createChannel();
		channel.exchangeDeclare(CommonVariable.EXCHANGE_NAME_INFO, CommonVariable.ROUTING_TYPE_DIRECT);
		channel.basicPublish(CommonVariable.EXCHANGE_NAME_INFO, CommonVariable.ROUTING_KEY_INFO, null,
				"info message".getBytes());

		RabbitMQUtils.closeConnection(connection, channel);
	}

}
