package com.study.lrucache;

import org.junit.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Created by lf52 on 2018/1/26.
 */
public class LRULinkedHashMapTest {

    /**
     * test LRULinkedHashMap��size�ﵽcapacity�Ժ��ִ�й��ڲ���
     */
    @Test
    public void test1(){
        WeakHashMap<String,String> wmap = new WeakHashMap<>();
        HashMap<String,String> lrumap = new LRULinkedHashMap(10);
        HashMap<String,String> map = new LinkedHashMap(10);
        for(int i = 0 ; i< 15 ;i++){
            lrumap.put("hello" + i , "world" + i);
            map.put("hello" + i , "world" + i);
        }
        System.out.println("lrumap size is : " + lrumap.size() + " : " + lrumap);
        System.out.println("map size is : " + map.size() + " : " + map);
    }

    /**
     * test lru ���ԣ�accessOrder = true ��
     */
    @Test
    public void test2(){

        LinkedHashMap<String, String> map = new LRULinkedHashMap(1000);
        map.put("a", "a"); //  a
        map.put("b", "b"); //  a b
        map.put("c", "c"); //  a b c
        map.put("a", "a"); //  b c a
        map.put("d", "d"); //  b c a d
        map.put("a", "a"); //  b c d a
        map.put("b", "b"); //  c d a b
        map.put("f", "f"); //  c d a b f
        map.put("g", "g"); //  c d a b f g

        /*put����getһ��map���Ѿ�����Ԫ�ػᰴlru��������*/
        map.get("d"); //c a b f g d
        for (Map.Entry<String, String> entry : map.entrySet()) {
            System.out.print(entry.getValue() + ", ");
        }
        System.out.println("=========================");

        map.get("a"); //c b f g d a
        for (Map.Entry<String, String> entry : map.entrySet()) {
            System.out.print(entry.getValue() + ", ");
        }
        System.out.println("=========================");

        map.get("c"); //b f g d a c
        for (Map.Entry<String, String> entry : map.entrySet()) {
            System.out.print(entry.getValue() + ", ");
        }
        System.out.println("=========================");

        map.get("b"); //f g d a c b
        for (Map.Entry<String, String> entry : map.entrySet()) {
            System.out.print(entry.getValue() + ", ");
        }
        System.out.println("=========================");

        /*put����getһ���µ�Ԫ�ز���ִ��lru����*/
        map.put("h", "h"); //f g d a c b h
        for (Map.Entry<String, String> entry : map.entrySet()) {
            System.out.print(entry.getValue() + ", ");
        }
        System.out.println("=========================");

        map.get("h"); //f g d a c b h
        for (Map.Entry<String, String> entry : map.entrySet()) {
            System.out.print(entry.getValue() + ", ");
        }
        System.out.println("=========================");
    }

    /**
     * test accessOrder = false ����ִ��lru���û����ԣ�put������ͬ��key������ͬ����λ��
     */
    @Test
    public void test3(){


        LinkedHashMap<String, String> map = new LRULinkedHashMap(16, 0.75f,false,1000);

        map.put("a", "a");
        map.put("b", "b");
        map.put("c", "c");
        map.put("a", "a");
        map.put("d", "d");
        map.put("a", "a");
        map.put("e", "e");
        map.put("b", "b");
        map.put("f", "f");
        map.put("g", "g");

        map.get("d");
        for (Map.Entry<String, String> entry : map.entrySet()) {
            System.out.print(entry.getValue() + ", ");
        }
        System.out.println("=========================");

        map.get("a");
        for (Map.Entry<String, String> entry : map.entrySet()) {
            System.out.print(entry.getValue() + ", ");
        }
        System.out.println("=========================");

        map.get("c");
        for (Map.Entry<String, String> entry : map.entrySet()) {
            System.out.print(entry.getValue() + ", ");
        }
        System.out.println("=========================");

        map.get("b");
        for (Map.Entry<String, String> entry : map.entrySet()) {
            System.out.print(entry.getValue() + ", ");
        }
        System.out.println("=========================");

        map.put("h", "h");
        for (Map.Entry<String, String> entry : map.entrySet()) {
            System.out.print(entry.getValue() + ", ");
        }
        System.out.println("=========================");

        map.get("h");
        for (Map.Entry<String, String> entry : map.entrySet()) {
            System.out.print(entry.getValue() + ", ");
        }
        System.out.println("=========================");
    }

}
