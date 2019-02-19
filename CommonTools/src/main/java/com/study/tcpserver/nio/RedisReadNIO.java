package com.study.tcpserver.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Created by lf52 on 2019/2/19.
 */
public class RedisReadNIO {

    public static void main(String[] args) {

        String command = "*2\r\n$6\r\nmemory\r\n$6\r\ndoctor\r\n";
        SocketChannel channel = null;

        try {
            //��һ��SocketChannel�����ӵ�ĳ̨������
            channel = SocketChannel.open();
            //ʹ�÷�����ģʽ
            channel.configureBlocking(false);
            channel.connect(new InetSocketAddress("10.16.50.223",6000));
            while(!channel.finishConnect() ){
                //wait
            }

            //����Buffer ����redis�ͻ�������д��channel��
            ByteBuffer buffer = ByteBuffer.wrap(command.getBytes("UTF-8"));
            channel.write(buffer);
            buffer.clear();

            //sleep 1s �ȴ�redis server��׼��������
            Thread.sleep(1000);

            int bytesRead = -1;
            //��channel�ж�ȡredis server�˷��صĽ��
            bytesRead = channel.read(buffer);
            while (bytesRead != -1) {

                if(bytesRead <= 0){
                    break;
                }
                //��Buffer��дģʽ�л�����ģʽ
                buffer.flip();
                while (buffer.hasRemaining()) {
                    System.out.print((char) buffer.get());//һ�ζ�1���ֽ�
                }
                //clear()�������������������,compact()����ֻ������Ѿ�����������
                buffer.clear();
                bytesRead = channel.read(buffer);

            }


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(channel.isOpen()){
                try {
                    channel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

}
