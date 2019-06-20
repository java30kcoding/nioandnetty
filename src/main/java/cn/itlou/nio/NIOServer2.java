package cn.itlou.nio;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * 通过Selector来实现升级Server
 * 一个线程处理所有请求，一个selector监听所有事件
 * 会成为瓶颈，所以需要用到多线程
 */
public class NIOServer2 {

    public static void main(String[] args) throws Exception {
        //1.创建网络服务端，ServerSocketChannel
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);

        //2.构建一个Selector选择器，并将channel注册上去
        Selector selector = Selector.open();
        //将serverSocketChannel注册到Selector上
        SelectionKey selectionKey = serverSocketChannel.register(selector, 0, serverSocketChannel);
        //对serverSocketChannel上的Accept感兴趣
        selectionKey.interestOps(SelectionKey.OP_ACCEPT);

        //3.绑定端口
        serverSocketChannel.socket().bind(new InetSocketAddress(8080));

        System.out.println("启动成功");

        while (true){
            //不再轮询通道，改用下面的轮询方式，select方法有阻塞效果，直到有事件通知才有返回
            selector.select();
            //获取事件
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            //遍历查询结果
            Iterator<SelectionKey> keyIterator = selectionKeys.iterator();

            while (keyIterator.hasNext()){
                //被封装的查询结果
                SelectionKey key = keyIterator.next();
                keyIterator.remove();
                //只关注Read和Accept
                if (key.isAcceptable()){
                    ServerSocketChannel server = (ServerSocketChannel) key.attachment();
                    //将拿到的客户端连接通道，注册到selector上
                    SocketChannel clientSocketChannel = server.accept();
                    clientSocketChannel.configureBlocking(false);
                    clientSocketChannel.register(selector, SelectionKey.OP_READ, clientSocketChannel);
                    System.out.println("收到新连接：" + clientSocketChannel.getRemoteAddress());
                }
                if (key.isReadable()){
                    SocketChannel socketChannel = (SocketChannel) key.attachment();
                    try {
                        ByteBuffer reqBuffer = ByteBuffer.allocate(1024);
                        while (socketChannel.isOpen() && socketChannel.read(reqBuffer) != -1){
                            if (reqBuffer.position() > 0){
                                break;
                            }
                        }
                        //如果没有数据了，则不继续后面的处理
                        if (reqBuffer.position() == 0){
                            continue;
                        }
                        reqBuffer.flip();
                        byte[] content = new byte[reqBuffer.limit()];
                        reqBuffer.get(content);
                        System.out.println(new String(content));

                        System.out.println("收到数据来自：" + socketChannel.getRemoteAddress());

                        //响应结果200
                        String response = "HTTP/1.1 200 OK\r\n" +
                                "Content-Length:11\r\n" +
                                "Hello World";
                        ByteBuffer buffer = ByteBuffer.wrap(response.getBytes());
                        while (buffer.hasRemaining()){
                            socketChannel.write(buffer);
                        }

                    }catch (Exception e){
                        e.printStackTrace();
                        key.cancel();
                    }
                }

            }

            selector.selectNow();

        }

    }

}
