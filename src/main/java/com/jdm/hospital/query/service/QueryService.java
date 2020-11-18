package com.jdm.hospital.query.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSONObject;

public interface QueryService {

	JSONObject zyFlowDetailList(JSONObject json, HttpServletRequest request, HttpServletResponse response);

	JSONObject mzFyList(JSONObject json, HttpServletRequest request, HttpServletResponse response);

	JSONObject depositList(JSONObject json, HttpServletRequest request, HttpServletResponse response);

}
