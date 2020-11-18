package com.jdm.hospital.perscenter.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.jdm.hospital.common.domain.Recode;

@Mapper
public interface PersonalCenterDao {
	//查询当前微信用户+当前患者的历史记录
	List<Recode> queryRecodeList(Map<String, Object> map);
	
	
	
}
