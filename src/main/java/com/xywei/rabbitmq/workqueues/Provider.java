package com.xywei.rabbitmq.workqueues;

import java.io.IOException;
import java.util.Date;

import org.junit.Test;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.MessageProperties;
import com.xywei.rabbitmq.common.CommonVariable;
import com.xywei.rabbitmq.utils.RabbitMQUtils;

/**
 * 假如生产者生产的消息过多，消费者消费不过，那么就需要多个消费者来消费
 * 
 * @author future
 * @Datetime 2020年12月31日 下午9:25:02<br/>
 * @Description
 */
public class Provider {

	@Test
	public void send() throws Exception {

		Connection connection = RabbitMQUtils.getRabbitMQConnection();
		Channel channel = connection.createChannel();

		channel.queueDeclare(CommonVariable.WORK_QUEUES, true, false, false, null);

		for (int i = 0; i < 20; i++) {

			channel.basicPublish("", CommonVariable.WORK_QUEUES, MessageProperties.PERSISTENT_TEXT_PLAIN,
					(i + ", work-queue, time+" + new Date()).getBytes());

		}

		RabbitMQUtils.closeConnection(connection, channel);
	}
}
