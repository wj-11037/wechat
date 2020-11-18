package com.jdm.hospital.common.domain;

import lombok.Data;

@Data
public class AccountClient {
	private int id;
	private String account;//账号
	private String password;//密码
	private String newPassword;//新密码
	private String key;//Key
	private int status;//0-开启  1- 禁用
	
	
	
	
}
