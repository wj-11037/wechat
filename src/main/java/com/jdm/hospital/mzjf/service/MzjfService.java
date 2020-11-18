package com.jdm.hospital.mzjf.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSONObject;

public interface MzjfService {

	JSONObject queryJFList(JSONObject json, HttpServletRequest request, HttpServletResponse response);

	JSONObject queryPrescriptionDetailInfo(JSONObject json, HttpServletRequest request, HttpServletResponse response);

	JSONObject commitMZJF(JSONObject json, HttpServletRequest request, HttpServletResponse response);

}
