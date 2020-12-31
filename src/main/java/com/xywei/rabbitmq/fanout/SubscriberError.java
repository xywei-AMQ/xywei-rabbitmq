package com.xywei.rabbitmq.fanout;

import java.io.IOException;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.xywei.rabbitmq.common.CommonVariable;
import com.xywei.rabbitmq.utils.RabbitMQUtils;

public class SubscriberError {

	public static void main(String[] args) throws Exception {

		Connection connection = RabbitMQUtils.getRabbitMQConnection();
		Channel channel = connection.createChannel();

		// 声明接受哪个路由器发送过来的消息
		channel.exchangeDeclare(CommonVariable.FANOUT_INFO, CommonVariable.EXCHANGE_TYPE_FANOUT);

		// 获取临时队列，用临时队列绑定路由器接收消息
		String queue = channel.queueDeclare().getQueue();
		channel.queueBind(queue, CommonVariable.FANOUT_ERROR, "");

		// 接收消息
		channel.basicConsume(queue, true, new DefaultConsumer(channel) {

			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body)
					throws IOException {
				try {
					Thread.sleep(5000);
					System.out.println("error接收到消息：" + new String(body));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

		});

	}

}
