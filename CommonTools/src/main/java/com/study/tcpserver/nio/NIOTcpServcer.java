package com.study.tcpserver.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by lf52 on 2018/2/12.
 */
public class NIOTcpServcer {


    public static void main(String[] args) {
        try {
            StartServer(8889);
        } catch (IOException e) {
            log(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * start server
     * @param port
     * @throws IOException
     */
    public static void StartServer(int port) throws IOException {
        //����ServerSocketChannel ����������ָ���Ķ˿�
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        //���÷�����ģʽ
        serverSocketChannel.configureBlocking(false);

        //�������ͨ�������ķ������׽���
        ServerSocket serverSocket = serverSocketChannel.socket();
        //���з���İ�
        serverSocket.bind(new InetSocketAddress(port));

        //����Selector
        Selector selector = Selector.open();

        //ע��selector
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        log("Starting Server , Port is : " + port);

        // 7.����Server����
        while (true) {

            //���ض��¼��Ѿ���������Щͨ���� �������ж�
            if (selector.select(1000 * 4) == 0) {
                log("Server Started , Waiting ...... ");
                continue;
            }

            // һ��������select()���������ҷ���ֵ������һ��������ͨ��������Ȼ�����ͨ������selector��selectedKeys()���������ʡ���ѡ�������selected keyset�����еľ���ͨ����

            // --- ���ش�ѡ��������ѡ�������
            Set<SelectionKey> selectionKeySet = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeySet.iterator();

            SocketChannel client =  null;

            // 7.3 ������ȡ
            while (iterator.hasNext()) {

                SelectionKey myKey = iterator.next();


                //�˼���ͨ���Ƿ���׼���ý����µ��׽������ӡ�
                if (myKey.isAcceptable()) {
                    // ���ܵ���ͨ���׽��ֵ����ӡ�
                    // �˷������ص��׽���ͨ��������У�����������ģʽ��
                    client = serverSocketChannel.accept();
                    //����Ϊ������
                    client.configureBlocking(false);
                    Socket socket = client.socket();
                    SocketAddress remoteAddr = socket.getRemoteSocketAddress();
                    log("Connected to: " + remoteAddr + "  \t Connection Accepted:   \n");

                    // 7.4.3ע�ᵽselector���ȴ�����
                    client.register(selector, SelectionKey.OP_READ);


                }
                if (myKey.isReadable()) {

                    //����Ϊ֮�����˼���ͨ����
                    client = (SocketChannel) myKey.channel();
                    ByteBuffer byteBuffer = ByteBuffer.allocate(1024 * 3);
                    //������������Ա��´ζ�ȡ
                    byteBuffer.clear();

                    // ��ȡ�����������������ݵ���������
                    int readCount = -1;
                    readCount = client.read(byteBuffer);

                    // ���û�����ݣ��رյ�ǰsocket channel��������
                    if (readCount == -1) {
                        Socket socket = client.socket();
                        SocketAddress remoteAddr = socket.getRemoteSocketAddress();
                        log("Connection closed by client: " + remoteAddr);
                        client.close();
                        myKey.cancel();
                    }else{
                        // Todo ���������ݵĴ����߼�
                        String receiveText = new String( byteBuffer.array(),0,readCount);
                        log("Server Get Data : "+receiveText);
                    }


                }

                // �Ƴ�
                iterator.remove();
            }
        }
    }


    private static void log(String str) {
        System.out.println(str);
    }
}



