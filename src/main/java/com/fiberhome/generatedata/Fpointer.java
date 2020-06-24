package com.fiberhome.generatedata;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * 整条json数据javabean对象
 * javabean-->即使用对象作为容器（一条数据就是一个对象）
 * @author Administrator
 *
 */
public class Fpointer {
	/**
	 * 表名key
	 */
	@JSONField(name="tablename")
	private String tablename;
	/**
	 * 分区key
	 */
	@JSONField(name="partition")
	private String partition;
	/**
	 * 全部字段key-->data
	 */
	private Person data;
	
	public String getTablename() {
		return tablename;
	}
	public void setTablename(String tablename) {
		this.tablename = tablename;
	}
	public String getPartition() {
		return partition;
	}
	public void setPartition(String partition) {
		this.partition = partition;
	}
	public Person getData() {
		return data;
	}
	public void setData(Person data) {
		this.data = data;
	}
	public Fpointer() {
		super();
	}
	
	
	
}
