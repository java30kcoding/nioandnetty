package cn.itlou.nio;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * 基于非阻塞的写法
 */
public class NIOServer {

    public static void main(String[] args) throws Exception {

        //创建服务端
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        //设置非阻塞
        serverSocketChannel.configureBlocking(false);
        //绑定端口
        serverSocketChannel.socket().bind(new InetSocketAddress(8080));
        System.out.println("启动成功");
        while (true){
            //获取TCP连接
            SocketChannel socketChannel = serverSocketChannel.accept();
            if (socketChannel != null){
                System.out.println("收到新连接" + socketChannel.getRemoteAddress());
                socketChannel.configureBlocking(false);
                try {
                    ByteBuffer requestBuffer = ByteBuffer.allocate(1024);
                    //人工阻塞，需要改进
                    while (socketChannel.isOpen() && socketChannel.read(requestBuffer) != -1){
                        //长链接情况下，需要手动判断数据有没有读取结束(此处做一个简单的判断，超过0字节就认为请求结束了)
                        if (requestBuffer.position() > 0){
                            break;
                        }
                    }
                    //如果没有数据，不进行后续处理
                    if (requestBuffer.position() == 0){
                        continue;
                    }
                    requestBuffer.flip();
                    byte[] content = new byte[requestBuffer.limit()];
                    requestBuffer.get(content);
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
                }
            }
        }

    }

}
