package com.jdm.wechat.utils;

import javax.servlet.http.HttpServletRequest;


/**
 * 获取ID地址
 * @author Allen
 *
 */
public class IPAddUtil {
	
	/**
	 * 获取请求ip地址
	 * @param request
	 * @return
	 */
	public static String getIP(HttpServletRequest request) {
		String ip = "";
		ip =request.getHeader("x-forwarded-for");
	    if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)){
	        ip = request.getHeader("Proxy-Client-IP");
	    }
	    if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)){
	        ip = request.getHeader("WL-Proxy-Client-IP");
	    }
	    if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)){
	        ip = request.getRemoteAddr();
	    }
	    if(ip.indexOf(",")!=-1){
	    	String[] ips = ip.split(",");
	    	ip = ips[0].trim();
	    }
		
		return ip;
	}
}
