package com.example.myapplication.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class StreamUtil {
    /**
     * ��ת�����ַ���
     * @param is  ������
     * @return  ��ת���ɵ��ַ���    ����null�����쳣
     */
    public static String streamToString(InputStream is) {
        //�ڶ�ȡ�Ĺ����У�����ȡ�����ݴ洢�������У�Ȼ��һ���Ե�ת�����ַ���
        //1.������������
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        //2.��������������û��Ϊֹ(ѭ��)
        byte[] buffer = new byte[1024];
        //3.��¼��ȡ���ݵ���ʱ����
        int temp = -1;

        try {
            while((temp = is.read(buffer))!=-1){
                bos.write(buffer,0,temp);
            }
            
            //���ض�ȡ������
            return bos.toString();
            
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                is.close();
                bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
