package com.jdm.hospital.zycz.controller;

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
import com.jdm.hospital.common.service.ComService;
import com.jdm.hospital.utils.JudeUtil;
import com.jdm.hospital.zycz.service.ZyService;

@Controller
@RequestMapping("hospital/zycz")
public class ZyController {
	
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	@Autowired
	public ZyService zyService;
	@Autowired
	JudeUtil judeUtil;
	@Autowired
	ComService comService;
	@Autowired
	HisConfig hisConfig;
	
	//跳到住院个人信息页面
	@RequestMapping(value = "zyInfo")
	public String goToindex(HttpServletRequest request,HttpServletResponse response) {
		
		return "zycz/zyinfo";
	}
	
	//跳到输入住院号页面
	@RequestMapping(value = "inputzyh")
	public String inputZyh(HttpServletRequest request,HttpServletResponse response) {
		
		return "zycz/inputzyh";
	}
	
	//跳到输入充值金额的页面
	@RequestMapping(value = "recharge")
	public String recharge(HttpServletRequest request,HttpServletResponse response) {
		
		return "zycz/recharge";
	}
	//跳到中转页面
	@RequestMapping(value = "transition")
	public String transition(HttpServletRequest request,HttpServletResponse response) {
		
		return "zycz/transition";
	}
	
	/**
	 * 住院相关请求入口
	 * @param params
	 * @return
	 */
	@RequestMapping(value = "service.do",method = RequestMethod.POST,produces="application/json;charset=UTF-8")
	@ResponseBody
	public JSONObject zyczEntrance(@RequestBody String params,HttpServletRequest request,HttpServletResponse response) {
		 
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
	 * 住院--主方法
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
				//log.info("=====按住院号查询在院病人基本信息(有一卡通的)=====>请求参数：{},"+json);
				jsonResult=queryPatInfoByZyh(json,request,response);
			}else if(method.equals("2")){
				//log.info("=====按住院号查询病人基本信息(-)=====>请求参数：{},"+json);
				jsonResult=queryPatInfoByZyhNo(json,request,response);
			}else if(method.equals("3")){
				//log.info("=====住院内部号虚拟为一卡通进行充值，并交住院押金=====>请求参数：{},"+json);
				jsonResult=rechageZyyjByZYNo(json,request,response);
			}else if(method.equals("4")){
				//log.info("=====就诊卡转住院押金=====>请求参数：{},"+json);
				jsonResult=cardMoneychangeToZyyj(json,request,response);
			}else if(method.equals("5")){
				//log.info("=====根据住院号获取当前在院病人费用流水=====>请求参数：{},"+json);
				jsonResult=queryZyFeeByZyNo(json,request,response);
			}else if(method.equals("6")){
				log.info("=====按住院号查询病人医嘱信息=====>请求参数：{},"+json);
				jsonResult=queryDocInfoforPatByZyNo(json,request,response);
			}else if(method.equals("7")){
				//log.info("=====根据住院号获取住院病人费用分类汇总=====>请求参数：{},"+json);
				jsonResult=queryZyFenLeiByZyNo(json,request,response);
			}else {
				//log.info("===============请求方法错误================》请求参数：{}",json);
				jsonResult.put("code", 9001);
				jsonResult.put("msg", "请求方法错误");
			}
		
		return jsonResult;
	}
	
	/**
	 * 根据住院号获取住院病人费用分类汇总
	 * @param json
	 * @param request
	 * @param response
	 * @return
	 */
	private JSONObject queryZyFenLeiByZyNo(JSONObject json, HttpServletRequest request, HttpServletResponse response) {
		JSONObject jsonResult=new JSONObject();
		jsonResult=zyService.queryZyFenLeiByZyNo(json,request,response);
		return jsonResult;
	}


	/**
	 * 按住院号查询病人医嘱信息
	 * @param json
	 * @param request
	 * @param response
	 * @return
	 */
	private JSONObject queryDocInfoforPatByZyNo(JSONObject json, HttpServletRequest request,HttpServletResponse response) {
		JSONObject jsonResult=new JSONObject();
		jsonResult=zyService.queryDocInfoforPatByZyNo(json,request,response);
		return jsonResult;
	}


	/**
	 * 根据住院号获取当前在院病人费用流水
	 * @param json
	 * @param request
	 * @param response
	 * @return
	 */
	private JSONObject queryZyFeeByZyNo(JSONObject json, HttpServletRequest request, HttpServletResponse response) {
		JSONObject jsonResult=new JSONObject();
		jsonResult=zyService.queryZyFeeByZyNo(json,request,response);
		return jsonResult;
	}


	/**
	 * 就诊卡转住院押金
	 * @param json
	 * @param request
	 * @param response
	 * @return
	 */
	private JSONObject cardMoneychangeToZyyj(JSONObject json, HttpServletRequest request,HttpServletResponse response) {
		JSONObject jsonResult=new JSONObject();
		jsonResult=zyService.cardMoneychangeToZyyj(json,request,response);
		return jsonResult;
	}


	/**
	 * 住院内部号虚拟为一卡通进行充值，并交住院押金
	 * @param json
	 * @param request
	 * @param response
	 * @return
	 */
	private JSONObject rechageZyyjByZYNo(JSONObject json, HttpServletRequest request, HttpServletResponse response) {
		JSONObject jsonResult=new JSONObject();
		jsonResult=zyService.rechageZyyjByZYNo(json,request,response);
		return jsonResult;
	}


	/**
	 * 按住院号查询在院病人基本信息(-)
	 * @param json
	 * @param request
	 * @param response
	 * @return
	 */
	private JSONObject queryPatInfoByZyhNo(JSONObject json, HttpServletRequest request, HttpServletResponse response) {
		JSONObject jsonResult=new JSONObject();
		jsonResult=zyService.queryPatInfoByZyhNo(json,request,response);
		return jsonResult;
	}


	/**
	 * 按住院号查询在院病人基本信息(有一卡通的)
	 * @param json
	 * @param request
	 * @param response
	 * @return
	 */
	private JSONObject queryPatInfoByZyh(JSONObject json, HttpServletRequest request, HttpServletResponse response) {
		JSONObject jsonResult=new JSONObject();
		jsonResult=zyService.queryPatInfoByZyh(json,request,response);
		return jsonResult;
	}
	
	
}
