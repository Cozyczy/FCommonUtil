package com.study.distributedlock.zookeeper;

import java.util.concurrent.TimeUnit;

/**
 * Created by lf52 on 2018/1/13.
 */
public interface DistributedLock {
    /**
     * ��ȡ�����������ռ�þ�һֱ�ȴ�
     * @throws Exception
     */
    public  void lock();

    /**
     * ���Ի�ȡ�������̷���
     * @return
     */
    public  boolean tryLock();

    /**
     * ���Ի�ȡ����ֱ����ʱ
     * @param time
     * @param unit
     * @return
     * @throws Exception
     */
    public  boolean tryLock(long time, TimeUnit unit);

    /**
     * �ͷ���
     * @throws Exception
     */
    public  void unlock();
}