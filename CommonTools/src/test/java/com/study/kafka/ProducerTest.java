package com.study.kafka;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

import java.util.Date;
import java.util.Properties;
import java.util.Random;

public class ProducerTest extends Thread{

	private final Producer<String, String> producer;
	
	public ProducerTest() {
		 Properties props = new Properties();   
         //Producer������
         props.setProperty("metadata.broker.list","ssecbigdata07:9093");    
         props.setProperty("serializer.class","kafka.serializer.StringEncoder");    
         props.put("request.required.acks","1"); 
         /*0��producer���ȴ�����brokerͬ����ɵ�ȷ�ϼ���������һ����������Ϣ����ѡ���ṩ��͵��ӳٵ��������;��Ա�֤������������������ʱĳЩ���ݻᶪʧ����leader��������producer����֪�飬����ȥ����Ϣbroker���ղ�������
           1��producer��leader�ѳɹ��յ������ݲ��õ�ȷ�Ϻ�����һ��message����ѡ���ṩ�˸��õ��;���Ϊ�ͻ��ȴ�������ȷ������ɹ�����д������leader����δ���ƽ�ʧȥ��Ψһ����Ϣ����
          -1��producer��follower����ȷ�Ͻ��յ����ݺ����һ�η�����ɡ� */
         ProducerConfig config = new ProducerConfig(props);    
         producer = new Producer<String, String>(config); 
	}
	//�߳����з���
	public void run() {
		Random random = new Random();
		try {    
           while(true){
        	     int num = random.nextInt(100)%(100-0+1) + 0;
            	 KeyedMessage<String, String> data = new KeyedMessage<String, String>("testrepair","the num is :",num+"");
            	 //������Ϣ
            	 producer.send(data);
            	 System.out.println(new Date() + "��"+  num+"");
            	 sleep(2000);
			}       
       
         } catch (Exception e) {    
             e.printStackTrace();    
         }    
         producer.close(); 
	}
	
	 public static void main(String[] args) {    
		 ProducerTest producerThread = new ProducerTest();
		 producerThread.start();
            
     }   
}