# RabbitMQ学习笔记

## 以3.8.9为例

# 目标

1. 使用消息队列用于秒杀系统流量削峰、异步处理、**应用解耦**
2. 集群和消息数据同步
3. 队列和消息的持久化

# 疑问

> 1. rabbitmq如何保护自己服务？假如是暴露在外网，被人知道IP和端口，岂不是能被人使用？-- 需要用户名和虚拟主机绑定一起。
>
> 2. 虚拟主机和队列有什么关系？
>
> 3. C++能使用rabbitmq吗？
>
> 4. 如何保证消息不丢失？
>
> 5. 如何保证消费不重复？
>
> 6. 有几种工作模式？
>
> 7. 消息确认机制是什么？
>
> 8. 如何保证队列持久化和消息**持久化**？
>
>    **--队列设置durable:ture，**
>
>    -**-消息设置messageproperties.persistent_text_plain**
>
> 9. **假如某个消费者收到N个消息，在处理完N-M（N>M）个消息的时候，此消费者宕机，如何把该消费者剩余未处理的消息派发给其他消费者？**
>
>    > 处理：
>    >
>    > 1. 先把自动确认机制关了，autoACK=true
>    > 2. 告诉通道一次只能消费一个消息：channel.basicQos(1)
>    > 3. 手动确认消息：channel.basicACK(envelope.getDeliveryTag(),false)，-参数1：手动确认标识，参数2：每次确认一个
>
> 10. 在广播模型中，有人说consumer不用绑定exchange和queue，怎么理解？
>
> 11. **集群数据同步只能同步交换机，没办法同步队列和消息？**--普通集群而言
>
> 12. **发布/订阅模式，默认没有exchange没有持久化，重启rabbitmq之后就丢失消息，怎么办？**
>
> 13. **集群的时候如何查看主节点是哪台机器？**
>
> 14. 集群出现个大问题：明明指定rabbit@mq138作为master，最后为什么变成了mq140是master！

# 犯错

> 1. 有些参数不能为null，但是可以是“”
>
>    ```markdown
>    java.lang.IllegalStateException: Invalid configuration: 'exchange' must be non-null.
>    	at com.rabbitmq.client.impl.AMQImpl$Basic$Publish.<init>(AMQImpl.java:3299)
>    	at com.rabbitmq.client.AMQP$Basic$Publish$Builder.build(AMQP.java:1255)
>    	at com.rabbitmq.client.impl.ChannelN.basicPublish(ChannelN.java:708)
>    	at com.rabbitmq.client.impl.ChannelN.basicPublish(ChannelN.java:685)
>    	at com.rabbitmq.client.impl.ChannelN.basicPublish(ChannelN.java:675)
>    	at com.rabbitmq.client.impl.recovery.AutorecoveringChannel.basicPublish(AutorecoveringChannel.java:206)
>    	at com.xywei.rabbitmq.helloworld.Consumer.produce(Consumer.java:26)
>    	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
>    	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
>    ```
>
>    
>
> 2. 如果生产者声明的和消费者绑定的队列信息不一致，就会报错，信息如下：
>
>    ```markdown
>    Exception in thread "main" java.io.IOException
>    	at com.rabbitmq.client.impl.AMQChannel.wrap(AMQChannel.java:129)
>    	at com.rabbitmq.client.impl.AMQChannel.wrap(AMQChannel.java:125)
>    	at com.rabbitmq.client.impl.AMQChannel.exnWrappingRpc(AMQChannel.java:147)
>    	at com.rabbitmq.client.impl.ChannelN.queueDeclare(ChannelN.java:968)
>    	at com.rabbitmq.client.impl.recovery.AutorecoveringChannel.queueDeclare(AutorecoveringChannel.java:342)
>    	at com.xywei.rabbitmq.helloworld.Consumer.main(Consumer.java:19)
>    Caused by: com.rabbitmq.client.ShutdownSignalException: channel error; protocol method: #method<channel.close>(reply-code=406, reply-text=PRECONDITION_FAILED - inequivalent arg 'durable' for queue 'helloworld' in vhost 'rabbitmq': received 'false' but current is 'true', class-id=50, method-id=10)
>    	at com.rabbitmq.utility.ValueOrException.getValue(ValueOrException.java:66)
>    	at com.rabbitmq.utility.BlockingValueOrException.uninterruptibleGetValue(BlockingValueOrException.java:36)
>    	at com.rabbitmq.client.impl.AMQChannel$BlockingRpcContinuation.getReply(AMQChannel.java:502)
>    	at com.rabbitmq.client.impl.AMQChannel.privateRpc(AMQChannel.java:293)
>    	at com.rabbitmq.client.impl.AMQChannel.exnWrappingRpc(AMQChannel.java:141)
>    	... 3 more
>    Caused by: com.rabbitmq.client.ShutdownSignalException: channel error; protocol method: #method<channel.close>(reply-code=406, reply-text=PRECONDITION_FAILED - inequivalent arg 'durable' for queue 'helloworld' in vhost 'rabbitmq': received 'false' but current is 'true', class-id=50, method-id=10)
>    	at com.rabbitmq.client.impl.ChannelN.asyncShutdown(ChannelN.java:517)
>    	at com.rabbitmq.client.impl.ChannelN.processAsync(ChannelN.java:341)
>    	at com.rabbitmq.client.impl.AMQChannel.handleCompleteInboundCommand(AMQChannel.java:182)
>    	at com.rabbitmq.client.impl.AMQChannel.handleFrame(AMQChannel.java:114)
>    	at com.rabbitmq.client.impl.AMQConnection.readFrame(AMQConnection.java:739)
>    	at com.rabbitmq.client.impl.AMQConnection.access$300(AMQConnection.java:47)
>    	at com.rabbitmq.client.impl.AMQConnection$MainLoop.run(AMQConnection.java:666)
>    	at java.lang.Thread.run(Thread.java:748)
>    
>    
>    ```
>
> 3. **在helloworld模型中，明明声明了队列持久化true，为什么重启后还会没持久化消息？**
>
>    --区分队列、消息持久化
>
>    --持久化队列：durable=true
>
>    --持久化消息：
>
> 4. 队列已经存在了，**下次声明的时候修改了是否自动删除、是否持久化等属性的时候，就会报错：**
>
>    ```markdown
>    java.io.IOException
>    	at com.rabbitmq.client.impl.AMQChannel.wrap(AMQChannel.java:129)
>    	at com.rabbitmq.client.impl.AMQChannel.wrap(AMQChannel.java:125)
>    	at com.rabbitmq.client.impl.AMQChannel.exnWrappingRpc(AMQChannel.java:147)
>    	at com.rabbitmq.client.impl.ChannelN.queueDeclare(ChannelN.java:968)
>    	at com.rabbitmq.client.impl.recovery.AutorecoveringChannel.queueDeclare(AutorecoveringChannel.java:342)
>    Caused by: com.rabbitmq.client.ShutdownSignalException: channel error; protocol method: #method<channel.close>(reply-code=406, reply-text=PRECONDITION_FAILED - inequivalent arg 'durable' for queue 'helloworld' in vhost 'rabbitmq': received 'false' but current is 'true', class-id=50, method-id=10)
>    	at com.rabbitmq.client.impl.ChannelN.asyncShutdown(ChannelN.java:517)
>    	at com.rabbitmq.client.impl.ChannelN.processAsync(ChannelN.java:341)
>    	at com.rabbitmq.client.impl.AMQChannel.handleCompleteInboundCommand(AMQChannel.java:182)
>    	at com.rabbitmq.client.impl.AMQChannel.handleFrame(AMQChannel.java:114)
>    	at com.rabbitmq.client.impl.AMQConnection.readFrame(AMQConnection.java:739)
>    	at com.rabbitmq.client.impl.AMQConnection.access$300(AMQConnection.java:47)
>    	at com.rabbitmq.client.impl.AMQConnection$MainLoop.run(AMQConnection.java:666)
>    	at java.lang.Thread.run(Thread.java:748)
>    
>    ```
>
> 5. 搭建集群的时候出现以下错误，**原因：主机名没有配置好**
>
>    ```shell
>    [xy@static-140 ~ 15:11:10]$ sudo rabbitmqctl join_cluster rabbit@mq138
>    Error: unable to perform an operation on node 'rabbit@static-140'. Please see diagnostics information and suggestions below.
>    
>    Most common reasons for this are:
>    
>     * Target node is unreachable (e.g. due to hostname resolution, TCP connection or firewall issues)
>     * CLI tool fails to authenticate with the server (e.g. due to CLI tool's Erlang cookie not matching that of the server)
>     * Target node is not running
>    
>    In addition to the diagnostics info below:
>    
>     * See the CLI, clustering and networking guides on https://rabbitmq.com/documentation.html to learn more
>     * Consult server logs on node rabbit@static-140
>     * If target node is configured to use long node names, don't forget to use --longnames with CLI tools
>    
>    DIAGNOSTICS
>    ===========
>    
>    attempted to contact: ['rabbit@static-140']
>    
>    rabbit@static-140:
>      * connected to epmd (port 4369) on static-140
>      * epmd reports: node 'rabbit' not running at all
>                      no other nodes on static-140
>      * suggestion: start the node
>    
>    Current node details:
>     * node name: 'rabbitmqcli-3856-rabbit@static-140'
>     * effective user's home directory: /var/lib/rabbitmq
>     * Erlang cookie hash: iMgXpnz9L+Ygr6nF0Lts9A==
>     
>     
>    ```
>
>    ```shell
>    [xy@server-139 ~ 15:12:45]$ sudo rabbitmqctl join_cluster rabbit@mq138
>    Error: unable to perform an operation on node 'rabbit@server-139'. Please see diagnostics information and suggestions below.
>    
>    Most common reasons for this are:
>    
>     * Target node is unreachable (e.g. due to hostname resolution, TCP connection or firewall issues)
>     * CLI tool fails to authenticate with the server (e.g. due to CLI tool's Erlang cookie not matching that of the server)
>     * Target node is not running
>    
>    In addition to the diagnostics info below:
>    
>     * See the CLI, clustering and networking guides on https://rabbitmq.com/documentation.html to learn more
>     * Consult server logs on node rabbit@server-139
>     * If target node is configured to use long node names, don't forget to use --longnames with CLI tools
>    
>    DIAGNOSTICS
>    ===========
>    
>    attempted to contact: ['rabbit@server-139']
>    
>    rabbit@server-139:
>      * connected to epmd (port 4369) on server-139
>      * epmd reports: node 'rabbit' not running at all
>                      no other nodes on server-139
>      * suggestion: start the node
>    
>    Current node details:
>     * node name: 'rabbitmqcli-3148-rabbit@server-139'
>     * effective user's home directory: /var/lib/rabbitmq
>     * Erlang cookie hash: iMgXpnz9L+Ygr6nF0Lts9A==
>    
>    ```
>
>    

# 总结

> 1. 涉及跨系统通信的时候，首选，消息队列
>
> 2. 虚拟主机就类似数据库中的一个数据库，每个项目对应单独的virtual host
>
> 3. 使用rabbitmq过程
>
>    创建用户和虚拟主机，然后两者绑定，生产者使用用户和密码连接虚拟主机，接着消费者给exchange发送消息或者也可以直接给队列发送消息，最后消费者拿出消息
>
> 4. 不同项目创建不同的虚拟主机，就类似数据库，生产者使用消息队列，就像使用数据库
>
> 5. gues用户有无限权利
>
> 6. 生产者可以生产一次，但是消费者一般是一直监听，如果设置消费一次，一般可能来不及触发处理消费的过程，所以测试时候消费者需要用main。
>
> 7. 同一个通道可以向不同的队列发送消息，所以需要绑定routingkey，真正发布消息的是routingkey
>
> 8. 设置队列自动删除的时候，只有消费者断开连接才会删除队列
>
> 9. **消息队列把消息给消费者的时候，如果设置autoACK=true，可能出现很大的问题，比如消息全部派发给消费者了，但是消费者还没有处理完成，这时候就可能出现消息清空，造成数据丢失；假如处理到第N个的时候宕机了，也会造成数据丢失。**
>
> 10. **生产者和消费者的队列一定要相同，各种属性保持一致，否则会报错**
>
> 11. 广播模型的routingkey没有作用
>
> 12. detached启动方式，没有启动web插件，所以无法通过web页面进行管理，如果使用star_app就能使用web页面
>
> 13. **一个通道可以有多个队列**
>
> 14. **自动删除队列是在消费者断开后自动删除**
>
> 15. **消息是存在临时队列中，使用临时队列的模型都会在宕机后丢失消息**
>
> 

# 1、RabbitMQ安装

> 因为RabbitMQ是用erlang语言写的，所以需要在下载RabbitMQ安装包之后，也下载erlang依赖

## 离线安装

> 1. rabbitmq官网或者github上下载安装包：
>
>    rabbitmq-server-3.8.9-1.el7.noarch.rpm
>
>    erlang-23.2.1-1.el7.x86_64.rpm
>
> 2. 到rpmfind.net下载socat依赖：
>
>    socat-1.7.3.2-2.el7.x86_64.rpm
>
> 3. 安装
>
>    ```shell
>    sudo rpm -ivh erlang-23.2.1-1.el7.x86_64.rpm
>    sudo rpm -ivh socat-1.7.3.2-2.el7.x86_64.rpm
>    sudo rpm -ivh rabbitmq-server-3.8.9-1.el7.noarch.rpm
>    ```
>
> 4. github对应版本的docs目录下载`rabbitmq.config.example`，复制到/etc/rabbitmq目录，重命名为`rabbitmq.config`，因为新版本在/user/share/rabbitmq/下面没有相应文件，然后在loopback_users附近设置：
>
>    ```shell
>     72 ## Related doc guide: https://rabbitmq.com/access-control.html.
>     73
>     74 ## The default "guest" user is only permitted to access the server
>     75 ## via a loopback interface (e.g. localhost).
>     76 ## {loopback_users, [<<"guest">>]},
>     77 loopback_users = none
>     78 ##
>     79 # loopback_users.guest = true
>    
>    ```
>
> 5. 启用web管理插件
>
>    ```shell
>    sudo rabbitmq-plugins enable rabbitmq_management
>    ```
>
> 6. 启动rabbitmq
>
>    ```shell
>    sudo systemctl start rebbitmq-server
>    ```
>
> 7. 浏览器输入IP:15672，输入guest/guest，能访问表示安装成功

# 2、RabbitMQ配置管理

> rabbitmq不仅可以使用web页面进行管理，也能使用命令行方式来管理

## 1、web页面管理rabbitmq

> 1. amdin用户和虚拟主机管理
>    - 添加用户
>    - 创建虚拟主机
>    - 锁定虚拟主机和用户

## 2、命令行管理rabbitmq

```markdown
# 查看帮助
rabbitmqctl help
```



## 3、插件管理

```markdown
# 查看有哪些操作
rabbitmq-plugins

# 哪些操作具体命令
rabbitmq-plugins help <command>
```



# 3、消息模型

> 有哪些消息模型？
>
> 发布确认模型只有在3.8版本之后才支持
>
> 生产者可以直接发送消息到队列

## 1、直连模型helloworld

**没有直接exchange参与，而是采用默认的exchange**

点对点，一对一，生产和消费

**场景：登录后的邮件、短信其中之一发送**

缺陷：因为消费多慢，导致消息堆积

## 2、工作队列work queues

**没有直接exchange参与，而是采用默认的exchange**

也叫任务模型

在helloworld模型基础上直接添加一个consumer即使work queues

**默认是轮询策略**

**修改为能者多劳模型步骤：【在consumer端修改】**

> ```java
> channel.basicQos(1);
> channel.basicConsume(CommonVariable.WORK_QUEUES, false, new DefaultConsumer(channel) {
>  public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body){
>      System.out.println("consumer2收到消息, " + new String(body));
>      //假如运行在这里的时候，消费者宕机了，消息没有发送确认，不会造成消息丢失，但是，
>      //可能会有重复处理其他业务的情况
>      channel.basicAck(envelope.getDeliveryTag(), false);
>  }
> });
> ```
>
> 

## 3、fanout

用到exchange

扇出，也就是广播模型

模型：一个exchange对应多条队列，一条队列对应一个消费者，生产者无法决定消息发送给哪个队列，而是由交换机来确定，队列没必要是持久化的，一般是临时的。

场景：登录后，既要积分，也要同时发送短信和邮件；下单后既要设计订单系统也要涉及库存系统

## 4、routing

**关键点：routing key**

### 1、模式一：直接模型，direct

exchange类型为direct

希望不同的消息被不同的队列接收

一条通道能绑定多条队列

队列都是自动删除，排它的

### 2、模式二：订阅模型，topics

exchange类型为topic

routingkey使用：

1. 消费者端使用通配符：*.user.#

2. 生产者使用具体的主题：beijing.user/beijing.user.news/user.news
3. *：有且只有一个单词：\*.user匹配beijing.user，不匹配a.beijing.user
4. #：可以有0个，也可以有一个，还能有多少个：#.user匹配user/beijing.user/a.beijing.user

# 4、消息确认机制

autoack=true，造成数据丢失

# 5、整合spring boot

必须先有消费者，否则即使生产者执行多少次，都不会产生消息

# 6、RabbitMQ场景使用

## 1、异步处理

## 2、流量削峰

## 3、应用解耦

# 7、RabbitMQ集群

**测试的版本是3.8.9**

**集群里面不推荐使用systemctl 方式启动rabbitmq**

> 默认，数据是在master，其他slave节点没有数据，但是可以访问到，即使master宕机，slave也没法顶替，这种模式仅仅是能看到的主从复制，非真正的复制数据，没有高可用。
>
> **集群之前必须要保证/var/lib/rabbitmq/.erlang.cookie是相同的**

## 1、普通集群

> 默认的模式，仅仅主从复制，非真正的复制，数据还是在master上
>
> 只有master活着，slave才能正常对外提供服务。
>
> **准备环境：**
>
> > 三台虚拟机:
> >
> > mq138: 192.168.11.138(master)
> >
> > mq139: 192.168.11.139(slave)
> >
> > mq140: 192.168.11.140(slave)
> >
> > rabbitmq安装包：
> >
> > erlang-23.2.1-1.el7.x86_64.rpm
> >
> > socat-1.7.3.2-2.el7.x86_64.rpm
> >
> > rabbitmq-server-3.8.9-1.el7.noarch.rpm
>
> **搭建步骤：**
>
> 1. 每台虚拟机上都安装配置好rabbitmq，设置主机名分别为mq138/mq139/mq140
>
> 2. 每台主机上都配置hosts映射:
>
>    ```markdown
>    192.168.11.138 mq138
>    192.168.11.139 mq139
>    192.168.11.140 mq140
>    ```
>
> 3. 保持erlang cookie一致，以mq138机器的.erlang.cookie为主，复制给mq139/mq140，检查需要保证一致
>
>    ```markdown
>     cat /var/lib/rabbitmq/.erlang.cookie
>    ```
>
> 4. 启动mq138/139/40，以后台方式启动，查看node名字
>
>    ```markdo
>    sudo rabbitmq-server -detached
>    ```
>
> 5. mq139/mq140都执行以下命令，加入mq138中：（rabbit@mq138是固定写法：rabbit@主机名）
>
>    ```markdown
>    sudo rabbitmqctl stop_app 
>    sudo rabbitmqctl join_cluster rabbit@mq138
>    sudo rabbitmqctl start_app
>    ```
>
> 6. 查看集群状态
>
>    ```markdown
>    sudo rabbitmqctl cluster_status
>    
>    显示如下表示配置集群OK
>    Cluster status of node rabbit@mq138 ...
>    Basics
>    
>    Cluster name: rabbit@mq138
>    
>    Disk Nodes
>    
>    rabbit@mq138
>    rabbit@mq139
>    rabbit@mq140
>    
>    Running Nodes
>    
>    rabbit@mq138
>    rabbit@mq139
>    rabbit@mq140
>    
>    Versions
>    
>    rabbit@mq138: RabbitMQ 3.8.9 on Erlang 23.2.1
>    rabbit@mq139: RabbitMQ 3.8.9 on Erlang 23.2.1
>    rabbit@mq140: RabbitMQ 3.8.9 on Erlang 23.2.1
>    
>    Maintenance status
>    
>    Node: rabbit@mq138, status: not under maintenance
>    Node: rabbit@mq139, status: not under maintenance
>    Node: rabbit@mq140, status: not under maintenance
>    
>    Alarms
>    
>    (none)
>    
>    Network Partitions
>    
>    (none)
>    
>    Listeners
>    
>    Node: rabbit@mq138, interface: [::], port: 15672, protocol: http, purpose: HTTP API
>    Node: rabbit@mq138, interface: [::], port: 25672, protocol: clustering, purpose: inter-node and CLI tool communication
>    Node: rabbit@mq138, interface: [::], port: 5672, protocol: amqp, purpose: AMQP 0-9-1 and AMQP 1.0
>    Node: rabbit@mq139, interface: [::], port: 15672, protocol: http, purpose: HTTP API
>    Node: rabbit@mq139, interface: [::], port: 25672, protocol: clustering, purpose: inter-node and CLI tool communication
>    Node: rabbit@mq139, interface: [::], port: 5672, protocol: amqp, purpose: AMQP 0-9-1 and AMQP 1.0
>    Node: rabbit@mq140, interface: [::], port: 15672, protocol: http, purpose: HTTP API
>    Node: rabbit@mq140, interface: [::], port: 25672, protocol: clustering, purpose: inter-node and CLI tool communication
>    Node: rabbit@mq140, interface: [::], port: 5672, protocol: amqp, purpose: AMQP 0-9-1 and AMQP 1.0
>    
>    Feature flags
>    
>    Flag: drop_unroutable_metric, state: disabled
>    Flag: empty_basic_get_metric, state: disabled
>    Flag: implicit_default_bindings, state: enabled
>    Flag: maintenance_mode_status, state: enabled
>    Flag: quorum_queue, state: enabled
>    Flag: virtual_host_metadata, state: enabled
>    
>    ```
>
> 7. **总结：**
>
>    1. **版本3.8.9配置完成后，已经能做到数据同步了，但是做不到高可用，和视频3.7版本有出入**
>
>    2. 如果master节点挂掉，启动的时候使用:的命令：
>
>       ```shell
>       sudo rabbitmq-server -detached
>       # 不能使用rabbitmqctl start_app，slave才使用rabbitmqctl start_app
>       ```
>
> 8. 这里出现个大问题：明明指定rabbit@mq138作为master，最后为什么变成了mq140是master！

## 2、镜像集群

> 在普通集群的基础上，实现高可用、队列消息同步等功能
>
> 普通集群已经实现了数据同步复制，镜像集群再实现HA
>
> 在普通集群的基础上，设置策略即可
>
> 策略：
>
> ```markdown
> rabbitmqctl set_policy [-p vhost] [--priority <priority>] [--apply-to <apply-to>] <name> <pattern> <definition>
> # -p vhost： 可选，对特定vhost的队列设置，不设置表示对vhost：/
> # priority: 可选，policy优先级
> # name：队列的名字
> # pattern：匹配的队列模式，正则匹配
> # definition：镜像定义，分三部分：ha-mode/ha-params/ha-sync-mode
> # ha-mode：
> 	all: 所有节点都进行镜像
> 	exactlly: 精确指定机器个数，个数由ha-params决定
> 	nodes: 在指定机器名字上进行镜像，名字ha-params决定
> # ha-params: ha-mode中用到的参数
> # ha-sync-mode: 队列消息的同步方式，为manual或者automatic
> ```
>
> 查看策略
>
> ```shell
> rabbitmqctl list_policies
> ```
>
> 配置策略
>
> ha-all是策略名字
>
> ```shell
> rabbitmqctl set_policy -p vhostname ha-all '^' '{"ha-mode":"all","ha-sync-mode":"automatic"}'
> # -p vhostname: 虚拟主机名字
> # ha-all: 策略名字
> # '^': 虚拟主机vhostname下面所有的队列
> # 
> ```
>
> 删除策略
>
> ```shell
> rebbitmqctl clear_policy ha-all
> ```
>
> 到此配置高可用，数据同步的rabbitmq集群OK



