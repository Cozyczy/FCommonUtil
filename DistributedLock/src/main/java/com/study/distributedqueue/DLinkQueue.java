package com.study.distributedqueue;

import org.apache.zookeeper.KeeperException;

/**
 * Created by lf52 on 2018/1/25.
 *
 * ����zkʵ��һ���򵥵ķֲ�ʽ�������У���������ɱ�ȳ�����
 */
public interface DLinkQueue<E> {

     boolean offer(E e);

     E poll();

     boolean contains(E e);

     int size() throws KeeperException, InterruptedException;

}
