package com.jdm.wechat.common.controller;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import com.jdm.wechat.common.service.WxUserService;


/**
 * 获取微信用户信息
 *@author Allen
 *@date 2019年11月26日 下午5:08:47
 */
@Controller
@RequestMapping("/weChat")
public class WxUserController {
	
	@Autowired
	WxUserService wxUserService;
	
	/**
	 * 获取微信用户信息
	 *@author Allen
	 *@date 2020年3月13日 上午11:19:06 
	 *@param request
	 *@param response
	 */
	@RequestMapping(value = "/getweChatUserInfo")
	public void wxCallback(HttpServletRequest request,HttpServletResponse response) {
		wxUserService.getWeChatInfo(request,response);
	}
	
	
	
	
	/**
	 * 绑定诊疗卡获取微信用户信息
	 *@author Allen
	 *@date 2020年3月13日 上午11:18:51 
	 *@param request
	 *@param response
	 *@return
	 */
	@RequestMapping(value = "/bdweChatUserInfo")
	public String bdweChatUserInfo(HttpServletRequest request,HttpServletResponse response) {
		
		return wxUserService.getbdweChatUserInfo(request,response);
	}
}
