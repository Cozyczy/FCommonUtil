package com.study.distributedlock.redis;

/**
 * Created by xiaojun on 2018/1/20.
 */
public interface DistributedLock {

    /**
     * ����
     * @param lockName  ����key
     * @param acquireTimeout  ��ȡ��ʱʱ��
     * @param timeout   ���ĳ�ʱʱ��
     * @return ����ʶ
     */
    public  String lock(String lockName, long acquireTimeout, long timeout);

    /**
     * �ͷ���
     * @param lockName ����key
     * @param identifier    �ͷ����ı�ʶ
     * @throws Exception
     */
    public boolean unlock(String lockName, String identifier);
}