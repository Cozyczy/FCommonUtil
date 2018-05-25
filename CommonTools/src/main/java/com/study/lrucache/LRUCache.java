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
 * -Xms5M -Xmx5M -XX:-UseGCOverheadLimit -XX:SoftRefLRUPolicyMSPerMB=0
 * -XX:SoftRefLRUPolicyMSPerMB=N ��������Ƚ����õģ��ٷ������ǣ�Soft reference��������б��ڿͻ����д��ĸ���һЩ��
 * �����Ƶ�ʿ����������в��� -XX:SoftRefLRUPolicyMSPerMB=<N>�����ƣ������ָ��ÿ�׶ѿ��пռ�� soft reference ����
 * ��һ������ǿ�ɴ��ˣ��ĺ�����������ζ��ÿ�׶��еĿ��пռ��е� soft reference �ᣨ�����һ��ǿ���ñ�����֮��
 * ���1���ӡ�ע�⣬����һ�����Ƶ�ֵ����Ϊ  soft reference ֻ������������ʱ�Żᱻ��������������ղ������ڷ�����
 * ϵͳĬ��Ϊһ�룬�Ҿ���û��Ҫ��1�룬�ͻ����в��þ������������Ϊ -XX:SoftRefLRUPolicyMSPerMB=0��
 *   softreference�Ƿ��������
 *   clock-timestamp >= freespace*SoftRefLRUPolicyMSPerMB
 *   clock���ϴ�gc��ʱ��
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

    private class KeyWord<K> extends SoftReference<K> {
        private K value;
        public KeyWord(K referent, ReferenceQueue<? super K> q,K value) {
            super(referent, q);
            this.value = value;
        }
        public K getValue() {
            return value;
        }

        @Override
        protected void finalize() throws Throwable {
            super.finalize();
        }
    }

    public void put(K key,V value){
        if(cache.containsKey(key)){
            synchronized (lruList){
                lruList.put(key,System.currentTimeMillis());
            }
            cache.put(key, value);

        }else{
            synchronized (lruList){
                KeyWord keyWord = new KeyWord(new byte[0],referenceQueue,key);
                lruList.put(key,System.currentTimeMillis());
            }
            cache.put(key,value);
        }
        //�ڴ�ʹ�ó���80%��֪ͨϵͳ������gc����
        if(((double)(Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory())/Runtime.getRuntime().totalMemory())>0.8){
            System.gc();
        }
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
                    LOGGER.info("TTL will remove "+key);
                    return null;
                }
            }
        }
        return value;
    }


    int count = 0 ;
    Thread thread  = new Thread(() -> {
        Object obj = null;
        while(true){
            try {
                obj = referenceQueue.remove();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(obj!=null) {
                synchronized (lruList) {
                    //gc��ʽ�Ƴ�cache�е�����
                    LOGGER.info(" GC Will remove " + ((KeyWord) obj).getValue());
                    cache.remove(((KeyWord) obj).getValue());
                    lruList.remove(((KeyWord) obj).getValue());
                    count++;
                }
            }
        }
    });

    private class LRUList extends LinkedHashMap<K,Long> {
        private int capacity;
        public LRUList(int capacity){
            super(16, 0.75f, true);
            this.capacity = capacity;
        }
        public LRUList(int initialCapacity, float loadFactor, boolean accessOrder,int capacity) {
            super(initialCapacity, loadFactor, accessOrder);
            this.capacity = capacity;
        }
        public void setCapacity(int capacity){
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
                    //cache size �ﵽcapacity lru�Ƴ�����
                    LOGGER.info("LRU will remove " + eldest.getKey() +"---"+capacity);
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