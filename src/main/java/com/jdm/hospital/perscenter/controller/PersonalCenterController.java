package com.jdm.hospital.perscenter.controller;

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
import com.jdm.hospital.perscenter.service.PersonalCenterService;
import com.jdm.hospital.utils.JudeUtil;

@Controller
@RequestMapping("hospital/person")
public class PersonalCenterController {
	
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	@Autowired
	public PersonalCenterService personalCenterService;
	@Autowired
	JudeUtil judeUtil;
	@Autowired
	HisConfig hisConfig;
	
	
	//1.跳到个人中心页面
	@RequestMapping(value = "personCenter")
	public String goToindex(HttpServletRequest request,HttpServletResponse response) {
		
		return "personcenter/person";
	}

	//2.历史操作记录
	@RequestMapping(value = "recode")
	public String mzRecode(HttpServletRequest request,HttpServletResponse response) {
		return "personcenter/recode";
	}
	
	
	/**
	 * 个人中心请求入口
	 * @param params
	 * @return
	 */
	@RequestMapping(value = "service.do",produces="application/json;charset=UTF-8")
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

	
	/**
	 * 个人中心--主方法
	 * @param json
	 * @param response 
	 * @param request 
	 * @return
	 */
	private JSONObject mainMethod(JSONObject json, HttpServletRequest request,
			HttpServletResponse response) {
		JSONObject jsonResult=new JSONObject();
		String method = json.getString("method");//方法 
		
			if(method.equals("1")){
				//log.info("=====个人信息=====>请求参数：{},"+json);
				jsonResult=personalInfo(json,request,response);
			}else if(method.equals("2")){
				//log.info("==========>历史操作记录请求参数：{},"+json);
				jsonResult=queryRecodeList(json,request,response);
			}else if(method.equals("3")){
				//log.info("==========>获取就诊码：{},"+json);
				jsonResult=queryQRCode(json,request,response);
			}else {
				log.info("===============请求方法错误================》请求参数：{}",json);
				jsonResult.put("code", 9001);
				jsonResult.put("msg", "请求方法错误");
			}
		
		return jsonResult;
	}
	/**
	 * 获取就诊码
	 * @param json
	 * @param request
	 * @param response
	 * @return
	 */
	private JSONObject queryQRCode(JSONObject json, HttpServletRequest request, HttpServletResponse response) {
		
		return personalCenterService.queryQRCode(json,request,response);
	}

	/**
	 * 历史操作记录
	 *@author Allen
	 *@date 2020年4月23日 下午3:36:09 
	 *@param json
	 *@param request
	 *@param response
	 *@return
	 */
	private JSONObject queryRecodeList(JSONObject json, HttpServletRequest request, HttpServletResponse response) {
		
		return personalCenterService.queryRecodeList(json,request,response);
	}

	/**
	 * 个人信息
	 * @param json
	 * @param request
	 * @param response
	 * @return
	 */
	private JSONObject personalInfo(JSONObject json, HttpServletRequest request, HttpServletResponse response) {
		JSONObject jsonResult=new JSONObject();
		jsonResult=personalCenterService.personalInfo(json,request,response);
		return jsonResult;
	}
}
