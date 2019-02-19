package com.study.tcpserver.nio;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Created by lf52 on 2019/2/19.
 */
public class FileReadNIO {

    public static void main(String[] args) {

        RandomAccessFile file = null;
        FileChannel channel = null;
        try {
            //��������Ϊ48�ֽڵĻ�����
            ByteBuffer buf = ByteBuffer.allocate(48);
            file = new RandomAccessFile("D://itemnumber.txt", "rw");
            channel = file.getChannel();
            int bytesRead = -1;
            bytesRead = channel.read(buf);

            while (bytesRead != -1){

                buf.flip();

                while (buf.hasRemaining()) {
                    System.out.print((char) buf.get());//һ�ζ�1���ֽ�
                }
                //clear()�������������������,compact()����ֻ������Ѿ�����������
                buf.clear();
                bytesRead = channel.read(buf);
            }


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(file != null){
                try {
                    file.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
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
