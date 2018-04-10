package com.study.distributedlock.manager;


import com.study.constants.Constants;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPoolConfig;


/**
 * Created by xiaojun on 2018/1/20.
 */
public class RedisManager {


    //JedisCluster�ڲ�ʹ���˳ػ�������ÿ��ʹ����϶����Զ��ͷ�Jedis��˲���Ҫ�رա�
    private static JedisCluster jedis = null;

    public static JedisCluster getConnection()  {
        if(jedis == null){
            jedis = createConnection();
        }
        return jedis;
    }

    private static JedisCluster createConnection()  {
        JedisPoolConfig config = new JedisPoolConfig();
        // �������������
        config.setMaxTotal(Constants.maxTotal);
        // ������������
        config.setMaxIdle(Constants.maxIdle);
        // �������ȴ�ʱ��
        config.setMaxWaitMillis(Constants.maxWaitMillis);
        // ��borrowһ��jedisʵ��ʱ���Ƿ���Ҫ��֤����Ϊtrue��������jedisʵ�����ǿ��õ�
        config.setTestOnBorrow(true);

        HostAndPort host = new HostAndPort(Constants.redis_host,Constants.redis_port);

        return new JedisCluster(host,Constants.redisTimeOut,config);

    }



}
