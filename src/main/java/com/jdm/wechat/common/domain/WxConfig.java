package com.jdm.wechat.common.domain;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import lombok.Data;

/**
 * 微信服务好配置类
 *@author Allen
 *@date 2019年10月28日 上午9:50:12
 */
@Configuration
@Data
public class WxConfig {
	
	@Value("${wx.appId}")
	private String appid;
	
	@Value("${wx.appsecert}")
	private String appsecert;
	//商户
	@Value("${wx.mchid}")
	private String mchid;
	
	@Value("${wx.paternerkey}")
	private String paternerkey;
	
	//获取accesstoken地址
	@Value("${wx.access_token.url}")
	private String accessTokenUrl;
	//创建菜单地址
	@Value("${wx.createmenu.url}")
	private String createmenuUrl;
	
	//获取codeUrl
	@Value("${wx.weChatUser.url}")
	private String weChatUser;
	
	@Value("${wx.author.url}")
	private String authorUrl;
	
	//获取用户信息
	@Value("${wx.userInfo.url}")
	private String userInfoUrl;

	//授权地址
	@Value("${wx.auth.url}")
	private String authUrl;
	//============微信消息模板===================
	//1挂号消息模板======
	@Value("${wx.temp.gh}")
	private String ghTemp;
	//2预约挂号消息模板====
	@Value("${wx.temp.yygh}")
	private String yyghTemp;
	//3预交金充值====
	@Value("${wx.temp.yjj}")
	private String yjj;
	//4门诊缴费====
	@Value("${wx.temp.mzjf}")
	private String mzjf;
	//住院充值
	@Value("${wx.temp.zycz}")
	private String zycz;
	
	//=============支付，退款地址==================
	//微信支付
	@Value("${wx.ioap.pay}")
	private String ioapPay;
	//查询支付结果
	@Value("${wx.ioap.queryPay}")
	private String ioapQueryPay;
	
	
	//=============回调地址==================
	//项目地址
	@Value("${wx.project.url}")
	private String projectUrl;
	//预交金充值
	@Value("${wx.notify.url}")
	private String notifUrl;
	
}
