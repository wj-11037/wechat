package com.jdm.hospital.common.domain;

import lombok.Data;

/*
 * 用户信息与微信用户关联
 */
@Data
public class Patient {
	private Integer id;
	private Integer wid;//微信id
	private String userName;//用户名
	private String sex;//性别
	private String birthday;//生日
	private String mobile;//手机
	private String address;//地址
	private String cardNo;//卡号
	private String idCardNo;//身份证号
	private String patientId;//患者ID
	private String createTime;//创建时间
	private String updateTime;//更新时间
	private String status;//状态：0-禁用1-开启
	private String isBinding;//是否绑定 0-未绑定 1- 绑定
	private String ageType;//年龄类型
}



