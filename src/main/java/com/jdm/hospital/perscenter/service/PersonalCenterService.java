package com.jdm.hospital.perscenter.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSONObject;

public interface PersonalCenterService {

	JSONObject personalInfo(JSONObject json, HttpServletRequest request, HttpServletResponse response);

	JSONObject queryRecodeList(JSONObject json, HttpServletRequest request, HttpServletResponse response);

	JSONObject queryQRCode(JSONObject json, HttpServletRequest request, HttpServletResponse response);

}
