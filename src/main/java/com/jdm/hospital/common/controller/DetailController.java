package com.jdm.hospital.common.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import com.alibaba.fastjson.JSONObject;
import com.jdm.hospital.common.domain.HisConfig;
import com.jdm.hospital.gh.service.GHService;
import com.jdm.hospital.perscenter.service.PersonalCenterService;
import com.jdm.hospital.utils.JudeUtil;
import com.jdm.hospital.yygh.service.YYGHService;

@Controller
@RequestMapping("detail/")
public class DetailController {
	
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	@Autowired
	public PersonalCenterService personalCenterService;
	@Autowired
	JudeUtil judeUtil;
	@Autowired
	HisConfig hisConfig;
	@Autowired
	GHService ghService;
	@Autowired
	YYGHService yyghService;
	
	
	//跳转到挂号详情页面
	@RequestMapping(value = "ghDetail")
	public String getGHDetail(HttpServletRequest request,HttpServletResponse response) {
		return "gh/detail";
	}
	
	//跳转到预约挂号详情页面
	@RequestMapping(value = "yyghDetail")
	public String getYYGHDetail(HttpServletRequest request,HttpServletResponse response) {
		
		return "yygh/detail";
	}
		

	/**
	 * 详情入口
	 * @param params
	 * @return
	 */
	@RequestMapping(value = "service.do",method = RequestMethod.POST,produces="application/json;charset=UTF-8")
	@ResponseBody
	public JSONObject ghService(@RequestBody String params,HttpServletRequest request,HttpServletResponse response) {
		 
		JSONObject jsonResult=new JSONObject();
		JSONObject json =null;
		try {
			json =JSONObject.parseObject(params.trim());
		} catch (Exception e) {
			jsonResult.put("code","1");
			jsonResult.put("msg", "请求参数异常");
			log.error("请求参数异常{}",jsonResult);	
			return jsonResult;
		}
		 
		//2.验证参数合法性
		jsonResult =  judeUtil.judgeSign(json);
	    if(!jsonResult.getString("code").equals("0")) {
	    	log.info("===============公众号请求验证失败==================");
	    	return jsonResult;
	    }
		//3.主方法
	    jsonResult=mainMethod(json,request,response);
	    log.info("===============返回前端：{},",jsonResult);
		return jsonResult;
	}

	
	
	private JSONObject mainMethod(JSONObject json, HttpServletRequest request,HttpServletResponse response) {
		JSONObject jsonResult=new JSONObject();
		String method = json.getString("method");//方法 
			if(method.equals("1")){
				//获取挂号详情
				jsonResult=getGHDetail(json,request,response);
			}else if(method.equals("2")){
				//获取预约挂号详情
				jsonResult=getYYGhDetail(json,request,response);
			}else {
				log.info("===============请求方法错误================》请求参数：{}",json);
				jsonResult.put("code", 9001);
				jsonResult.put("msg", "请求方法错误");
			}
		
		return jsonResult;
	}
	
	/**
	 * 获取挂号详情
	 * @param json
	 * @param request
	 * @param response
	 * @return
	 */
	private JSONObject getGHDetail(JSONObject json, HttpServletRequest request, HttpServletResponse response) {
		JSONObject jsonResult=new JSONObject();
		jsonResult=ghService.getCurrentGH(json,request,response);
		return jsonResult;
	}
	
	/**
	 * 预约挂号详情
	 * @param json
	 * @param request
	 * @param response
	 * @return
	 */
	private JSONObject getYYGhDetail(JSONObject json, HttpServletRequest request, HttpServletResponse response) {
		JSONObject jsonResult=new JSONObject();
		jsonResult=yyghService.getCurrentYYGH(json,request,response);
		return jsonResult;
	}
	
}
