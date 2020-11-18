package com.jdm.hospital.gh.domain;

import lombok.Data;

/**
 * 订单
 * @author Allen
 */
@Data
public class GHInfo {
	private Integer id;
	private Integer wid;//微信id
	private Integer pid;//患者id
	private String actionType;//操作类型  GH-挂号 ,YYGH-预约挂号
	private String ghId;//挂号成功返回的id
	private String piaoHao;//票号
	private String orderCode;//订单号
	private Integer orderStatus;//订单状态
	private String amount;//订单金额 
	private String deptId;//科室ID
	private String deptName;//科室名称
	private String ghType;//挂号类型
	private String ghTypeId;//挂号类型编码
	private String doctorId;//医生ID
	private String doctorName;//医生
	private String period;//时段
	private String userName;//患者姓名
	private String cardNo;//患者卡号
	private String idCardNo;//身份证号
	private String condate;//就诊日期
	private String createTime;//创建时间
	private String updateTime;//更新时间	
	
}
