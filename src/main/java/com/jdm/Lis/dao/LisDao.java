package com.jdm.Lis.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.jdm.Lis.domain.Report;

@Mapper
public interface LisDao {

	List<Report> queryReportList();
	//根据日期查询检验报告
	List<Report> queryReportListByDate(Map<String, Object> map);
	//=========================================
	//1.查询门诊检验报告的数量
	List<Report> queryReportQuntity(Map<String, String> map);
	//2.获取检验项目名称
	List<Report> querySName(Map<String, String> mapqs);
	//3.获取检验报告明细
	List<Report> queryReportDetailList(Map<String, String> map);
	
	
	
}
