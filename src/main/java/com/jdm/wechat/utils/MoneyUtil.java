package com.jdm.wechat.utils;

import java.math.BigDecimal;

/**
   * 钱工具类
 *@author Allen
 *@date 2019年11月28日 上午8:23:08
 */
public class MoneyUtil {
	
	/**
	 * 将double的金额 （元）转为 int分
	 *@author Allen
	 *@date 2019年11月28日 上午8:23:44 
	 *@param d
	 *@return
	 */
	public static int getIntegerFromDoubleString(String d) {
        if ("0".equals(d) || "".equals(d)) {
            return 0;
        }
        BigDecimal b = new BigDecimal(d).setScale(2, BigDecimal.ROUND_HALF_UP);
        BigDecimal bd = new BigDecimal(b.doubleValue() * 100).setScale(0, BigDecimal.ROUND_HALF_UP);
        return Integer.parseInt(bd.toString());
    }
	
	
}
