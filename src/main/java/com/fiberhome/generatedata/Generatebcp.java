package com.fiberhome.generatedata;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class Generatebcp {

	public static void main(String[] args) {

		//String targetFilePath = "/home/ws/java/test.bcp";
		String targetFilePath = "F:/Workspaces/postgresql-parser/data_50.bcp";
		FileWriter writer = null;
		try{
			
			File targetFile = new File(targetFilePath);
			if (!targetFile.getParentFile().exists()) {
				targetFile.getParentFile().mkdirs();
			}
			writer = new FileWriter(targetFile);
			
			long currentTime = System.currentTimeMillis();
			for(int i=0;i<50;i++){
				String lineStr = generateOneLineStr(i,currentTime);
				writer.write(lineStr);
				writer.flush();
			}
			long lastTime = System.currentTimeMillis();
			System.out.println(lastTime - currentTime + "ms");
			
			
		}catch(Exception ex){
			ex.printStackTrace();
		}finally{
			if(writer != null){
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
	/**
	 * 产生单条数据
	 * @param i
	 * @param currentTime
	 * @return
	 */
	public static String generateOneLineStr(int i, long currentTime){
		String ID = getRandom();
		String CAPTURE_TIME = getCAPTURE_TIME();
		String SEX = (i % 2 == 0)?"男":"女";		//偶数为男，奇数为女
		String AGE = getAge();
		String PROVINCE = getProvince();
		String HEIGHT = getHeight();
		String IMEI = getIMEI();
		String IMSI = getImsi();
		String MAC = getMac();
		String QQ = getQQ();
		String WX = getWX();
//		String SAHNGWANG = String.valueOf(new Random().nextInt(2));		//valueof 返回
//		String NANJIANG = String.valueOf(new Random().nextInt(2));
//		String JIABAN = String.valueOf(new Random().nextInt(2));
//		String CHANGGE = String.valueOf(new Random().nextInt(2));
//		String TIAOWU = String.valueOf(new Random().nextInt(2));
//		String DIANYING = String.valueOf(new Random().nextInt(2));
		String PINGPANG = String.valueOf(new Random().nextInt(2));
		String BASEBALL = String.valueOf(new Random().nextInt(2));
		String FOOTBALL = String.valueOf(new Random().nextInt(2));
		
//		String lineStr = ID+","+CAPTURE_TIME+","+SEX+","+AGE+","+PROVINCE+","+HEIGHT+","+IMEI+","
//		               + IMSI+","+MAC+","+QQ+","+WX+","+PINGPANG+","+BASEBALL+","+FOOTBALL+"\n";
		String lineStr = ID + "\t" + SEX + "\t" + AGE + "\t" + PROVINCE + "\t" + QQ + "\t" + WX + "\r\n";
		
		return lineStr;
	}
	
	/**
	 * 获取CAPTURE_TIME
	 * @return
	 */
	  private static String getIMEI() {// calculator IMEI
			int r1 = 1000000 + new Random().nextInt(9000000);
			int r2 = 1000000 + new Random().nextInt(9000000);
			String input = r1 + "" + r2;
			char[] ch = input.toCharArray();
			int a = 0, b = 0;
			for (int i = 0; i < ch.length; i++) {
				int tt = Integer.parseInt(ch[i] + "");
				if (i % 2 == 0) {
					a = a + tt;
				} else {
					int temp = tt * 2;
					b = b + temp / 10 + temp % 10;
				}
			}
			int last = (a + b) % 10;
			if (last == 0) {
				last = 0;
			} else {
				last = 10 - last;
			}
			return input + last;
		}
	  
	private static String getImsi() {
		// 460022535025034
		String title = "4600";
		int second = 0;
		do {
			second = new Random().nextInt(8);
		} while (second == 4);
		int r1 = 10000 + new Random().nextInt(90000);
		int r2 = 10000 + new Random().nextInt(90000);
		return title + "" + second + "" + r1 + "" + r2;
	}
	
	private static String getMac(){
		char[] char1 = "abcdef".toCharArray();
		char[] char2 = "0123456789".toCharArray();
		StringBuffer mBuffer = new StringBuffer();
		for (int i = 0; i < 6; i++) {
			int t = new Random().nextInt(char1.length);
			int y = new Random().nextInt(char2.length);
			int key = new Random().nextInt(2);
			if (key ==0) {
				mBuffer.append(char2[y]).append(char1[t]);
			}else {
				mBuffer.append(char1[t]).append(char2[y]);
			}
			
			if (i!=5) {
				mBuffer.append(":");
			}
		}
		return mBuffer.toString();
	}
	private static String getQQ(){
		
		Random rand=new Random();
		String result="";
		String range[]={"6","7","8","9","10","11","12"};
		int numQQ=Integer.parseInt(randomOne(range));
		for(int a=0;a<numQQ;a++)
		{
			result+=rand.nextInt(10)+1;
			
		}
		return result;		
}
	private static String getWX() {  
		
		String range[]={"7","10","14"};
		int length=Integer.parseInt(randomOne(range));
		String phones[]={"139","158","138","151","157","187","132","171","177","179","186"};
		String phone=randomOne(phones);
	    String val = "";  
	    String val2 = ""; 
	    Random random = new Random();  
	    int rd=random.nextInt(2);
	    if (rd==0){
	    	for(int i = 0; i < length; i++) {          
		        String charOrNum = random.nextInt(2) % 2 == 0 ? "char" : "num";  
		        //输出字母还是数字  
		        if( "char".equalsIgnoreCase(charOrNum) ) {  
		            //输出是大写字母还是小写字母 
		            int temp = random.nextInt(2) % 2 == 0 ? 65 : 97;  
		            val += (char)(random.nextInt(26) + temp);  
		        } else if( "num".equalsIgnoreCase(charOrNum) ) {  
		            val += String.valueOf(random.nextInt(10));  
		        }  
		    }  
	    }else if(rd==1)
	    {
	    	for(int i = 0; i < 8; i++) {
	    		val2+=String.valueOf(random.nextInt(10));  
	    	}
	    	val=phone+val2;
	    }
		return val;
	    	
	    }
	
	
	
	public static String randomOne(String s[]) {
		return s[new Random().nextInt(s.length - 1)];
	}

//	private static String randomOne(String[] range) {
//		// TODO Auto-generated method stub
//		return null;
//	}

	public static String getCAPTURE_TIME(){
		Random rand = new Random();
		int i = rand.nextInt(1525749247);
		if(i < 1525144447){
			i = 1525144447 + rand.nextInt(10000);
		}
		return String.valueOf(i);
	}
	
	
	/**
	 * 获取ID
	 * @return
	 */
	public static String getRandom(){
		Random rand = new Random();
		int i = rand.nextInt(100);
		return String.valueOf(i);
	}
	
	/**
	 * 年龄
	 * @return
	 */
	public static String getAge(){
		Random rand = new Random();
		int i = rand.nextInt(100);
		return String.valueOf(i);
	}
	
	/**
	 * 省份
	 * @return
	 */
	public static String getProvince(){
		List<String> list = new ArrayList<String>();
		list.add("山东省");
		list.add("江苏省");
		list.add("浙江省");
		list.add("福建省");
		list.add("广东省");
		list.add("安徽省");
		list.add("河南省");
		list.add("河北省");
		list.add("辽宁省");
		list.add("吉林省");
		Random rand = new Random();
		int i = rand.nextInt(10);
		return list.get(i);
	} 
	
	/**
	 * 获取身高
	 * @return
	 */
	public static String getHeight(){
		Random rand = new Random();
		int i = rand.nextInt(200);
		return String.valueOf(i);
	}
	
	
	public static void write(String targetFilePath, String body) {
		try {
			File targetFile = new File(targetFilePath);
			if (!targetFile.getParentFile().exists()) {
				targetFile.getParentFile().mkdirs();
			}
			FileWriter writer = new FileWriter(targetFile);
			writer.write(body);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
