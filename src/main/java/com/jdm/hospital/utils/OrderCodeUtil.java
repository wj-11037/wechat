package com.jdm.hospital.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class OrderCodeUtil {
	/**
	 * 生成订单号工具类
	 * @return
	 */
	public static String createOrderCode() {
		String orderStr=""; 
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		orderStr=sdf.format(new Date());
		for(int i=0;i<4;i++) {
			Random  ran=new Random();
			int nextInt = ran.nextInt(9);
			orderStr=orderStr+nextInt+"";
		}
		return orderStr;
	}
}
