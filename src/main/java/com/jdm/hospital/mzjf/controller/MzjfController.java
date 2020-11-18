package com.jdm.hospital.mzjf.controller;

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
import com.jdm.hospital.common.service.ComService;
import com.jdm.hospital.mzjf.service.MzjfService;
import com.jdm.hospital.utils.JudeUtil;

@Controller
@RequestMapping("hospital/mzjf")
public class MzjfController {
	
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	@Autowired
	public MzjfService mzjfService;
	@Autowired
	JudeUtil judeUtil;
	@Autowired
	ComService comService;
	
	
	//跳到缴费列表页面
	@RequestMapping(value = "jfList")
	public String jfList(HttpServletRequest request,HttpServletResponse response) {
		
		return "mzjf/jfList";
	}
	
	//跳到处方详情页面
	@RequestMapping(value = "jfDetail")
	public String goTojfDetail(HttpServletRequest request,HttpServletResponse response) {
		
		return "mzjf/detail";
	}
	
	//余额不足，跳到充值页面
	@RequestMapping(value = "recharge")
	public String goToRecharge(HttpServletRequest request,HttpServletResponse response) {
		
		return "mzjf/recharge";
	}
	//余额不足，跳到充值页面
	@RequestMapping(value = "transition")
	public String transition(HttpServletRequest request,HttpServletResponse response) {
		
		return "mzjf/transition";
	}
	
	/**
	 * 门诊缴费请求入口
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
	    log.info("==============>返回前端：{},",jsonResult);
		return jsonResult;
	}

	
	/**
	 * 挂号--主方法
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
				//log.info("=====门诊缴费--查询处方头信息=====>请求参数：{},"+json);
				jsonResult=queryJFList(json,request,response);
			}else if(method.equals("2")){
				//log.info("=====门诊缴费--查询处方费用信息=====>请求参数：{},"+json);
				jsonResult=queryPrescriptionDetailInfo(json,request,response);
			}else if(method.equals("3")){
				//log.info("=====门诊缴费--提交缴费=====>请求参数：{},"+json);
				jsonResult=commitMZJF(json,request,response);
			}else {
				log.info("===============请求方法错误================》请求参数：{}",json);
				jsonResult.put("code", 9001);
				jsonResult.put("msg", "请求方法错误");
			}
		
		return jsonResult;
	}
	
	/**
	 * 门诊缴费--提交缴费
	 * @param json
	 * @param request
	 * @param response
	 * @return
	 */
	private JSONObject commitMZJF(JSONObject json, HttpServletRequest request, HttpServletResponse response) {
		JSONObject jsonResult=new JSONObject();
		jsonResult=mzjfService.commitMZJF(json,request,response);
		return jsonResult;
	}



	/**
	 * 门诊缴费--查询处方费用信息
	 * @param json
	 * @param request
	 * @param response
	 * @return
	 */
	private JSONObject queryPrescriptionDetailInfo(JSONObject json, HttpServletRequest request,HttpServletResponse response) {
		JSONObject jsonResult=new JSONObject();
		jsonResult=mzjfService.queryPrescriptionDetailInfo(json,request,response);
		return jsonResult;
	}



	/**
	 * 获取缴费列表
	 * @param json
	 * @param request
	 * @param response
	 * @return
	 */
	private JSONObject queryJFList(JSONObject json, HttpServletRequest request, HttpServletResponse response) {
		JSONObject jsonResult=new JSONObject();
		jsonResult=mzjfService.queryJFList(json,request,response);
		return jsonResult;
	}
}
