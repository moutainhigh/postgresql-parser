package com.fiberhome.generatedata;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * FP&DBOne测试表结构 javabean
 * @author Administrator
 *
 */
public class Person {
    /**
     * ID
     */
    @JSONField(name="id")
    private String ID;

	/**
	 * 性别
	 */
	@JSONField(name="sex")
	private String SEX;
	/**
	 * 年龄
	 */
	@JSONField(name="age")
	private String AGE;
	/**
	 * 省份
	 */
	@JSONField(name="province")
	private String PROVINCE;
	/**
	 * QQ号
	 */
	@JSONField(name="qq")
	private String QQ;
	/**
	 * WX号
	 */
	@JSONField(name="wx")
	private String WX;


    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getSEX() {
        return SEX;
    }

    public void setSEX(String SEX) {
        this.SEX = SEX;
    }

    public String getAGE() {
        return AGE;
    }

    public void setAGE(String AGE) {
        this.AGE = AGE;
    }

    public String getPROVINCE() {
        return PROVINCE;
    }

    public void setPROVINCE(String PROVINCE) {
        this.PROVINCE = PROVINCE;
    }

    public String getQQ() {
        return QQ;
    }

    public void setQQ(String QQ) {
        this.QQ = QQ;
    }

    public String getWX() {
        return WX;
    }

    public void setWX(String WX) {
        this.WX = WX;
    }

    public Person() {
		super();
		// TODO Auto-generated constructor stub
	}
	
}
