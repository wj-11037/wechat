package com.jdm.hospital.yygh.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSONObject;

public interface YYGHService {

	JSONObject getDeptList(JSONObject json, HttpServletRequest request, HttpServletResponse response);

	JSONObject getdoctorList(JSONObject json, HttpServletRequest request, HttpServletResponse response);

	JSONObject commitYYGH(JSONObject json, HttpServletRequest request, HttpServletResponse response);

	JSONObject queryYYGHInfoByOrderCode(JSONObject json, HttpServletRequest request, HttpServletResponse response);

	JSONObject getYYGHRrcorde(JSONObject json, HttpServletRequest request, HttpServletResponse response);

	JSONObject getCurrentYYGH(JSONObject json, HttpServletRequest request, HttpServletResponse response);

	JSONObject refundYYGH(JSONObject json, HttpServletRequest request, HttpServletResponse response);

}
