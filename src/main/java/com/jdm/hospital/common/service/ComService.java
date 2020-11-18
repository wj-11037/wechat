package com.jdm.hospital.common.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSONObject;

public interface ComService {

	JSONObject queryPatientAndRegist(HttpServletRequest req, HttpServletResponse res, JSONObject json);

	JSONObject toRegisHis(HttpServletRequest req, HttpServletResponse res, JSONObject json);

	JSONObject toBindingInfo(HttpServletRequest request, HttpServletResponse response, JSONObject json);

	JSONObject toRecharge(HttpServletRequest request, HttpServletResponse response, JSONObject json);

	JSONObject queryPatientOnly(HttpServletRequest req, HttpServletResponse res, JSONObject json);

	JSONObject queryBalance(HttpServletRequest request, HttpServletResponse response);

	JSONObject queryAllNews(HttpServletRequest request, HttpServletResponse response);
	
	JSONObject queryNewsDetailById(JSONObject json, HttpServletRequest request, HttpServletResponse response);

	JSONObject queryQrCodeInfo(JSONObject json, HttpServletRequest request, HttpServletResponse response);

	JSONObject queryRechargeResult(JSONObject json, HttpServletRequest request, HttpServletResponse response);;

}
