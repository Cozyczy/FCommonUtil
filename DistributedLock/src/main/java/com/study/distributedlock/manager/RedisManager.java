package com.study.distributedlock.manager;


import org.apache.hadoop.conf.Configuration;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPoolConfig;


/**
 * Created by xiaojun on 2018/1/20.
 */
public class RedisManager {

    private static Configuration conf = new Configuration();

    //JedisCluster�ڲ�ʹ���˳ػ�������ÿ��ʹ����϶����Զ��ͷ�Jedis��˲���Ҫ�رա�
    private static JedisCluster jedis = null;

    public static JedisCluster getConnection()  {
        if(jedis == null){
            jedis = createConnection();
        }
        return jedis;
    }

    private static JedisCluster createConnection()  {
        conf.addResource("jedis.xml");
        JedisPoolConfig config = new JedisPoolConfig();
        // �������������
        config.setMaxTotal(conf.getInt("maxTotal", 100));
        // ������������
        config.setMaxIdle(conf.getInt("maxIdle", 8));
        // �������ȴ�ʱ��
        config.setMaxWaitMillis(conf.getInt("maxWaitMillis",1000 * 100));
        // ��borrowһ��jedisʵ��ʱ���Ƿ���Ҫ��֤����Ϊtrue��������jedisʵ�����ǿ��õ�
        config.setTestOnBorrow(true);

        HostAndPort host = new HostAndPort(conf.get("host"),conf.getInt("port", 6379));

        return new JedisCluster(host,conf.getInt("timeOut",3000),config);

    }



}
