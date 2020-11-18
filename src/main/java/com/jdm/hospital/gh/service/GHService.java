package com.jdm.hospital.gh.service;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSONObject;

public interface GHService {

	JSONObject getDeptList(JSONObject json, HttpServletRequest request, HttpServletResponse response);

	JSONObject getdoctorList(JSONObject json, HttpServletRequest request, HttpServletResponse response);

	JSONObject commitGH(JSONObject json, HttpServletRequest request, HttpServletResponse response);

	JSONObject getGHRrcorde(JSONObject json, HttpServletRequest request, HttpServletResponse response);

	JSONObject getCurrentGH(JSONObject json, HttpServletRequest request, HttpServletResponse response);

	int saveParams(Map<String, Object> map);

	int saveMzJfParams(Map<String, Object> map);


	

}
