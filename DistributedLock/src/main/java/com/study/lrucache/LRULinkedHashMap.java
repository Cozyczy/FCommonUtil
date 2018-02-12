package com.study.lrucache;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by lf52 on 2018/1/26.
 */
public class LRULinkedHashMap<K,V>  extends LinkedHashMap<K,V> {

    //�洢��������,map�����ݳ���capacityִ�й��ڲ���
    private int capacity;

    public LRULinkedHashMap(int capacity){
        super(16, 0.75f, true);
        this.capacity = capacity;
    }

    /**
     * jdk1.8�е�map����get����put������ʱ�����K-V�Ѿ����ڣ������afterNodeAccess������
     * �����afterNodeAccess�����У�accessOrderΪtrueʱ���ȵ���remove����ĵ�ǰ��βԪ�ص�ָ���ϵ��֮�����addBefore����������ǰԪ�ؼ���header֮ǰ��
     */
    public LRULinkedHashMap(int initialCapacity, float loadFactor, boolean accessOrder,int capacity) {
        super(initialCapacity, loadFactor, accessOrder);
        this.capacity = capacity;
    }

    /**
     * ��дremoveEldestEntry����
     *
     * ������Ԫ�ؼ���Map��ʱ������Entry��addEntry�����������removeEldestEntry����
     *   1.Ĭ�ϵ������removeEldestEntry����ֻ����false��ʾԪ����Զ�����ڣ����ڴ湻�õ������map�����ޱ�󣩡�
     *   2.��д�Ժ󣬵�map�ﵽcapacity��С�Ժ�᷵��true������ִ�й��ڲ��Ա�֤map�Ĵ�С�������ޱ��
     *
     * @param eldest
     * @return
     */
    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {

        //System.out.println(eldest.getKey() + "=" + eldest.getValue());

        if(size() > capacity) {
            return true;
        }

        return false;
    }

}
