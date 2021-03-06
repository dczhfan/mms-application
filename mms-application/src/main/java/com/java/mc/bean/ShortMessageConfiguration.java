package com.java.mc.bean;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ShortMessageConfiguration implements java.io.Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1983335167716437959L;
	private Integer id;
	private String displayName;
	private Short smTunnel;
	private String componyName;
	private Long smAccessNumber;
	private String applicationId;
	private String applicationName;
	private String applicationPassword;
	private String extendCode;
	private String serviceAddress;
	private String serviceType;
	private int limitCycle;
	private int limitCount;
	private String status;
	private Timestamp createTime;
	private Timestamp updateTime;
	private String testPhoneNumber;
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getComponyName() {
		return componyName;
	}
	public void setComponyName(String componyName) {
		this.componyName = componyName;
	}
	public String getApplicationId() {
		return applicationId;
	}
	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}
	public String getApplicationName() {
		return applicationName;
	}
	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}
	
	@JsonIgnore
	public String getApplicationPassword() {
		return applicationPassword;
	}
	public void setApplicationPassword(String applicationPassword) {
		this.applicationPassword = applicationPassword;
	}
	public String getExtendCode() {
		return extendCode;
	}
	public void setExtendCode(String extendCode) {
		this.extendCode = extendCode;
	}
	public String getServiceAddress() {
		return serviceAddress;
	}
	public void setServiceAddress(String serviceAddress) {
		this.serviceAddress = serviceAddress;
	}
	public String getServiceType() {
		return serviceType;
	}
	public void setServiceType(String serviceType) {
		this.serviceType = serviceType;
	}
	public String getCreateTime() {
		if(this.createTime != null){
			return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(this.createTime);
		}
		return null;
	}
	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}
	public String getUpdateTime() {
		if(this.updateTime != null){
			return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(this.updateTime);
		}
		return null;
	}
	public void setUpdateTime(Timestamp updateTime) {
		this.updateTime = updateTime;
	}
	public Short getSmTunnel() {
		return smTunnel;
	}
	public void setSmTunnel(Short smTunnel) {
		this.smTunnel = smTunnel;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Long getSmAccessNumber() {
		return smAccessNumber;
	}
	public void setSmAccessNumber(Long smAccessNumber) {
		this.smAccessNumber = smAccessNumber;
	}
	public String getTestPhoneNumber() {
		return testPhoneNumber;
	}
	public void setTestPhoneNumber(String testPhoneNumber) {
		this.testPhoneNumber = testPhoneNumber;
	}
	public int getLimitCycle() {
		return limitCycle;
	}
	public void setLimitCycle(int limitCycle) {
		this.limitCycle = limitCycle;
	}
	public int getLimitCount() {
		return limitCount;
	}
	public void setLimitCount(int limitCount) {
		this.limitCount = limitCount;
	}
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
}
