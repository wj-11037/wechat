package com.jdm.hospital.query.controller;

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
import com.jdm.hospital.query.service.QueryService;
import com.jdm.hospital.utils.JudeUtil;

@Controller
@RequestMapping("hospital/query")
public class QueryController {
	
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	@Autowired
	JudeUtil judeUtil;
	@Autowired
	QueryService queryService;
	
	//1.输入住院号页面
	@RequestMapping(value = "inputzyh")
	public String zyFlowDetailList(HttpServletRequest request,HttpServletResponse response) {
		
		return "query/zy/inputzyh";
	}
	
	//2.住院流水详情页面
	@RequestMapping(value = "zyfyls")
	public String zyfyls(HttpServletRequest request,HttpServletResponse response) {
		
		return "query/zy/zyfyls";
	}
	
	//3.门诊缴费记录页面
	@RequestMapping(value = "mzjfRecode")
	public String mzjfRecode(HttpServletRequest request,HttpServletResponse response) {
		return "query/mz/mzjf";
	}
	
	
	//4.报告查询页面
	@RequestMapping(value = "queryRepport")
	public String queryRepport(HttpServletRequest request,HttpServletResponse response) {
		return "query/zy/repport";
	}
	
	
	
	/**
	    * 住院相关请求入口
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
	
	
	/**
	 * 住院--主方法
	 * @param json
	 * @param response 
	 * @param request 
	 * @return
	 */
	private JSONObject mainMethod(JSONObject json, HttpServletRequest request,HttpServletResponse response) {
		JSONObject jsonResult=new JSONObject();
		String method = json.getString("method");//方法 
		
			if(method.equals("1")){
				//log.info("=====根据住院号获取当前在院病人费用流水=====>请求参数：{},"+json);
				jsonResult=zyFlowDetailList(json,request,response);
			}else if(method.equals("2")){
				//log.info("=====查询门诊费用列表=====>请求参数：{},"+json);
				jsonResult=mzFyList(json,request,response);
			}else if(method.equals("3")){
				//log.info("=====一卡通—根据卡号获取当前在院病人押金条明细=====>请求参数：{},"+json);
				jsonResult=depositList(json,request,response);
			}else {
				//log.info("===============请求方法错误================》请求参数：{}",json);
				jsonResult.put("code", 9001);
				jsonResult.put("msg", "请求方法错误");
			}
		
		return jsonResult;
	}
	
	/**
	    * 一卡通—根据卡号获取当前在院病人押金条明细
	 *@author Allen
	 *@date 2020年2月28日 下午3:02:37 
	 *@param json
	 *@param request
	 *@param response
	 *@return
	 */
	private JSONObject depositList(JSONObject json, HttpServletRequest request, HttpServletResponse response) {
		JSONObject jsonResult=new JSONObject();
		jsonResult=queryService.depositList(json,request,response);
		return jsonResult;
	}
	
	/**
	 * 查询门诊费用列表
	 * @param json
	 * @param request
	 * @param response
	 * @return
	 */
	private JSONObject mzFyList(JSONObject json, HttpServletRequest request, HttpServletResponse response) {
		//log.info("=====查询门诊费用列表======");
		return queryService.mzFyList(json,request,response); 
	}

	/**
	 * 根据住院号获取当前在院病人费用流水
	 * 住院
	 * @param json
	 * @param request
	 * @param response
	 * @return
	 */
	private JSONObject zyFlowDetailList(JSONObject json, HttpServletRequest request, HttpServletResponse response) {
		JSONObject jsonResult=new JSONObject();
		jsonResult=queryService.zyFlowDetailList(json,request,response);
		return jsonResult;
	}
}
