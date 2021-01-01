package com.xywei.rabbitmq.routing.direct;

import java.io.IOException;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.xywei.rabbitmq.common.CommonVariable;
import com.xywei.rabbitmq.utils.RabbitMQUtils;

public class ConsumerDebug {

	public static void main(String[] args) throws Exception {
		Connection connection = RabbitMQUtils.getRabbitMQConnection();
		Channel channel = connection.createChannel();
		channel.exchangeDeclare(CommonVariable.EXCHANGE_NAME_DEBUG, CommonVariable.ROUTING_TYPE_DIRECT);
		String queue = channel.queueDeclare().getQueue();
		channel.queueBind(queue, CommonVariable.EXCHANGE_NAME_DEBUG, CommonVariable.ROUTING_KEY_DEBUG);
		channel.queueBind(queue, CommonVariable.EXCHANGE_NAME_DEBUG, CommonVariable.ROUTING_KEY_ERROR);
		channel.basicConsume(queue, true, new DefaultConsumer(channel) {

			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body)
					throws IOException {
				System.out.println("debug consumer收到: " + new String(body));
			}

		});
	}
}
