package com.study.tcpserver.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;

/**
 * Created by lf52 on 2019/2/19.
 */
public class NIOClient {

    public static void main(String[] args) throws IOException, InterruptedException {

        InetSocketAddress crunchifyAddr = new InetSocketAddress("localhost", 8889);
        SocketChannel crunchifyClient = SocketChannel.open(crunchifyAddr);

        log("Connecting to Server on port 8889...");

        // 2.���巢�͵�����
        ArrayList<String> companyDetails = new ArrayList<String>();
        companyDetails.add("Lenovo");
        companyDetails.add("Samsung");
        companyDetails.add("Huawei");
        companyDetails.add("Facebook");
        companyDetails.add("Twitter");
        companyDetails.add("IBM");
        companyDetails.add("Google");

        // 3.ѭ������
        for (String companyName : companyDetails) {

            // 3.1 ���ַ�תת��Ϊ�ֽ�
            byte[] message = new String(companyName).getBytes("UTF-8");

            // 3.2 ����Buffer �����ֽ��������ݷ�װ��Buffer��
            ByteBuffer buffer = ByteBuffer.wrap(message);

            // 3.3 Channel��ȡBuffer�е�����
            crunchifyClient.write(buffer);

            log("sending: " + companyName);
            // ���Buffer
            buffer.clear();

            // �ȴ�2000����
            Thread.sleep(500);
        }
        // �ر�Channel
        crunchifyClient.close();
    }

    private static void log(String str) {
        System.out.println(str);
    }


}
