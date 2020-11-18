package com.jdm.hospital.perscenter.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.jdm.hospital.common.domain.Patient;

@Mapper
public interface BindingDao {
	//通过wid获取patientList
	List<Patient> queryPatientList(Integer id);
	//通过卡和wid获取patient信息
	Patient queryPatientByCardNoAndWid(@Param("cardNo")String cardNo, @Param("wid")Integer id);
	//解绑所有的卡
	int untyingAllPatientByWid(Integer id);
	//将传入的卡号状态改为绑定
	int updateBindingStausByCardNo(@Param("cardNo")String cardNo,@Param("isBinding")String isBinding);
	
	
}
