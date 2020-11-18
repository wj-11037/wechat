package com.jdm.hospital.yjjcz.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSONObject;
import com.jdm.hospital.common.domain.Patient;
import com.jdm.hospital.common.domain.WeChatOrder;
import com.jdm.wechat.common.domain.WeChatUser;

public interface YjjczService {

	JSONObject toRecharge(HttpServletRequest request, HttpServletResponse response, JSONObject json);
	//保存微信支付数据
	void saveWeChatOrder(JSONObject json, Patient patient, WeChatUser weChatUser);
	WeChatOrder queryWeChatOrderByOrderNoAndWid(String orderCode, Integer id);
	int updateOrderStatusByOrderNoAndWid(String orderCode, Integer id,String updateTime, String transaction_id);
	JSONObject notifyToRecharge(WeChatOrder wechatOrder);
	//回调挂号
	JSONObject notifyToGH(WeChatOrder wechatOrder);
	//回调预约挂号
	JSONObject notifyToYYGH(WeChatOrder wechatOrder);
	//门诊缴费
	JSONObject notifyToMZJF(WeChatOrder wechatOrder);

}
