package com.jdm.hospital.common.domain;

import lombok.Data;

/**
 * 微信订单数据
 * @author Allen
 */
@Data
public class WeChatOrder {
	private Integer id;
	private Integer wid;//微信id
	private Integer pid;//患者id
	private String btype;//业务类型
	private double amount;//金额
	private String name;//姓名
	private String cardNo;//卡号
	private String idCardNo;//身份证号
	private String flowNo;//流水号
	private String orderCode;//订单号
	private String orderStatus;//订单状态
	private String createTime;//创建时间
	private String updateTime;//更新时间
	private String zyh;//住院号
	private String sqBaId;//住院号内部号
	private String params;//参数
	private String mzjfAmount;//总金额
	private String jfjson;//缴费JSON
	
}
