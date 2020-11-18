package com.jdm.Lis.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSONObject;

public interface LisService {


	JSONObject queryReportQuntity(JSONObject json, HttpServletRequest request, HttpServletResponse response);

	JSONObject queryReportDetail(JSONObject json, HttpServletRequest request, HttpServletResponse response);

}
