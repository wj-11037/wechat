package com.jdm.Lis.domain;

import lombok.Data;

@Data
public class Result {
	private String dDate;//: 日期, 关键字段，与Report关联
	private Integer nIID;//: 仪器序号, 关键字段，与Report关联
	private Integer nSID;//: 样本号, 关键字段，与Report关联
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

	
}
