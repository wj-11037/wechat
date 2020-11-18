package com.jdm.wechat.pay.dao;

import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.jdm.hospital.common.domain.WeChatOrder;

@Mapper
public interface PayNotifyDao {
	
	//1.根据订单号查询微信支付订单信息
	WeChatOrder queryWechatOrderByOrderNo(String orderNo);
	//2.更新订单状态
	int updateWechatOrderStatus(Map<String,Object> map);
	
	
}
