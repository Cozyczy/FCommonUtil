package com.study.distributedlock.redis.impl;

import com.study.distributedlock.manager.RedisManager;
import com.study.distributedlock.redis.DistributedLock;
import org.apache.log4j.Logger;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.exceptions.JedisException;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by xiaojun on 2018/1/20.
 */
public class RedisDistributedLock implements DistributedLock {

    private static final Logger logger = Logger.getLogger(RedisDistributedLock.class);

    /**
     * ����
     * @param lockName  ����key
     * @param acquireTimeout  ��ȡ���ĳ�ʱʱ��
     * @param ttlTime   ���ĳ�ʱʱ��(redis key ttl time)
     * @return
     */
    public String lock(String lockName, long acquireTimeout, long ttlTime) {
        JedisCluster conn = null;
        String retIdentifier = null;
        try {
            conn = RedisManager.getConnection();
            String identifier = UUID.randomUUID().toString();
            String lockKey = "DLock-" + lockName;

            int lockExpire = (int)(ttlTime / 1000);

            long end = System.currentTimeMillis() + acquireTimeout;
            //while true ѭ�������û�л������Դ�ͻ�һֱ����ֱ����ȡ���Ĳ�����ʱ����ȡ���ĳ�ʱʱ�䣬�������ʱ���������ȡ����
            while (System.currentTimeMillis() < end) {

                if (conn.setnx(lockKey, identifier) == 1) {
                    //�ɹ���ȡ������Դ�����ر�ʶ
                    conn.expire(lockKey, lockExpire);
                    retIdentifier = identifier;
                    return retIdentifier;
                }

                // ����-1����keyû�����ó�ʱʱ�䣬Ϊkey����һ����ʱʱ��
                if (conn.ttl(lockKey) == -1) {
                    conn.expire(lockKey, lockExpire);
                }

                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

        } catch (JedisException e) {
            logger.error("get lock error", e);
        }
        return retIdentifier;
    }


    /**
     * �ͷ�����ɾ��key������watch��ʧ�ܻ�ع���
     * @param lockName ����key
     * @param identifier  �ͷ����ı�ʶ
     * @return
     */
    public boolean unlock(String lockName, String identifier) {

        JedisCluster conn = null;
        String lockKey = "DLock-" + lockName;
        boolean retFlag = false;
        try {
            conn = RedisManager.getConnection();
            while (true) {

                // ͨ��valueֵ���ж��ǲ��Ǹ��������Ǹ�������ɾ�����ͷ���
                if (identifier.equals(conn.get(lockKey))) {
                    if(conn.del(lockKey) == 1){
                        retFlag = true;
                        break;
                    }

                }

            }
        } catch (JedisException e) {
            logger.error("release lock error", e);
        }
        return retFlag;
    }
}
