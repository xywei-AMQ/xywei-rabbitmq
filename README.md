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
> 8. 如何保证队列持久化和消息持久化？
>
>    --队列设置durable:ture，
>
>    --消息设置messageproperties.persistent_text_plain
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
> 12. 

# 犯错

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
> 13. 

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

## 2、工作队列work queue

**没有直接exchange参与，而是采用默认的exchange**

也叫任务模型

在helloworld模型基础上直接添加一个consumer即可

**默认是轮询策略**

## 3、fanout

用到exchange

扇出，也就是广播模型

模型：一个exchange对应多条队列，一条队列对应一个消费者，生产者无法决定消息发送给哪个队列，而是由交换机来确定，队列没必要是持久化的，一般是临时的。

场景：登录后，既要积分，也要同时发送短信和邮件；下单后既要设计订单系统也要涉及库存系统

## 4、routing

**关键点：routing key**

### 1、模式一：直接模型，direct

希望不同的消息被不同的队列接收

### 2、模式二：订阅模型，topic

# 4、消息确认机制

autoack=true，造成数据丢失

# 5、整合spring boot

必须先有消费者，否则即使生产者执行多少次，都不会产生消息

# 6、RabbitMQ场景使用

## 1、异步处理

## 2、流量削峰

## 3、应用解耦

# 7、RabbitMQ集群

> 默认，数据是在master，其他slave节点没有数据，但是可以访问到，即使master宕机，slave也没法顶替，这种模式仅仅是能看到的主从复制，非真正的复制数据，没有高可用。
>
> **集群之前必须要保证/var/lib/rabbitmq/.erlang.cookie是相同的**

## 1、普通集群

> 默认的模式，仅仅主从复制，非真正的复制，数据还是在master上
>
> 只有master活着，slave才能正常对外提供服务。
>
> 

## 2、镜像集群

> 在普通集群的基础上，实现高可用、队列消息同步等功能
>
> 