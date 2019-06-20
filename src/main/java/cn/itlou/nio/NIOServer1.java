package cn.itlou.nio;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * 升级NIOServer
 * 用一个线程处理轮询所有请求
 * 问题: 轮询通道的方式,低效,浪费CPU
 */
public class NIOServer1 {

    /**
     * 已建立连接的集合
     */
    private static ArrayList<SocketChannel> channels = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        //创建网络连接服务端
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.socket().bind(new InetSocketAddress(8080));
        System.out.println("启动成功");
        while (true){
            SocketChannel socketChannel = serverSocketChannel.accept();
            //tcp读取/响应
            if (socketChannel != null){
                System.out.println("收到新连接：" + socketChannel.getRemoteAddress());
                socketChannel.configureBlocking(false);
                channels.add(socketChannel);
            }else {
                //没有新连接的情况下，就去处理现有连接的数据，处理完就删除掉
                Iterator<SocketChannel> iterator = channels.iterator();
                while (iterator.hasNext()){
                    SocketChannel ch = iterator.next();
                    try {
                        ByteBuffer reqBuffer = ByteBuffer.allocate(1024);
                        if (ch.read(reqBuffer) == 0){
                            //等于0，代表这个通道没有数据需要处理，那就待会在处理
                            continue;
                        }
                        while (ch.isOpen() && ch.read(reqBuffer) != -1){
                            if (reqBuffer.position() > 0){
                                break;
                            }
                        }
                        if (reqBuffer.position() == 0){
                            continue;
                        }
                        reqBuffer.flip();
                        byte[] content = new byte[reqBuffer.limit()];
                        reqBuffer.get(content);
                        System.out.println(new String(content));
                        System.out.println("收到数据来自：" + ch.getRemoteAddress());

                        //响应结果200
                        String response = "HTTP/1.1 200 OK\r\n" +
                                "Content-Length:11\r\n" +
                                "Hello World";
                        ByteBuffer buffer = ByteBuffer.wrap(response.getBytes());
                        while (buffer.hasRemaining()) {
                            ch.write(buffer);
                        }
                        iterator.remove();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }
    }

}
