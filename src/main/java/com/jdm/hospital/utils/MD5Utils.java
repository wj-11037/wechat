package com.jdm.hospital.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
public class MD5Utils {
	
	/**
	 * 将字符串MD5加密
	 * 2019年6月6日
	 */
	public static String Md5ToString(String str) {
		if (str == null || str.length() == 0) {
			throw new IllegalArgumentException("String to encript cannot be null or zero length");
		}
		StringBuffer hexString = new StringBuffer();
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(str.getBytes());
			byte[] hash = md.digest();
			for (int i = 0; i < hash.length; i++) {
				if ((0xff & hash[i]) < 0x10) {
					hexString.append("0" + Integer.toHexString((0xFF & hash[i])));
				} else {
					hexString.append(Integer.toHexString(0xFF & hash[i]));
				}
			}
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return hexString.toString();
	}
	
	public static void main(String[] args) {
		long timeStamp =System.currentTimeMillis()/1000;
		String signreqString="wxgzhwxgzh@dmin88bd2fdc-388c-45bb-9654-2ffb808ef3d7"+timeStamp;
		//pwd="wxgzh@dmin",先将密码MD5(pwd+FHM)
		String sign = MD5Utils.Md5ToString(signreqString);//签名入参
		System.out.println("时间戳="+timeStamp);
		System.out.println("MD5加密字符串="+sign);
	}
}
 







