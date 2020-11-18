package com.jdm.wechat.common.domain;

import lombok.Data;

@Data
public class AccessToken {
	private Integer id;
	private String accesstoken;
	private String updateTime;
}
