package com.study.zkpersistence;

import com.alibaba.fastjson.JSON;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;

import java.io.*;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by lf52 on 2018/11/1.
 *
 * ���Խ�zkĳ���ڵ��µ������ӽڵ��Լ������ݰ���д���ļ��У�Ҳ���Ի��ڸ��ļ����ָ�zk������
 */
public class ZKPersistenceUtils {

    private static final Log logger = LogFactory.getLog(ZKPersistenceUtils.class);

    private static CuratorFramework client;

    private ConcurrentLinkedQueue<ZkNode> queue;

    public ZKPersistenceUtils(String zkaddr) {

        if(client == null){
            synchronized (this){
                client = CuratorFrameworkFactory.builder().connectString(zkaddr)
                        .sessionTimeoutMs(5000)
                        .connectionTimeoutMs(5000)
                        .retryPolicy(new RetryNTimes(Integer.MAX_VALUE, 5000))//һֱ����ֱ���ɹ�
                        .build();
                client.start();
            }
        }
        queue = new ConcurrentLinkedQueue();
    }

    /**
     * ��zkĳ���ڵ��µ����ݰ���д���ļ���
     * @param zkPath
     * @param filePath
     * @return
     */
    public boolean persistence(String zkPath,String filePath)  {

        boolean flag = true;

        FileOutputStream fos = null;
        BufferedWriter bw = null;
        try {
            File file = new File(filePath);
            fos = new FileOutputStream(file);
            bw = new BufferedWriter(new OutputStreamWriter(fos));

            ConcurrentLinkedQueue queue = copyNode(zkPath);
            final BufferedWriter finalBw = bw;
            queue.forEach(node -> {
                try {
                    finalBw.write(JSON.toJSONString(node));
                    finalBw.newLine();
                } catch (IOException e) {
                    logger.error(e);
                }
            });

        } catch (Exception e) {
            logger.error(e);
            flag = false;
        }finally {
            try {
                if(bw != null){
                    bw.close();
                }
                if(fos != null){
                    fos.close();
                }

            } catch (IOException e) {
                logger.error(e);
            }
        }

        return flag;
    }

    /**
     * ���ļ��е����ݻָ���zk��
     * @param zkPath
     * @param filePath
     * @return
     */
    public boolean recovery(String zkPath,String filePath)  {

        if(isExists(zkPath)){
            if(!"/".equals(zkPath)){
                throw new RuntimeException(zkPath + " is exist in ZK");
            }
        }else{
            //�ȴ������ڵ�
            try {
                client.create().withMode(CreateMode.PERSISTENT).forPath(zkPath);
            } catch (Exception e) {
                logger.error(zkPath + " node create error",e);
            }
        }


        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        try {

            fis = new FileInputStream(filePath);
            isr=new InputStreamReader(fis, "UTF-8");
            br = new BufferedReader(isr);

            String line = "";
            while ((line=br.readLine())!=null) {

                ZkNode node = JSON.parseObject(line, ZkNode.class);
                String path = node.getPath();
                String data = node.getData();
                if( !"/".equals(zkPath)){
                    if(!zkPath.equals(path.substring(0,path.indexOf("/",2)-1))){
                        path = zkPath + path.substring(path.indexOf("/",2),path.length());
                    }
                }
                if (data != null){
                    client.create().withMode(CreateMode.PERSISTENT).forPath(path,data.getBytes("UTF-8"));
                }else{
                    client.create().withMode(CreateMode.PERSISTENT).forPath(path);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
                if (isr != null) {
                    isr.close();
                }
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return true;
    }


    /**
     *
     * @param rootpath
     * @return
     * @throws Exception
     */
    public boolean deleteZkNode4Path(String rootpath) throws Exception {
        List<String> list = client.getChildren().forPath(rootpath);
        if(list.size() == 0){
            //û���ӽڵ�ֱ��ɾ��
            client.delete().forPath(rootpath);
        }else{
            //ѭ��ɾ�����нڵ�
            for (String node : list){
                String path = "";
                if (rootpath.equals("/"))
                    path = rootpath + node;
                else {
                    path = rootpath + "/" + node;
                }
                deleteZkNode4Path(path);
                System.out.println(path);

            }
            client.delete().forPath(rootpath);
        }

        return true;
    }


    /**
     * ��ȡĳ���ڵ�������ӽڵ㼰������
     * @param path
     * @return
     */
    private ConcurrentLinkedQueue copyNode(String path) throws Exception {

        if(!isExists(path)){
            throw new RuntimeException(path + " is not exist in ZK");
        }

        List<String> list = client.getChildren().forPath(path);
        if(list.size() > 0){
            list.forEach(node -> {
                try {
                    String childpath = formatPath(path, node);
                    byte[] data = client.getData().forPath(childpath);
                    queue.add(new ZkNode(childpath,(data != null && data.length > 0)? new String(data, "UTF-8") : null));
                    copyNode(childpath);
                } catch (Exception e) {
                    logger.error(e);
                }
            });
        }
        return queue;
    }


    /**
     * �жϵ�ǰ�ڵ��Ƿ����
     * @param path
     * @return
     */
    private boolean isExists(String path){
        try {
            if(client.checkExists().forPath(path) != null){
                return true;
            }
        } catch (Exception e) {
            logger.error("connect to ZK error", e);
        }

        return false;
    }

    /**
     * ��ʽ��zk�Ľڵ�·��
     * @param path
     * @param node
     * @return
     */
    private String formatPath(String path,String node){
        if (path.equals("/"))
            path = path + node;
        else {
            path = path + "/" + node;
        }
        return path;
    }



}
