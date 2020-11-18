package com.jdm.wechat.common.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.jdm.hospital.common.domain.Patient;
import com.jdm.wechat.common.domain.AccessToken;
import com.jdm.wechat.common.domain.WeChatUser;

@Mapper
public interface WxUserDao {
	//1通过id查询微信用户信息
	WeChatUser queryWechatUserInfoById(Integer id);
	//2通过openid获取微信用户信息
	WeChatUser queryWeChatUserByOPenid(String openid);
	
	
	//3通过微信id获取患者信息
	Patient getPatientByWid(Integer id);
	
	//保存微信用户信息
	int saveWechatUserInfo(WeChatUser weChatUser);
	
	//获取和更新acesstoken
	AccessToken getAccessToken();
	int updateAccessToken(@Param("accesstoken")String accesstoken,@Param("updateTime")String updateTime);
	//更新微信信息
	void updateWechatUserInfo(WeChatUser wxuser);
	
	
	
}
