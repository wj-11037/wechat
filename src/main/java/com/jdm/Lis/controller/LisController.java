package com.jdm.Lis.controller;

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
import com.jdm.Lis.service.LisService;
import com.jdm.hospital.utils.JudeUtil;

/**
 * 检验报告
 *@author Allen
 *@date 2020年4月17日 下午2:43:58
 */
@Controller
@RequestMapping("hospital/lis")
public class LisController {
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	@Autowired
	private JudeUtil judeUtil;
	
	@Autowired
	private LisService lisService;
	
	
	
	//1.查询检验报告跳到首页
	@RequestMapping(value = "queryRepport")
	public String queryRepport(HttpServletRequest request,HttpServletResponse response) {
		return "query/report/index";
	}
	
	//2.输入住院号，选择查询日期
	@RequestMapping(value = "inputzyhAndDate")
	public String reportInputzyh(HttpServletRequest request,HttpServletResponse response) {
		return "query/report/inputzyhAndDate";
	}
	
	//3.跳到查询住院检验报告条数页面
	@RequestMapping(value = "zyReportQuntity")
	public String reportQuntity(HttpServletRequest request,HttpServletResponse response) {
		return "query/report/zyReportQuntity";
	}
	
	//4.跳到查询住院检验报告详情页面
	@RequestMapping(value = "zyReportDetail")
	public String zyReportDetail(HttpServletRequest request,HttpServletResponse response) {
		return "query/report/zyReportDetail";
	}
	
	
	//1.门诊检验报告选择日期
	@RequestMapping(value = "chooseDate")
	public String chooseDate(HttpServletRequest request,HttpServletResponse response) {
		return "query/report/chooseDate";
	}
	
	
	//2.门诊检验报告
	@RequestMapping(value = "mzReportQuantity")
	public String mzReport(HttpServletRequest request,HttpServletResponse response) {
		return "query/report/mzReportQuantity";
	}
	
	
	
	
	
	
	
		//公共请求入口
		@RequestMapping(value = "/service.do", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
		@ResponseBody
		public JSONObject playEntrance(@RequestBody String params,
				HttpServletRequest request,
				HttpServletResponse response){
			JSONObject jsonResult=new JSONObject();
			//1.将入参转为json
			JSONObject json =null;
			try {
				System.out.println("params.trim()====>"+params.trim());
				json =JSONObject.parseObject(params.trim());
			} catch (Exception e) {
				log.error("===============公共请求参数错误=====================");	
				jsonResult.put("msg", "请求参数错误");
				jsonResult.put("code", 9001);
				return jsonResult;
			}
			//2.验证参数合法性
			jsonResult =  judeUtil.judgeSign(json);
		    if(!jsonResult.getString("code").equals("0")) {
		    	log.info("===============请求验证失败==================");
		    	return jsonResult;
		     }
			//3.主方法
			jsonResult=mainMethod(request,response,json);
			log.info("===============公共请求返回前端：{},",jsonResult);
			return jsonResult;
		}


		private JSONObject mainMethod(HttpServletRequest request, HttpServletResponse response, JSONObject json) {
			JSONObject jsonResult=new JSONObject();
			String method = json.getString("method");//方法 
			
			if(method.equals("1")) {
				//log.info("===============查询检验报告数量 =========》请求参数：{},",json);
				jsonResult=queryReportQuntity(json,request,response);
			}else if(method.equals("2")) {
				//log.info("===============查询检验报告明细=======》请求参数：{},",json);	
				jsonResult=queryReportDetail(json,request,response);
			}else {
				log.info("===============请求方法错误================》请求参数：{}",json);
				jsonResult.put("code", 9001);
				jsonResult.put("msg", "请求方法错误");
				jsonResult.put("data", "");
			}
			return jsonResult;
		}
		
		
		
		/**
		 * 查询检验报告明细
		 *@author Allen
		 *@date 2020年4月17日 下午4:24:59 
		 *@param json
		 *@return
		 */
		private JSONObject queryReportDetail(JSONObject json, HttpServletRequest request,
				HttpServletResponse response) {
			return lisService.queryReportDetail(json,request,response);
		}


		/**
		 * 查询检验报告数量
		 *@author Allen
		 *@date 2020年4月17日 下午2:46:14 
		 *@param json
		 *@param request
		 *@param response
		 *@return
		 */
		private JSONObject queryReportQuntity(JSONObject json, HttpServletRequest request,
				HttpServletResponse response) {
			return lisService.queryReportQuntity(json,request,response);
		}

}
