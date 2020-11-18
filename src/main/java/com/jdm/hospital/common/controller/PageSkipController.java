package com.jdm.hospital.common.controller;

import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jdm.hospital.common.domain.Patient;

/**
 * 
 * @author Allen
 *
 */
@Controller
@RequestMapping("page")
public class PageSkipController {
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	//跳转到注册页面
	@RequestMapping("/toRegist")
	public String toRegist() {
		return "register";
	}
	//医院简介页面
	@RequestMapping("hos_desc")
	public String hos_desc() {
		return "hospital/hos_desc";
	}
	
	//科室简介页面
	@RequestMapping("dept_desc")
	public String dept_desc() {
		
		return "hospital/dept_desc";
	}	
	
	//新闻动态
	@RequestMapping("news")
	public String news() {
		
		return "hospital/news";
	}
	
	//新闻详情
	@RequestMapping("news_detail")
	public String news_detail() {
		
		return "hospital/news_detail";
	}
	
	//跳转到注册页面
	@RequestMapping("/toBinding")
	public String toBindingPatient(HttpServletRequest requset,HttpServletResponse response,Model model) {
		List<Patient> patientList=new ArrayList<Patient>();
		
		try {
			HttpSession session = requset.getSession();
			JSONArray patientja = (JSONArray) session.getAttribute("bindInfo");
			for(int i=0;i<patientja.size();i++) {
				Patient patient=new Patient();
				JSONObject object = (JSONObject) patientja.get(i);
				patient.setIdCardNo(object.getString("idNo"));
				patient.setCardNo(object.getString("cardNo"));
				patient.setUserName(object.getString("userName"));
				patientList.add(patient);
			}
			model.addAttribute("patientList", patientList);
			log.info("================>session获取绑定患者信息:{},"+patientList);
			
		} catch (Exception e) {
			e.printStackTrace();
			log.info("================>session获取绑定患者信息异常:{},"+e);
		}
		
		return "binding";
	}
	
}
