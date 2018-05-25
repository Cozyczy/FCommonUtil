package com.study.lrucache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by lf52 on 2018/1/27.
 *
 * VM Option
 * -Xms5M -Xmx5M  -XX:-UseGCOverheadLimit
 *
 * ���Խ����20���߳�ͬʱ��������
 *   1.���ڴ���������£�put��������
 *     LRUCache size: 10000, LRUCache costtime: 50       cacheBuilder size: 10000, cacheBuilder costtime: 47
 *     LRUCache size: 100000, LRUCache costtime: 206     cacheBuilder size: 100000, cacheBuilder costtime: 187
 *     LRUCache size: 200000, LRUCache costtime: 381     cacheBuilder size: 200000, cacheBuilder costtime: 295
 *     LRUCache size: 500000, LRUCache costtime: 1203    cacheBuilder size: 500000, cacheBuilder costtime: 1115
 *     LRUCache size: 1000000, LRUCache costtime: 2484   cacheBuilder size: 1000000, cacheBuilder costtime: 2251
 *     ���ߵ����������ԡ�
 *
 *   2.ģ���ڴ���ܻᱬ�ĳ�����put��������-Xms5M -Xmx5M  -XX:-UseGCOverheadLimit size = 15000
 *     cacheBuilder size: 5897, cacheBuilder costtime: 810��weakKeys��
 *     cacheBuilder size: 14928, cacheBuilder costtime: 50194
  *    LRUCache size: 5421, LRUCache costtime: 5912��softKeys��
 *
 *     �Ӳ��Խ��������google guava�ڿ���weakKeys�����������Զ����LRUCache������softKeys�������Ҷ���cache�Ķ�ʧ�ʻ�����ƽ��
 *     google guava�ڲ�����weakKeys���������Ȼcache��ʧ�ʺܵͣ��������ܻ�ܲƵ��gc���£������Һ�����gc�������������ڴ�ᱬ����
 *     ����google guava�ڿ���weakKeys�ĳ����£���ʹ�ڴ���㣬�������������߳�ɨ��������Ͻ���ڴ�����Ĺ����з��������õĶ���Ҳ�����
 *     ����ܻᵼ�����������cache��ͻȻ��������ʧ�������
 *
 *   3.LRUCache������������׼ȷ�ģ�foreach������google guava��಻��size = 10000��
 *     LRUCache size: 10000, LRUCache costtime: 1241
 *    cacheBuilder size: 10000, cacheBuilder costtime: 1277
 *
 */
public class LRUCacheTest {

    static int size = 30000;
    static ExecutorService pool =  new ThreadPoolExecutor(50, 100, 60, TimeUnit.SECONDS,
                                       new ArrayBlockingQueue<>(50 * 4, true),
                                       new ThreadFactoryBuilder().setNameFormat("LRU TEST POOL").build(),
                                       new ThreadPoolExecutor.AbortPolicy());


    public static void main(String[] args) throws InterruptedException {

        LRUCache lruCache = new LRUCache(size,30000, TimeUnit.MILLISECONDS);

        //Cache<String,String> cache = CacheBuilder.newBuilder().weakKeys().maximumSize(size)
        Cache<String,String> cache = CacheBuilder.newBuilder().weakKeys().maximumSize(size)
                .expireAfterAccess(7, TimeUnit.DAYS)
                .removalListener((RemovalListener<String, String>) removalNotification -> {
                    System.out.println("buildCache will remove : "+removalNotification.getKey());
                }).build();

        /*long start = System.currentTimeMillis();
        List<Future<Boolean>> lrulist = new ArrayList(size);
        for(int i=0;i<size;i++){
            lrulist.add(pool.submit(new putTask(lruCache, "aaaaaaa" + i, "aaaaaa" + i)));
            Thread.sleep(0, 100);
        }
        lrulist.forEach(item -> {
            try {
                item.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        long end = System.currentTimeMillis();
        System.out.println("LRUCache size: " + lruCache.getCache().size() + ", LRUCache costtime: " + (end - start));*/

        long start1 = System.currentTimeMillis();
        List<Future<Boolean>> cachelist = new ArrayList(size);
        for(int i=0;i<size;i++){
            cachelist.add(pool.submit(new putTask1(cache, new String("aaaaaaa" + i), new String("aaaaaa") + i)));
            Thread.sleep(0,50);
        }
        cachelist.forEach(item -> {
            try {
                item.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        long end1 = System.currentTimeMillis();

        System.out.println("cacheBuilder size: " + cache.size() + ", cacheBuilder costtime: " + (end1 - start1));

    }


    static class putTask implements Callable<Boolean>{
        private LRUCache lruCache;
        private String key;
        private String value;

        public putTask(LRUCache lruCache,String key,String value){
            this.key = key;
            this.value = value;
            this.lruCache = lruCache;
        }
        @Override
        public Boolean call() throws Exception {
            lruCache.put(key,value);
            System.out.println("add--------------");
            return true;
        }
    }

    static class putTask1 implements Callable<Boolean>{
        private Cache cache;
        private String key;
        private String value;

        public putTask1(Cache cache,String key,String value){
            this.key = key;
            this.value = value;
            this.cache = cache;
        }
        @Override
        public Boolean call() throws Exception {
            cache.put(key,value);
            System.out.println("add--------------");
            return true;
        }
    }

    /**
     * test LRUCache , cacheBuilder foreach����
     */
    @Test
    public void testForeach() throws InterruptedException {

        LRUCache lruCache = new LRUCache(20000,30000, TimeUnit.MILLISECONDS);

        Cache<String,String> cache = CacheBuilder.newBuilder().maximumSize(10000000)
                .expireAfterAccess(7, TimeUnit.DAYS)
                .removalListener((RemovalListener<String, String>) removalNotification -> {
                    System.out.println("cache will remove : "+removalNotification.getKey());
                }).build();


        for(int i=0;i<20000;i++){
            lruCache.put(new String("aaaaaaa" + i), new String("aaaaaa") + i);
            cache.put(new String("aaaaaaa" + i), new String("aaaaaa") + i);
        }

        long start = System.currentTimeMillis();
        lruCache.getCache().forEach((k, v) ->
                        System.out.println("lru key is : " + k + " ,lru value is : " + v)
        );
        long end = System.currentTimeMillis();


        long start1 = System.currentTimeMillis();
        cache.asMap().forEach((k, v) ->
                        System.out.println("cache key is : " + k + " , cache value is : " + v)
        );
        long end1 = System.currentTimeMillis();

        System.out.println("LRUCache size: " + lruCache.getCache().size() + ", LRUCache costtime: " + (end - start));
        System.out.println("cacheBuilder size: " + cache.size() + ", cacheBuilder costtime: " + (end1 - start1) + " ,value");


    }

    /**
     * test LRUCache����׼ȷ��
     */
    @Test
    public void test() throws InterruptedException {

        LRUCache lruCache = new LRUCache(10000,30000, TimeUnit.MILLISECONDS);

        for(int i=0;i<10000;i++){
            lruCache.put(new String("aaaaaaa" + i), new String("aaaaaa") + i);
        }

        long start = System.currentTimeMillis();
        lruCache.getCache().forEach((k, v) ->
                        System.out.println("lru key is : " + k + " ,lru value is : " + v)
        );
        long end = System.currentTimeMillis();

        System.out.println(lruCache.getCache().size());
        System.out.println("LRUCache size: " + lruCache.getCache().size() + ", LRUCache costtime: " + (end - start));


    }

    @Test
    public void test1() throws InterruptedException, ExecutionException {
        Cache<String,String> cache = CacheBuilder.newBuilder().weakKeys().weakValues().maximumSize(size)
                .expireAfterAccess(7, TimeUnit.DAYS)
                .removalListener((RemovalListener<String, String>) removalNotification -> {
                    System.out.println("buildCache will remove : " + removalNotification.getKey());
                }).build();
        cache.put(new String("aa1"), "aa1");
        String v = cache.get(new String("aa1"), new Callable<String>() {
            @Override
            public String call() throws Exception {
                return null;
            }
        });
        System.out.println(v);
    }

}
