package com.xywei.rabbitmq.routing.topics;

import java.io.IOException;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.xywei.rabbitmq.utils.RabbitMQUtils;

/**
 * 全国用户，只能收到全国的天气通知
 * 
 * @author future
 * @Datetime 2021年1月1日 下午4:21:17<br/>
 * @Description
 */
public class ConsumerUserWeather {

	public static void main(String[] args) throws Exception {

		String exchange = "user.topics";
		String type = "topic";
		//关键点，全国用户只能收到全国的天气通知
		String routingKey = "user.weather";

		Connection connection = RabbitMQUtils.getRabbitMQConnection();
		Channel channel = connection.createChannel();
		channel.exchangeDeclare(exchange, type);
		String queue = channel.queueDeclare().getQueue();
		channel.queueBind(queue, exchange, routingKey);
		channel.basicConsume(queue, true, new DefaultConsumer(channel) {

			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body)
					throws IOException {
				System.out.println("全国用户收到：" + new String(body));
			}

		});

	}

}
