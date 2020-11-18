package com.jdm.hospital.perscenter.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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
import com.jdm.hospital.perscenter.service.BindingService;
import com.jdm.hospital.utils.JudeUtil;
import com.jdm.wechat.common.domain.WxConfig;

@Controller
@RequestMapping("binding")
public class BindingController {
	
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	@Autowired
	public BindingService bindingService;
	@Autowired
	JudeUtil judeUtil;
	@Autowired
	HisConfig hisConfig;
	@Autowired
	WxConfig wxConfig;
	
	
	//跳到绑定诊疗卡页面
	@RequestMapping(value = "binding")
	public void goBingDing(HttpServletRequest request,HttpServletResponse response) throws IOException {		
		//先获取微信用户信息
		try {
			log.info("==========绑定诊疗卡，先跳转微信获取用户信息======================");
			String url="https://open.weixin.qq.com/connect/oauth2/authorize?appid="+
					wxConfig.getAppid()+"&redirect_uri="+
					URLEncoder.encode(wxConfig.getProjectUrl()+"/weChat/bdweChatUserInfo", "UTF-8")
					+"&response_type=code&scope=snsapi_userinfo&state=STATE#wechat_redirect";
			response.sendRedirect(url);
			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
	}
	
	//跳到添加就诊卡页面
	@RequestMapping(value = "add")
	public String add(HttpServletRequest request,HttpServletResponse response) {
		
		return "binding/add";
	}
	
	
	
	/**
	   * 个人中心请求入口
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
	    	log.info("==============>公众号请求验证失败==================");
	    	return jsonResult;
	    }
		//3.主方法
	    jsonResult=mainMethod(json,request,response);
	    log.info("==============>返回前端：{},",jsonResult);
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
				//log.info("=====查询诊疗卡集合=====>请求参数：{},"+json);
				jsonResult=queryCardList(json,request,response);
			}else if(method.equals("2")){
				//log.info("=====切换绑定诊疗卡=====>请求参数：{},"+json);
				jsonResult=bingDingCardInfo(json,request,response);
			}else if(method.equals("3")){
				//log.info("=====绑定新诊疗卡=====>请求参数：{},"+json);
				jsonResult=bingDingNewCardInfo(json,request,response);
			}else {
				log.info("===============请求方法错误================》请求参数：{}",json);
				jsonResult.put("code", 9001);
				jsonResult.put("msg", "请求方法错误");
			}
		
		return jsonResult;
	}
	
	/**
	 * 绑定新诊疗卡
	 * @param json
	 * @param request
	 * @param response
	 * @return
	 */
	private JSONObject bingDingNewCardInfo(JSONObject json, HttpServletRequest request, HttpServletResponse response) {
		JSONObject jsonResult=new JSONObject();
		jsonResult=bindingService.bingDingNewCardInfo(json,request,response);
		return jsonResult;
	}

	/**
	 * 查询诊疗卡集合
	 * @param json
	 * @param request
	 * @param response
	 * @return
	 */
	private JSONObject queryCardList(JSONObject json, HttpServletRequest request, HttpServletResponse response) {
		JSONObject jsonResult=new JSONObject();
		jsonResult=bindingService.queryCardList(json,request,response);
		return jsonResult;
	}

	/**
	 * 绑定诊疗卡
	 * @param json
	 * @param request
	 * @param response
	 * @return
	 */
	private JSONObject bingDingCardInfo(JSONObject json, HttpServletRequest request, HttpServletResponse response) {
		JSONObject jsonResult=new JSONObject();
		jsonResult=bindingService.bingDingCardInfo(json,request,response);
		return jsonResult;
	}
}
