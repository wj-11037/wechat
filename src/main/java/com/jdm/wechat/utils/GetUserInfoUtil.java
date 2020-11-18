package com.jdm.wechat.utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * 获取微信用户信息
 *@author Allen
 *@date 2019年11月4日 上午10:28:51
 */
public class GetUserInfoUtil {
	
	
	public static void getWxUserInfo() throws UnsupportedEncodingException {
		String url="https://open.weixin.qq.com/connect/oauth2/authorize?appid=wx37bf6fac129dbff9&redirect_uri="
				   +URLEncoder.encode("http://jmxw.free.idcfengye.com/wechatlogin/logincheck","utf-8")
				   +"&response_type=code&scope=snsapi_base&state=1&connect_redirect=1#wechat_redirect"; 
		try {
			String sendGet = HttpRequest.sendGet(url);
			System.out.println("sendGet="+sendGet);
		} catch (IOException e) {
			e.printStackTrace();
			
		}	
	}
	
		public static void main(String[] args) {
			try {
				GetUserInfoUtil.getWxUserInfo();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			System.out.println();
		}
}
