package com.jdm.hospital.common.domain;

import java.math.BigDecimal;

import lombok.Data;

/**
 * 操作记录
 * @author Allen
 */
@Data
public class Recode {
	private Integer id;
	private Integer wid;//微信id
	private Integer pid;//患者id
	private String bType;//业务类型
	private BigDecimal amount;//金额
	private String orderCode;//订单号
	private String name;//姓名
	private String cardNo;//卡号
	private String idCardNo;//身份证号
	private String createTime;//创建时间
	private String msg;//消息
	private String status;//1-成功 0-失败
}
