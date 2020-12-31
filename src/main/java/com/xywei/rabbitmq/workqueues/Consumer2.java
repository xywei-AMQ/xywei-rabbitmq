package com.xywei.rabbitmq.workqueues;

import java.io.IOException;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.xywei.rabbitmq.common.CommonVariable;
import com.xywei.rabbitmq.utils.RabbitMQUtils;

public class Consumer2 {

	public static void main(String[] args) throws Exception {
		Connection connection = RabbitMQUtils.getRabbitMQConnection();
		final Channel channel = connection.createChannel();
		channel.basicQos(1);
		channel.queueDeclare(CommonVariable.WORK_QUEUES, true, false, false, null);
		channel.basicConsume(CommonVariable.WORK_QUEUES, false, new DefaultConsumer(channel) {

			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body)
					throws IOException {
				System.out.println("consumer2收到消息, " + new String(body));
				try {
					Thread.sleep(8000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				channel.basicAck(envelope.getDeliveryTag(), false);
			}

		});
	}

}
