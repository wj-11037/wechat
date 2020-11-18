package com.jdm.wechat.common.domain;

import com.jdm.hospital.common.domain.Patient;

import lombok.Data;

/**
    *      微信用户信息
 * @author Administrator
 */

@Data
public class WeChatUser {
	private Integer id;
	private String openid;//openid
	private String nickname;//昵称
	private Integer sex;//用户的性别（1是男性，2是女性，0是未知）
	private String country;
	private String province;
	private String city;
	private String language;
	private String headImgUrl;
	private String status;//用户状态 0-显示 1-禁用
	private String createTime;//创建时间
	private String updateTime;//更新时间
	private Patient patient;//医院的患者信息
}







