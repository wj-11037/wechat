package com.jdm.hospital.perscenter.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSONObject;

public interface BindingService {
	
	JSONObject bingDingCardInfo(JSONObject json, HttpServletRequest request, HttpServletResponse response);

	JSONObject queryCardList(JSONObject json, HttpServletRequest request, HttpServletResponse response);

	JSONObject bingDingNewCardInfo(JSONObject json, HttpServletRequest request, HttpServletResponse response);

	

}
