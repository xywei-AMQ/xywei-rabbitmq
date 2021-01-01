package com.xywei.rabbitmq.routing.direct;

import java.io.IOException;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.xywei.rabbitmq.common.CommonVariable;
import com.xywei.rabbitmq.utils.RabbitMQUtils;

/**
 * 消费者依然是需要用临时队列绑定交换机
 * error只能收到error的消息
 * @author future
 * @Datetime 2021年1月1日 下午2:59:37<br/>
 * @Description
 */
public class ConsumeError {

	public static void main(String[] args) throws Exception {
		Connection connection = RabbitMQUtils.getRabbitMQConnection();
		Channel channel = connection.createChannel();
		channel.exchangeDeclare(CommonVariable.EXCHANGE_NAME_ERROR, CommonVariable.ROUTING_TYPE_DIRECT);
		String queueName = channel.queueDeclare().getQueue();
		channel.queueBind(queueName, CommonVariable.EXCHANGE_NAME_ERROR, CommonVariable.ROUTING_KEY_ERROR);
		channel.basicConsume(queueName, true, new DefaultConsumer(channel) {

			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body)
					throws IOException {
				System.out.println("error consumer收到：" + new String(body));
			}

		});

	}

}
