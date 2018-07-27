package com.study.javabasic;

import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lf52 on 2018/7/5.
 */
public class OOMTest {

    /**
     * -Xms5M -Xmx5M
     * jdk1.6 : java.lang.OutOfMemoryError: PermGen space
     * jdk1.7,jdk1.8 : java.lang.OutOfMemoryError: Java Heap space
     * 1.6 �ַ����������ڷ�������jdk1.7,jdk1.8�Ժ����������
     */
    @Test
    public void test1(){
        List<String> list = new ArrayList();
        for (int i = 0;i < Integer.MAX_VALUE;i++){
            String.valueOf(i).intern();
            list.add(String.valueOf(i).intern());
            System.out.println(String.valueOf(i).intern());
        }
    }

    /**
     * -XX:MetaspaceSize=5m -XX:MaxMetaspaceSize=5m
     * jdk1.6,jdk1.7 : java.lang.OutOfMemoryError: PermGen space
     * jdk1.8 : java.lang.OutOfMemoryError: Metaspace
     * jdk1.8�Ժ��Ƴ������ô�PermGen space������ص���Ϣ������metaspace��
     */
    @Test
    public void test2(){
        URL url = null;
        List<ClassLoader> classLoaderList = new ArrayList<ClassLoader>();
        try {
            url = new File("D:\\test.txt").toURI().toURL();
            URL[] urls = {url};
            while (true){
                ClassLoader loader = new URLClassLoader(urls);
                classLoaderList.add(loader);
                loader.loadClass("com.study.javabasic.OOMTest");
                System.out.println(loader);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
