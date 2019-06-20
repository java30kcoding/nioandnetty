package cn.itlou.nio;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

public class NIOClient {

    public static void main(String[] args) throws Exception {
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        socketChannel.connect(new InetSocketAddress("127.0.0.1", 8080));
        while (!socketChannel.finishConnect()){
            //没连接上则一直等待
            Thread.yield();
        }
        Scanner scanner = new Scanner(System.in);
        System.out.println("请输入：");
        //发送内容
        String msg = scanner.nextLine();
        ByteBuffer byteBuffer = ByteBuffer.wrap(msg.getBytes());
        while (byteBuffer.hasRemaining()){
            socketChannel.write(byteBuffer);
        }
        //读取响应
        System.out.println("收到服务端响应：");
        ByteBuffer reqBuffer = ByteBuffer.allocate(1024);

        while (socketChannel.isOpen() && socketChannel.read(reqBuffer) != -1){
            //长连接下，需要手动判断数据有没有读取结束，此处为简单判断
            if (reqBuffer.position() > 0){
                break;
            }
        }

        reqBuffer.flip();
        byte[] content = new byte[reqBuffer.limit()];
        reqBuffer.get(content);
        System.out.println(new String(content));
        scanner.close();
        socketChannel.close();

    }

}
