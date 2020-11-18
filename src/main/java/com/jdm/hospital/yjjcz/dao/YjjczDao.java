package com.jdm.hospital.yjjcz.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.jdm.hospital.common.domain.Recode;
import com.jdm.hospital.common.domain.WeChatOrder;

@Mapper
public interface YjjczDao {
	//保存微信订单数据
	void saveWeChatOrder(WeChatOrder wo);
	//根据订单号查询支付的订单信息
	WeChatOrder queryWeChatOrderByOrderNoAndWid(@Param("orderCode")String orderCode,@Param("wid")Integer wid);
	//修改订单的支付状态
	int updateOrderStatusByOrderNoAndWid(@Param("orderCode")String orderCode,
										 @Param("wid")Integer wid,
										 @Param("updateTime")String updateTime, 
										 @Param("flowNo")String flowNo);
	
	//根据订单号更新微信的数据
	int updateWechatOrderInfo(WeChatOrder wo);
	
	
	
	
}
