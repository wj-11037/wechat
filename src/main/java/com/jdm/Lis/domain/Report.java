package com.jdm.Lis.domain;

import lombok.Data;

/**
   * 检验报告
 *@author Allen
 *@date 2020年4月16日 上午10:23:43
 */
@Data
public class Report {
	
	
	//========检验报告数量=======
	private String nIID;//仪器序号
	private String nSID;//样本号
	private String sANo;//申请单号
	private String sPID;//病员号
	private String sPName;//病人姓名
	private String sSex;//性别
	private String nSAge;//年龄
	private String sAUnit;//年龄单位
	private String nPTID;//病员类别ID
	private String nDID;//科别ID
	private String nZID;//病区ID
	private String sBedno;//病床号
	private String nSDID;//送检医生ID
	private String nTDID;//检验医生ID
	private String nSTID;//标本类型ID
	private String dSTime;//送检时间
	private String dPTime;//出检时间
	private String sRNote;//结果注释
	private String sRemark;//备注
	private String nADID;//审核医生ID，0-未审，>0已审
	private String bType;//样本类型，0-常规，1-急诊，2-质控
	private String sGerm;//微生物的细菌名称
	
	
	//result
	private String dDate;//: 日期, 关键字段，与Report关联
	private Integer nITID;//：项目类别ID, 关键字段
	private String sItem;//：项目代号，关键字段
	private String sItem2;//：项目次代号，关键字段，一般为1个空格。
	private String bTimes;//：检验次数，关键字段，一般为1。
	private float fOD;//：OD值
	private String bCType;//：浓度值类型。1-数字， 2-字符型
	private float fConc;//：浓度值，数字
	private String sConc;//：浓度值，字符
	private String sResult;//：结论
	private float fCutOff;//：CutOff值
	private String bAbnormal;//：结果是否异常，0-未判别，1-正常，2-偏低，3-偏高
	private String sAbnormal;//：异常符号
	private String tDoctor;//检验医生
	private String sDoctor;//送检医生
	private String dept;//开处方的科室
	private String iType;//检验类型
	//Item
	private String nTID;//项目类别ID 
	private String nID;//项目序号
	private String sName;//项目名称
	private String sUnit;//单位
	private String nDecimal;//小数位数
	private String fRLow;//参考下限
	private String fRHigh;//参考上限
	private String sPrint;//打印参考
	private String bPrint;//是否打印，0-不打印，1-打印
}
