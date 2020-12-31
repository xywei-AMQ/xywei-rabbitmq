package com.xywei.rabbitmq.helloworld;

import java.io.IOException;
import java.util.Calendar;

import org.junit.Test;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.MessageProperties;
import com.xywei.rabbitmq.common.CommonVariable;
import com.xywei.rabbitmq.utils.RabbitMQUtils;

public class Provider {

	@Test
	public void testProduce() throws IOException {

		String msg = "helloworld, " + Calendar.getInstance().getTime();

		// 创建连接
		Connection connection = RabbitMQUtils.getRabbitMQConnection();
		// 创建通道
		Channel channel = connection.createChannel();

		/**
		 * 声明队列，一个通道可以有多少个队列 </br>
		 * 1. 参数1：队列名字 2. 参数2：队列是否持久化 3. 参数3：队列是否只能被本连接使用 4. 参数4：消费完成后是否自动删除队列 5. 参数5：
		 */
		channel.queueDeclare(CommonVariable.HELLO_WORLD_QUEUE_NAME_AUTODELETE, true, false, true, null);

		/**
		 * 通道根据绑定的队列名字，发布消息 </br>
		 * 1. exchange 没有用到，使用的是默认路由，不能为null 2. routingKey 使用默认路由的时候，和队列名字一致 3. props
		 * 额外属性 4. body 需要产生的小心
		 * 
		 */
		channel.basicPublish("", CommonVariable.HELLO_WORLD_QUEUE_NAME_AUTODELETE, MessageProperties.PERSISTENT_TEXT_PLAIN, msg.getBytes());

		RabbitMQUtils.closeConnection(connection, channel);

	}
}
