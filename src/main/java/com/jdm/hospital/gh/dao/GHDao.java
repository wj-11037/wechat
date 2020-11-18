package com.jdm.hospital.gh.dao;

import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.jdm.hospital.common.domain.Recode;
import com.jdm.hospital.gh.domain.GHInfo;

@Mapper
public interface GHDao {
	//保存挂号信息
	void saveGHInfo(GHInfo ghinfo);
	//保存操作记录
	void saveRecode(Recode rc);
	//更新挂号id
	void updateGHInfo(GHInfo ghinfo);
	//获取挂号信息
	GHInfo queryGHInfoByOrderCode(String orderCode);
	//保存挂号参数
	int saveParams(Map<String, Object> map);
	//保存缴费的参数
	int saveMzJfParams(Map<String, Object> map);

}
