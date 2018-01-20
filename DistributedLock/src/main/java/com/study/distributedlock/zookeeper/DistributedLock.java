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
    public  void lock()  throws Exception;

    /**
     *��ȡ����ֱ����ʱ
     * @param time
     * @param unit
     * @return
     * @throws Exception
     */
    public  boolean lock(long time, TimeUnit unit)  throws Exception;

    /**
     * �ͷ���
     * @throws Exception
     */
    public  void unlock()  throws Exception;
}