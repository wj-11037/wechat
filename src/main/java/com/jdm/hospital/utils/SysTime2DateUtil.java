package com.jdm.hospital.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * 
 *@author Allen
 *@date 2019年11月14日 下午2:26:30
 */
public class SysTime2DateUtil {
	/**
	 * 时间戳转为日期
	 *@author Allen
	 *@date 2019年11月29日 上午9:03:17 
	 *@param systime
	 *@return
	 */
	public static  String chaneToDate(String systime) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
	    String format = sdf.format(new Date(Long.valueOf(systime)));
		return format;
	}
	
	/** 
     * 日期格式字符串转换成时间戳 
     * @param date 字符串日期 
     * @param format 如：yyyy-MM-dd HH:mm:ss 
     * @return 
     */  
    public static String date2TimeStamp(String date_str){  
        try {  
        	
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");  
            return String.valueOf(sdf.parse(date_str).getTime());  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
        return "";  
    }  
    
    
    /**
               * 计算时间差，精确到毫秒
     *@author Allen
     *@date 2019年12月25日 上午8:28:23 
     *@param oldtime
     *@return
     */
    public static Long overTime(String oldtime){
    	Long ot=0l;
    	try {
			SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			long oldt = sdf.parse(oldtime).getTime()+60*30*1000;
			
			long nowtime=System.currentTimeMillis();
			
			ot=(oldt-nowtime);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return ot;  
    }  
    
    
    public static void main(String[] args) { 
    	String oldtime="2019-12-27 14:24:00";
    	Long overTime = overTime(oldtime);
    	System.out.println((overTime/1000)/60);
	}
    
    
    
    
}
