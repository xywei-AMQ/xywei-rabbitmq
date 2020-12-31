package com.xywei.rabbitmq.helloworld;

import java.io.IOException;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.xywei.rabbitmq.common.CommonVariable;
import com.xywei.rabbitmq.utils.RabbitMQUtils;

public class Consumer {

	public static void main(String[] args) throws Exception {

		Connection connection = RabbitMQUtils.getRabbitMQConnection();
		Channel channel = connection.createChannel();
		channel.queueDeclare(CommonVariable.HELLO_WORLD_QUEUE_NAME_AUTODELETE, true, false, true, null);
		channel.basicConsume(CommonVariable.HELLO_WORLD_QUEUE_NAME_AUTODELETE, true, new DefaultConsumer(channel) {

			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body)
					throws IOException {
				System.out.println("consumer收到消息：" + new String(body));
			}

		});
	}

}
