# 高并发网络编程

[TOC]

## TCP/UDP协议

### OSI网络七层模型

​	为使不同计算机厂家的计算机能够互相通信，以便在更大范围内建立计算机网络，有必要建立一个国际范围的网络体系结构标准。

![](http://prvyof0n9.bkt.clouddn.com/net1.png)

### 各层的主要功能

低三层：屏蔽底层网络复杂性。

- `物理层`：使原始的数据比特流能在物理介质上传输。
- `数据链路层`：通过校验、确认和反馈重发等手段，形成稳定的数据链路。
- `网络层`：进行路由选择和流量控制。(IP协议)



- `传输层`：提供可靠的端口到端口的数据传输服务(TPC/UDP协议)。

高三层：

- `会话层`：负责建立、管理和终止进程之间的会话和数据交换。
- `表示层`：负责数据格式转换、数据加密与解密、压缩与解压缩等。
- `应用层`：为用户的应用进程提供网络服务。

### 传输控制协议TCP

​	传输控制协议是Internet一个重要的传输层协议。TCP提供面向连接、可靠、有序、字节流传输服务。应用程序在使用TCP之前，必须先建立TCP连接。

![](http://prvyof0n9.bkt.clouddn.com/net2.png)

​	标志位说明：

| 字段    | 含义           |
| ------- | -------------- |
| URG     | 紧急指针       |
| **ACK** | 确认序号       |
| PSH     | 有DATA数据传输 |
| RST     | 连接重置       |
| **SYN** | 建立连接       |
| **FIN** | 关闭连接       |

### TCP握手机制

![](http://prvyof0n9.bkt.clouddn.com/net3.png)

### 用户数据报协议UDP

​	用户数据报协议UDP是Internet传输层协议。提供无连接、不可靠、数据报尽力传输服务。

![](http://prvyof0n9.bkt.clouddn.com/net4.png)

​	使用UDP是应该关注以下几点：

- 应用程序更容易控制发送什么数据以及何时发送。
- 无需建立连接。
- 无连接状态。
- 首部开销小。

## JAVA NIO

​	始于Java1.4，提供新的Java IO操作非阻塞API。用来替代JAVA IO和Java Networking相关的API。

​	NIO中有三个核心组件：

- Buffer缓冲区
- Channel通道
- Selector选择器

### Buffer缓冲区

​	缓冲区本质上是一个可以写入数据的内存块(类似数组)，然后可以再次读取。此内存

块包含在NIO Buffer对象中，该对象提供一组方法，可以更轻松地使用内存块。

​	相比直接操作数据对象，**Buffer API更加容易操作和管理**。

​	使用Buffer进行数据写入与读取，需要进行如下四个步骤：

1.将数据写入缓冲区。

2.调用buffer.flip()，转换为读取模式。

3.缓冲区读取数据。

4.调用buffer.clear()或buffer.compact()清除缓冲区。

### Buffer工作原理

​	Buffer的三个重要属性：

- `capacity容量`：作为一个内存块，Buffer具有固定的大小，也称为容量。
- `position位置`：写入模式时代表写数据的位置。读取模式时代表读取数据的位置。
- `limit限制`：写入模式，限制等于buffer的容量。读取模式下，limit等于写入的数据量。

![](http://prvyof0n9.bkt.clouddn.com/net5.png)

### ByteBuffer内存类型

​	ByteBuffer为性能关键型代码提供了直接内存(direct堆外)和非直接内存(heap堆内)两种实现。

​	堆外内存获取的方式是：

```java
ByteBuffer directByteBuffer = ByteBuffer.allocateDirect(noBytes);
```

​	好处：

- 进行网络IO或者文件IO时比heapBuffer少一次拷贝。(file/socket --- OS memory --- jvm heap)GC会移动对象内存，在写file或socket过程中，JVM的实现中，**会先把数据复制到堆外**，再进行写入。
- GC范围之外，降低GC压力，但实现了自动管理。DirectByteBuffer中有一个Cleaner对象(虚引用：PhantomReference)，Cleaner被GC前会执行clean方法，触发DirectByteBuffer中定义的Deallocator()方法。

​	为什么数据会移动到堆外再复制？

因为在复制过程中，Java的GC可能会触发，而GC中的复制-清除算法会改变对象在内存中的地址导致数据不正确，所以先移动到堆外防止发生这种错误。

​	建议：

- 性能确实可观的时候才去使用；分配给大型、长寿命；(网络传输、文件读写场景)
- 通过虚拟机参数MaxDirectMemorySize限制大小，防止耗尽整个机器的内存。

### Channel通道

​	BIO应用和NIO应用的区别：

![](http://prvyof0n9.bkt.clouddn.com/net6.png)

### SocketChannel

​	SocketChannel用于建立TCP网络连接，类似java.net.Socket。有两种创建形势：

1.客户端主动发起和服务器的连接。

2.服务端获取的新连接。

```java
//客户端主动发起连接的方式
SocketChannel socketChannel = SocketChannel.open();
socketChannel.configureBlocking(false);//设置为非阻塞模式
socketChannel.connect(new InetSocketAddress("http://www.baidu.com", 80));

channel.write(byteBuffer);//发送请求数据-向通道写入数据

int bytesRead = socketChannel.read(byteBuffer);//读取服务器返回-读取缓冲区的数据

socketChannel.close();//关闭连接
```

- `write写`：write()在尚未写入任何内容时就可能返回了。需要在循环中调用write()。
- `read读`：read()方法可能直接返回而根本不读取任何数据，根据返回的int值判断读取了多少字节。

### ServerSocketChannel

​	ServerSocketChannel可以监听新建的TCP连接通道，类似ServerSocket。

```java
//创建网络服务端
ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
serverSocketChannel.configureBlocking(false);//设置为非阻塞模式
serverSocketChannel.socket().bing(new InetSocketAddress(8080));//绑定端口
while(true){
    SocketChannel socketChannel = serverSocketChannel.accept();//获取新的TCP通道
    if(socketChannel != null){
        //TCP请求 读取/响应
    }
}
```

​	`serverSocketChannel.accept()`：如果该通道处于非阻塞模式，那么如果没有挂起的连接，该方法将立即返回null。必须检查返回的SocketChannel是否为null。

### Selector选择器

​	Selector是一个Java NIO组件，可以检查一个或多个NIO通道，并确定哪些通道已经准备好进行读取或写入。**实现单个线程可以管理多个通道，从而管理多个网络连接**。

​	一个线程使用Selector监听多个channel的不同事件：

​	四个事件分别对应SelectionKey四个常量。

1. Connect连接(SelectionKey.OP_CONNECT)
2. Accept准备就绪(OP_ACCEPT)
3. Read读取(OP_READ)
4. Write写入(OP_WRITE)

#### 事件驱动机制

​	实现一个线程处理多个通道的核心概念理解：事件驱动机制。

​	非阻塞的网络通道下，开发者通过Selector注册对通道感兴趣的事件类型，线程通过监听事件来触发相应的代码执行。(更底层是操作系统的多路复用机制)

```java
Selector selector = Selector.open();
        socketChannel.configureBlocking(false);
        //注册感兴趣的事件
        SelectionKey key = socketChannel.register(selector, SelectionKey.OP_READ);
        while (true){
            int readyChannels =selector.select();
            if (readyChannels == 0){
                continue;
            }
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> keyIterator = selectionKeys.iterator();
            while (keyIterator.hasNext()){
                SelectionKey key = keyIterator.next();
                //判断不同的事件类型，执行对应的处理逻辑
                //key.isAcceptable(); key.isConnectable(); key.isReadable(); key.isWriteable()
                keyIterator.remove();
            }
        }
```

## NIO对比BIO

![net7](E:\博客图片\网络编程\net7.png)

## NIO与多线程结合的改进方案

​	Doug Lea的著名文章《Scalable IO in Java》

![net8](E:\博客图片\网络编程\net8.png)

## Netty简介

​	Netty是一个高性能、高可扩展性的异步事件驱动的**网络应用程序框架**，它极大地简化了TCP和UDP客户端和服务器开发等网络编程。

​	Netty重要的四个内容：

1. Reactor线程模型：一种高性能的多线程程序设计思路
2. Netty中自己定义的Channel概念：增强版的通道概念
3. ChannelPipeline职责链设计模式：事件处理机制
4. 内存管理：增强的ByteBuffer缓冲区

![](E:\博客图片\网络编程\netty9.png)

​	图片来自官网，可以看出包含三大块：

1. 支持Socket等多种传输模式
2. 提供多种协议编解码实现
3. 核心设计包含事件处理模型、API的使用、ByteBuffer的增强

## Netty线程模型

​	为了让NIO处理更好的利用多线程特性，Netty实现了Reactor模型。

​	Reactor模型中有四个核心概念：

1. Resources资源(请求/任务)
2. Synchronous Event Demultiplexer同步事件复用器
3. Dispatcher分配器
4. Request Handler请求处理器

![](E:\博客图片\网络编程\netty10.png)

## EventLoopGroup初始化过程

![](E:\博客图片\网络编程\netty11.png)

![](E:\博客图片\网络编程\netty12.png)

​	**两组EventLoopGroup(Main&Sub)处理不同通道的不同事件**。

### EventLoop的启动

​	EventLoop自身实现了Executor接口，当调用executor方法提交任务时，则判断是否启动，未启动则调用内置的executor创建新线程来触发run方法执行。

![](E:\博客图片\网络编程\netty13.png)

### Bind绑定端口过程

![](E:\博客图片\网络编程\netty14.png)

### Channel概念

​	netty中的Channel是一个抽象的概念，可以理解为对JDK NIO Channel的增强和拓展。增加了很多属性和方法，完整信息可以看代码注释，下面罗列几个常见的属性和方法。

- AbstractChannel
  - pipeline DefaultChannelPipeline	//通道内事件处理链路
  - eventLoop EventLoop                           //绑定的EventLoop，用于执行操作
  - unsafe Unsafe                                        //提供I/O相关操作的封装
  - config() ChannelConfig                          //返回通道配置信息
  - read() Channel                                         //开始读数据，触发读取链路调用
  - write(Object msg) ChannelFuture        //写数据，触发链路调用
  - bind(SocketAddress SocketAddress) ChannelFuture //绑定

## Neety职责链和Pipeline详解

### 设计模式 - 责任链模式

​	责任链模式：为请求创建了一个处理对象的链。

​	发起请求和具体处理请求的过程进行解耦：职责链上的处理者负责处理请求，客户只需要将请求发送到职责链上即可，无需关心请求的处理细节和请求的传递。

![](E:\博客图片\网络编程\netty15.png)

### 实现责任链模式

​	**实现责任链模式4要素：**

> 1. 处理器抽象类
> 2. 具体的处理器实现类
> 3. 保存处理器信息
> 4. 处理执行

​	**伪代码实现：**也可以通过链表实现；见：PipelineDemo.java

```
//集合形式存储类似Tomcat中的Filters
//处理器抽象类
class AbstractHandler{void doHandler(Object obj)}

//处理器具体实现类
class Handler1 extends AbstractHandler{assert continue;}
class Handler2 extends AbstractHandler{assert continue;}
class Handler3 extends AbstractHandler{assert continue;}

//创建集合并存储所有处理器实例信息
List handlers = new List();
handlers.add(handler1, handler2, handler3);

//处理请求，调用处理器
void process(request){
    for(handler in handlers){
        handler.doHandler(request);
    }
}

//发起请求调用，通过责任链处理请求
call.process(request);
```

### Netty中的ChannelPipeline责任链

![](http://img.shaking.top/netty16.png)

​	**Pipeline管道**保存了通道所有处理器信息。

​	创建新的channel时自动创建一个专有的pipeline。

​	入站**事件**和出站**操作**会调用pipeline上的处理器。 

### 入站事件和出站事件

- 入站事件：通常指I/O线程生成了入站数据。

  (通俗理解：从socket底层自己往上冒上来的事件都是入站事件)

  比如EventLoop收到selector的OP_READ事件，**入站处理器**调用socketChannel.read(ByteBuffer)接收到数据后，这将导致通道的ChannelPipeline中包含的下一个中的channelRead方法被调用。

- 出站事件：通常是指I/O线程执行实际的输出操作。

  (通俗理解：想主动往socket底层操作的事件都是出站)

  比如bind方法用意是请求server socket绑定到给定的Socket Address，这将导致通道的ChannelPipeline中包含的下一个**出站处理器**中的bind方法被调用。

<center><b>inbound入站事件</b></center>

|             事件              |         描述          |
| :---------------------------: | :-------------------: |
|     fireChannelRegistered     |    channel注册事件    |
|    fireChannelUnregistered    |  channel解除注册事件  |
|       fireChannelActive       |    channel活跃事件    |
|      fireExceptionCaught      |       异常事件        |
|    fireUserEventTriggered     |    用户自定义事件     |
|        fireChannelRead        |     channel读事件     |
|    fireChannelReadComplete    |   channel读完成事件   |
| fireChannelWritabilityChanged | channel写状态变化事件 |

<center><b>outbound出站事件</b></center>

|     事件      |               描述                |
| :-----------: | :-------------------------------: |
|     bind      |           端口绑定事件            |
|    connect    |             连接事件              |
|  disconnect   |           断开连接事件            |
|     close     |             关闭事件              |
|  deregister   |           解除注册事件            |
|     flush     |        刷新数据到网络事件         |
|     read      | 读事件，用于注册OP_READ到selector |
|     write     |              写事件               |
| writeAndFlush |           写出数据事件            |

### Pipeline中的handler是什么

- `ChannelHandler`：用于处理I/O事件或拦截I/O操作，并转发到ChannelPipeline中的下一个处理器。

  这个顶级接口定义功能很弱，实际使用时会去实现下面两个大的子接口：、

  处理入站I/O事件的**ChannelInboundHandler**、处理出站I/O操作的**ChannelOutboundHandler**

- `适配器类`：为了方便开发，避免所有handler去实现一遍接口方法，Netty提供了简单的实现类：

  **ChannelInboundHandlerAdapter**处理入站I/O事件

  **ChannelOutboundHandlerAdapter**来处理出站I/O事件

  **ChannelDuplexHandler**来支持同事处理入站和出站事件

- `ChannelHandlerContext`：实际存储在Pipeline中的对象并非ChannelHandler，而是上下文对象。

  将handler，包裹在上下文对象中，通过上下文对象与它所属的ChannelPipeline交互，向上或向下传递事件或者修改pipeline都是通过上下文对象。

### 维护Pipeline中的handler

> ChannelPipeline是线程安全的，ChannelHandler可以在任何时候添加或删除。
>
> 例如，你可以在即将交换敏感信息时掺入加密处理程序，并在交换后删除它。
>
> 一般操作，初始化的时候增加进去，较少删除。下面是Pipeline中管理handler的API

| 方法名称    | 描述                 |
| ----------- | -------------------- |
| addFirst    | 最前面插入           |
| addLast     | 最后面插入           |
| addBefore   | 插入到指定处理器前面 |
| addAfter    | 插入到指定处理器后面 |
| remove      | 移除指定处理器       |
| removeFirst | 移除第一个处理器     |
| removeLast  | 移除最后一个处理器   |
| replace     | 替换指定的处理器     |

```
//伪代码示例
ChannelPipeline p = ...;
p.addLast("1", new InboundHandlerA());
p.addLast("2", new InboundHandlerB());
p.addLast("3", new OutboundHandlerA());
p.addLast("4", new OutboundHandlerB());
p.addLast("5", new InboundOutboundHandlerX());
```

### handler的执行分析

![](http://img.shaking.top/netty17.png)

当入站事件发生时，执行顺序是1,2,3,4,5

当出站事件发生时，执行顺序是5,4,3,2,1

在这一原则之上，ChannelPipeline在执行时会进行选择

**3和4位出站处理器**，因此入站事件的实际执行是：1,2,5

**1和2为入站处理器**，因此出站事件的实际执行是：5,4,3



不同的入站事件会触发handler的不同方法‘:

上下文对象中的fire**方法，代表入站事件传播和处理，其余的方法代表出站事件的传播和处理。

### 分析registered入站事件的处理

![](http://img.shaking.top/netty18.png)

- 创建和初始化Channel

  通道创建时构建一个pipeline，头尾分别是HeadContext......TailContext

  init()增加了一个Channelintializer，这个handler用于初始化通道，我们自己的相关初始化定义都是通过它执行的

- 注册到EventLoop中的Selector上

  config().group().register() registered成功之后

  触发ChannelIntializer.channelRegistered，初始化handler执行一次之后，会把自己从pipeline中删除掉

<center>ServerSocketChannel.pipeline的变化</center>

![](http://img.shaking.top/netty19.png)

### 分析bind出站事件的处理

![](http://img.shaking.top/netty20.png)























