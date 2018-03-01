package com.study.serializedutil;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * protostuff ���л�������
 *
 * protostuffֻҪ��֤���ֶ�����������󣬶����õ���sunϵ�е�JDK,���Խ�����ӻ�ɾ��Bean�е��ֶε������
 */
public class ProtoStuffUtils{
    private static Objenesis objenesis = new ObjenesisStd(true);

    private static <T> Schema<T> getSchema(Class<T> cls)
    {
        /**
         * 2018/2/28 fix bug : RuntimeSchema.createFrom(cls) --> RuntimeSchema.getSchema(cls)
         * ֮ǰ�Ĵ����ÿ�ε������л��ķ�����ʱ��loadһ��class���ᵼ�³����ز��ϵ�����
         */
        Schema<T> schema = RuntimeSchema.getSchema(cls);
        return schema;
    }

    public static <T> byte[] serialize(T obj)
    {
        Class<T> cls = (Class<T>) obj.getClass();
        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        Schema<T> schema = getSchema(cls);
        byte[] data = ProtostuffIOUtil.toByteArray(obj,schema,buffer);
        return data;
    }
    public static <T> T deserialize(byte[] data,Class<T> cls)
    {
        T message = objenesis.newInstance(cls);
        Schema<T> schema = getSchema(cls);
        ProtostuffIOUtil.mergeFrom(data,message,schema);
        return message;
    }

    public static <T> void serialize(String path,T obj)
    {
        Class<T> cls = (Class<T>) obj.getClass();
        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        Schema<T> schema = getSchema(cls);
        FileOutputStream output = null;
        try {
            output = new FileOutputStream(path);
            ProtostuffIOUtil.writeTo(output,obj,schema,buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            try {
                output.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


}
