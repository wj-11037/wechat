package com.jdm.hospital.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 校验工具类
 *@author Allen
 *@date 2019年12月25日 下午2:50:20
 */
public class RegUtils {
     //手机号码
	 public static final String PHONE ="^(1[3-9])\\d{9}$";
	 //中文
	 public static final String CHINESE = "^[\\u4E00-\\u9FA5\\uF900-\\uFA2D]+$";  
	 //邮箱 
	 public static final String EMAIL = "^\\w+((-\\w+)|(\\.\\w+))*\\@[A-Za-z0-9]+((\\.|-)[A-Za-z0-9]+)*\\.[A-Za-z0-9]+$";  
	 
	 
	 
	 
	 /** 
	     * 校验手机号码 
	     * @param mobile 
	     * @return 
	     * @author lqyao 
	     */  
	    public static final boolean isMoblie(String mobile){  
	        boolean flag = false;  
	        if (null != mobile && !mobile.trim().equals("") && mobile.trim().length() == 11) {  
	            Pattern pattern = Pattern.compile(PHONE);  
	            Matcher matcher = pattern.matcher(mobile.trim());  
	            flag = matcher.matches();  
	        }  
	        return flag;  
	    }  
	    /** 
	     * 校验邮箱 
	     * @param value 
	     * @return 
	     * @author lqyao 
	     */  
	    public static final boolean isEmail(String value){  
	        boolean flag = false;  
	        if (null != value && !value.trim().equals("")) {  
	            Pattern pattern = Pattern.compile(EMAIL);  
	            Matcher matcher = pattern.matcher(value.trim());  
	            flag = matcher.matches();  
	        }  
	        return flag;  
	    }
	    /** 
	               * 校验中文 
	     * @param value 
	     * @return 
	     * @author lqyao 
	     */  
	    public static final boolean isCHINESE(String value){  
	        boolean flag = false;  
	        if (null != value && !value.trim().equals("")) {  
	            Pattern pattern = Pattern.compile(CHINESE);  
	            Matcher matcher = pattern.matcher(value.trim());  
	            flag = matcher.matches();  
	        }  
	        return flag;  
	    }
	    
	    
	    
	   public static void main(String[] args) {
		String m="19912366696";
		String c="涨s";
		String e="275660829@q.com";
		boolean moblie = isMoblie(m);
		boolean chinese = isCHINESE(c);
		boolean email = isEmail(e);
		
		System.out.println("手机："+moblie);
		System.out.println("中文："+chinese);
		System.out.println("邮箱："+email);
		
		
	} 
	    
}
