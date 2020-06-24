package com.fiberhome.generatedata;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.alibaba.fastjson.JSON;

public class BcpToJson {
	public static void main(String[] args) {
		serialize();
	}
	
	/**
	 * 序列化：由javabean的各个private具体属性值转成json串数据
	 */
	private static void serialize() {
		String inFileName = "data_50.bcp";
		String outFileName = "data_50_json";
		
		File inFile = new File(inFileName);
		File outFile = new File(outFileName);
		BufferedReader reader = null;
		BufferedWriter writer = null;
		try {
			reader = new BufferedReader(new FileReader(inFileName));
			writer = new BufferedWriter(new FileWriter(outFile));
			long startTime = System.currentTimeMillis();
			String line = null;
			while((line=reader.readLine())!=null){
				String[] strArray = null;
				strArray = line.split("\t");
				
				Person person = new Person();
				person.setID(strArray[0]);
				person.setSEX(strArray[1]);
				person.setAGE(strArray[2]);
				person.setPROVINCE(strArray[3]);
				person.setQQ(strArray[4]);
				person.setWX(strArray[5]);
				
//				String jsonstr = JSON.toJSONString(person);
//				System.out.println(jsonstr);	//此json格式数据可以入到FP
				
				Fpointer fp = new Fpointer();
				fp.setTablename("ws_test"); //得到的是tablename:part_test形式
//				fp.setPartition("20200430");
				fp.setData(person);//不需要添加"data"，在javabean中自动回生成"data":user形式
				String jsonString = JSON.toJSONString(fp);
				System.out.println(jsonString);
				//将生成的json流数据写入到新文本中
				writer.write(jsonString);
				writer.newLine();
				writer.flush();
			}
			long endTime = System.currentTimeMillis();
			System.out.println("耗时为：" + (endTime - startTime) + "ms");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally { 
			try {
				if(null!=writer){
					writer.close();
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			try {
				if (null!=reader){
					reader.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
