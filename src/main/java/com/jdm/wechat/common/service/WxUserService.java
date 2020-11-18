package com.jdm.wechat.common.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface WxUserService {

	void getWeChatInfo(HttpServletRequest request, HttpServletResponse response);

	String getbdweChatUserInfo(HttpServletRequest request, HttpServletResponse response);
	
}
