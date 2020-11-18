package com.jdm.hospital.common.dao;


import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.jdm.hospital.common.domain.AccountClient;
import com.jdm.hospital.common.domain.ContentDO;
import com.jdm.hospital.common.domain.Patient;
import com.jdm.hospital.common.domain.Recode;
import com.jdm.hospital.gh.domain.GHInfo;

@Mapper
public interface CommonDao {
	//通过账号id查询账号信息
	AccountClient queryAccountClientINFO(String accountid);
	//保存患者信息
	void savePatient(Patient patient);
	//通过身份证号码查询患者信息
	Patient queryPatientByCardNoAndWid(@Param("cardNo")String cardNo,@Param("wid") Integer id);
	//通过插入的患者信息返回的主键id，获取患者信息
	Patient queryPatientByID(Integer id);
	void saveRecode(Recode rc);
	//通过微信id+订单号查询改订单是否已经支付
	Recode queryRecodeByOrderCode(@Param("wid")Integer wid, @Param("orderCode")String orderCode);
	List<ContentDO> queryAllNews();
	ContentDO getOneNews(String cid);
	void updateNewsbrowseNum(String string);
	//查询充值记录
	Recode queryRecode(String orderCode);
	
	//通过卡号+微信id查询患者信息集合
	List<Patient> queryPatientListByidCardAndWid(@Param("cardNo")String cardNo,@Param("wid")Integer wid);
	
	//获取挂号的排队号集合
	List<GHInfo> getPiaoHao(Map<String,Object> map);
	
	
}
