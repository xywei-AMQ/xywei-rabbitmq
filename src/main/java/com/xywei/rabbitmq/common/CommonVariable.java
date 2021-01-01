package com.xywei.rabbitmq.common;

public class CommonVariable {

	public static String EXCHANGE_TYPE_FANOUT = "fanout";

	public static String HELLO_WORLD_QUEUE_NAME = "helloworld";
	public static String HELLO_WORLD_QUEUE_NAME_DURABLE_FALSE = "helloworld_druable_false";
	public static String HELLO_WORLD_QUEUE_NAME_AUTODELETE = "helloworld_autodelete";

	public static String WORK_QUEUES = "work-queue";

	public static String FANOUT_INFO = "INFO";
	public static String FANOUT_ERROR = "ERROR";
	public static String FANOUT_DEBUG = "DEBUG";

	public static String ROUTING_TYPE_DIRECT = "direct";
	public static String ROUTING_TYPE_TOPIC = "topic";

	public static String EXCHANGE_NAME_INFO = "info";
	public static String EXCHANGE_NAME_DEBUG = "debug";
	public static String EXCHANGE_NAME_ERROR = "error";
	
	public static String ROUTING_KEY_INFO="info";
	public static String ROUTING_KEY_DEBUG = "dubug";
	public static String ROUTING_KEY_ERROR="error";
	

}
