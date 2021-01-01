package com.xywei.rabbitmq.fanout;

import java.io.IOException;

import org.junit.Test;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.MessageProperties;
import com.xywei.rabbitmq.common.CommonVariable;
import com.xywei.rabbitmq.utils.RabbitMQUtils;

/**
 * fanout，也就是发布/订阅模式 根据订阅的不同，接受不同的消息 这时候需要用到exchange
 * 这种有个劣势，就是exchange、queue、message会丢失
 * @author future
 * @Datetime 2020年12月31日 下午10:08:43<br/>
 * @Description
 */
public class Publisher {

	@Test
	public void send() throws Exception {
		Connection connection = RabbitMQUtils.getRabbitMQConnection();
		Channel channel = connection.createChannel();
		// 和直接路由不同，这里的队列不需要生产者声明，而是由消费者声明灵石队列，但是要声明exchange
		channel.exchangeDeclare(CommonVariable.FANOUT_DEBUG, "fanout", true);
//		channel.exchangeDeclare(CommonVariable.FANOUT_INFO, "fanout");
//		channel.exchangeDeclare(CommonVariable.FANOUT_ERROR, "fanout");
		for (int i = 0; i < 10; i++) {
			channel.basicPublish(CommonVariable.FANOUT_DEBUG, "", null, (i + ", fanout DEBUG").getBytes());
//			channel.basicPublish(CommonVariable.FANOUT_INFO, "", null, (i + ", fanout INFO").getBytes());
//			channel.basicPublish(CommonVariable.FANOUT_ERROR, "", null, (i + ", fanout ERROR").getBytes());
		}
		RabbitMQUtils.closeConnection(connection, channel);
	}
}
