package com.jdm.hospital.utils;

import java.text.DecimalFormat;

public class DecimalMoney {
	
	/**
	 * 保留两位小数
	 * @param money
	 * @return
	 */
	public static double decimalFormat(double money) {
		DecimalFormat decimalFormat = new DecimalFormat("#.00");
		return money = Double.parseDouble(decimalFormat.format(money));
	}
	
}
