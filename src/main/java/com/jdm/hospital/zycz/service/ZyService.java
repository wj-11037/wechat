package com.jdm.hospital.zycz.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSONObject;
import com.jdm.hospital.common.domain.WeChatOrder;

public interface ZyService {

	JSONObject queryPatInfoByZyh(JSONObject json, HttpServletRequest request, HttpServletResponse response);

	JSONObject queryPatInfoByZyhNo(JSONObject json, HttpServletRequest request, HttpServletResponse response);

	JSONObject rechageZyyjByZYNo(JSONObject json, HttpServletRequest request, HttpServletResponse response);

	JSONObject cardMoneychangeToZyyj(JSONObject json, HttpServletRequest request, HttpServletResponse response);

	JSONObject queryZyFeeByZyNo(JSONObject json, HttpServletRequest request, HttpServletResponse response);

	JSONObject queryDocInfoforPatByZyNo(JSONObject json, HttpServletRequest request, HttpServletResponse response);

	JSONObject queryZyFenLeiByZyNo(JSONObject json, HttpServletRequest request, HttpServletResponse response);

	JSONObject notifyToRechargeZyFee(WeChatOrder wechatOrder);

}
