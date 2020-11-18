package com.jdm.wechat.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 时间戳转为时间格式
 *@author Allen
 *@date 2019年11月5日 上午11:25:41
 */

public class SysTime2DateUtil {
	
	public static  String chaneToDate(String systime) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
	    String format = sdf.format(new Date(Long.valueOf(systime+"000")));
		return format;
	}
	
}
