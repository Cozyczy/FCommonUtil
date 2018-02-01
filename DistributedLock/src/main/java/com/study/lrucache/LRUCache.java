package com.study.lrucache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * -Xms5M -Xmx5M -XX:-UseGCOverheadLimit
 */
public class LRUCache<K,V> {

    private static final Logger LOGGER = LoggerFactory.getLogger(LRUCache.class);

    //���ݴ洢���ʣ��̰߳�ȫ
    private ConcurrentHashMap<K,V> cache ;
    //��֤LRU
    private LRUList lruList;
    //cache�����ݵ�ttl����ʱ��
    private int ttl;
    //ttl�ĵ�λ
    private TimeUnit timeUnit;
    //SoftReference�����õ����ö���
    private ReferenceQueue referenceQueue;

    public LRUCache(int maxCapacity){
        this(maxCapacity,7,TimeUnit.DAYS);
    }

    public LRUCache(int maxCapacity, int ttl, TimeUnit timeUnit){
        cache = new ConcurrentHashMap<>();
        lruList = new LRUList(maxCapacity);
        referenceQueue = new ReferenceQueue();
        thread.setDaemon(true);
        thread.start();
        this.ttl = ttl;
        this.timeUnit = timeUnit;
    }

    class KeyWord<K> extends SoftReference<K> {
        private K value;
        public KeyWord(K referent, ReferenceQueue<? super K> q,K value) {
            super(referent, q);
            this.value = value;
        }
        public K getValue() {
            return value;
        }

        /**
         * 1.finalize()��Object��protected������������Ը��Ǹ÷�����ʵ����Դ��������GC�ڻ��ն���֮ǰ���ø÷�����
         * 2.finalize������ʲôʱ�򱻵���?
         *  ���������յ�ʱ��ĳ������Ҫ�����յ�ʱ�򣬻��Ƚ���һ�α�ǣ����ҽ��ö����finalize�ŵ�һ�������ȼ����߳���ȥִ��,�ȵ���һ���������յ�ʱ���ٰ����������ա�
         *  jvm������֤����������֮ǰ�ܹ�ִ������finalize������������ִ��finalize�������̷߳�������ѭ������������finalize�������޷�ִ���ˡ�
         **/
        @Override
        protected void finalize() throws Throwable {
            try{
                lruList.remove(value);
                cache.remove(value);
            }finally {
                super.finalize();
                LOGGER.info(value +" finalize called ");
            }
        }
    }

    public void put(K key,V value){
        if(cache.containsKey(key)){
            //cache���Ѿ����ڵ�ǰ��key,ֻ��������ttlʱ��
            synchronized (lruList){
                lruList.put(key,System.currentTimeMillis());
                cache.put(key, value);
            }

        }else{
            synchronized (lruList){
                //referent byte[0]��֤������С
                KeyWord keyWord = new KeyWord(new byte[0],referenceQueue,key);
                lruList.put(key,System.currentTimeMillis());
                cache.put(key,value);
            }

        }

        //LOGGER.info(key + "-----------"+value);
    }


    public V get(K key){
        V value = cache.get(key);
        if(value!=null){
            Long p =lruList.get(key);
            if(p !=null){
                boolean isDelete = System.currentTimeMillis() - p>timeUnit.toMillis(ttl)?true:false;
                if(isDelete){
                    synchronized (lruList) {
                        lruList.remove(key);
                        cache.remove(key);
                    }
                    LOGGER.warn("TTL will remove "+key);
                    return null;
                }
            }
        }
        return value;
    }

    //ͳ��gc���յĶ�������
    int count = 0 ;
    boolean flag = true;
    Thread thread  = new Thread(() -> {
        Object obj = null;
        while(true){
            obj = referenceQueue.poll();
            if(obj != null){
                synchronized (lruList) {
                    LOGGER.info(" GC Will remove " + ((KeyWord) obj).getValue());
                    cache.remove(((KeyWord) obj).getValue());
                    lruList.remove(((KeyWord) obj).getValue());

                    //��gcҪ����ʱ���ֶ���lrucache��size����1/10������ͨ��lru�������ͷų������ڴ�
                    if(flag){
                        if(lruList.size()<=128){
                            lruList.setCapacity(128);
                        }else{
                            int num = lruList.size();
                            lruList.setCapacity(num - num/8);
                            flag = false;
                        }

                    }
                    count++;
                }

            }else{
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    });

    public class LRUList extends LinkedHashMap<K,Long> {
        private int capacity;
        public LRUList(int capacity){
            super(16, 0.75f, true);
            this.capacity = capacity;
        }
        public void setCapacity(int capacity){
            this.capacity = capacity;
        }
        public LRUList(int initialCapacity, float loadFactor, boolean accessOrder,int capacity) {
            super(initialCapacity, loadFactor, accessOrder);
            this.capacity = capacity;
        }

        @Override
        public Long  put(K key, Long value) {
            return super.put(key, value);
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<K, Long> eldest) {
            if (size() > capacity) {
                synchronized (lruList) {
                    LOGGER.warn("LRU will remove " + eldest.getKey() +"---"+capacity);
                    cache.remove(eldest.getKey());
                    this.remove(eldest.getKey());
                }
                return true;
            }
            return false;
        }
    }

    public Map<K,V> getCache(){
        return cache;
    }

    public LRUList getLruList(){
        return lruList;
    }


    public int getCount(){
        return count;
    }

}
