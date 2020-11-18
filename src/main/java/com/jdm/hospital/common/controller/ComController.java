package com.jdm.hospital.common.controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

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
import com.jdm.hospital.utils.IdcardUtils;
import com.jdm.hospital.utils.JudeUtil;
import com.jdm.hospital.utils.RegUtils;
import com.jdm.wechat.common.dao.WxUserDao;
import com.jdm.wechat.common.domain.WeChatUser;
import com.jdm.wechat.common.domain.WxConfig;
import com.jdm.wechat.utils.HttpRequest;
/**
 * @author Allen
 */
@Controller
@RequestMapping("common")
public class ComController {
	
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	@Autowired
	private JudeUtil judeUtil;
	@Autowired
	HisConfig hisConfig;
	@Autowired
	ComService comService;
	@Autowired
	WxConfig wxConfig;
	@Autowired
	WxUserDao wxUserDao;
	
	//跳转首页
	@RequestMapping(value = "index")
	public void index(HttpServletRequest request,HttpServletResponse response) {
		try {
			String url="https://open.weixin.qq.com/connect/oauth2/authorize?appid="+
				wxConfig.getAppid()+"&redirect_uri="+
				URLEncoder.encode(wxConfig.getAuthorUrl(), "UTF-8")
				+"&response_type=code&scope=snsapi_userinfo&state=STATE#wechat_redirect"; 
			response.sendRedirect(url);
		} catch (IOException e) {
			log.error("跳转授权异常:{},",e);
		}
		
	}
	
	@RequestMapping(value = "/getweChatUserInfo")
	public String wxCallback(HttpServletRequest request,HttpServletResponse response) {
		HttpSession session = request.getSession();
		//==================1.通过code获取AccessToken===================
		String code = request.getParameter("code");
		try {
			 String accessTokenUrl = "https://api.weixin.qq.com/sns/oauth2/access_token?appid="+wxConfig.getAppid()
					 + "&secret="+wxConfig.getAppsecert()
					 + "&code="+code
					 + "&grant_type=authorization_code";
			 
			//==================2.发送请求获取accessToken====================
			String accessTokenStr = HttpRequest.sendGet(accessTokenUrl);
			JSONObject accessToken=JSONObject.parseObject(accessTokenStr);
			log.info("==============>获取accessTokenStr:{}",accessTokenStr);
			String openid = accessToken.getString("openid");
			
			WeChatUser weChatUser=wxUserDao.queryWeChatUserByOPenid(openid);
			
			if(weChatUser==null) {
				log.info("==============>数据库中无微信用户信息，跳转注册页面注册");
				//获取微信用户信息,存入session
				queryAndsaveWeChatUserInfo(accessToken,openid,request,response);
			}else {
				session.setAttribute("weChatUser", weChatUser);//将微信用户信息保存至session中
			}
			
			
		}catch (Exception e) {
			log.error("跳转授权，获取微信用户信息异常:{},",e);
		}
		
		return "index";
	}
	/**
	 * 调用微信接口获取微信用户信息
	 * @param accessToken
	 * @param openid
	 * @param request 
	 */
	private void queryAndsaveWeChatUserInfo(JSONObject accessToken, String openid, HttpServletRequest request,HttpServletResponse response) {
		//=================1.先通过openid获取用户信息===================
        String access_token = accessToken.getString("access_token");
        String infoUrl = "https://api.weixin.qq.com/sns/userinfo?access_token="+access_token
        					 + "&openid="+openid
        					 + "&lang=zh_CN";
        String userInfoStr="";
		try {
			userInfoStr = HttpRequest.sendGet(infoUrl);
			JSONObject userInfoJson=JSONObject.parseObject(userInfoStr);
			log.info("==============>从微信接口获取微信用户信息，返回json:{}",userInfoJson);
			saveWechatUserInfo(userInfoJson,request,response);
		} catch (IOException e) {
			log.info("==============>获取微信用户信息异常==============");
		}
	}
	/**
	 * 保存微信用户信息
	 * @param request 
	 * @param userInfoStr
	 */
	private void saveWechatUserInfo(JSONObject userInfoJson, HttpServletRequest request,HttpServletResponse response) {		
		HttpSession session = request.getSession();
		SimpleDateFormat sdf=new SimpleDateFormat("yyy-MM-dd HH:mm:ss");
		WeChatUser wu=wxUserDao.queryWeChatUserByOPenid(userInfoJson.getString("openid"));
		if(wu!=null) {//
			//将微信用户信息保存至session中，去注册
			session.setAttribute("weChatUser", wu);
			return;
		}
		//=================保存用户信息=============================
		WeChatUser wxuser =null;
		try {
			wxuser=new WeChatUser();
			wxuser.setHeadImgUrl(userInfoJson.getString("headimgurl"));
			wxuser.setLanguage(userInfoJson.getString("language"));
			wxuser.setNickname(userInfoJson.getString("nickname").trim());
			wxuser.setOpenid(userInfoJson.getString("openid"));
			wxuser.setProvince(userInfoJson.getString("province").trim());
			wxuser.setCity(userInfoJson.getString("city").trim());
			wxuser.setCountry(userInfoJson.getString("country").trim());
			wxuser.setSex(Integer.parseInt(userInfoJson.getString("sex")));
			wxuser.setStatus("1");//用户状态 1-显示 0-禁用
			wxuser.setCreateTime(sdf.format(new Date()));
			wxUserDao.saveWechatUserInfo(wxuser);//保存微信用户信息
			//通过插入数据的主键id获取微信用户信息
			WeChatUser weChatUser=wxUserDao.queryWechatUserInfoById(wxuser.getId());
			session.setAttribute("weChatUser", weChatUser);//将微信用户信息保存至session中，去注册
			log.info("==============>保存微信用户信息，跳转注册页面注册，weChatUser:{},",weChatUser);
		} catch (Exception e) {
			log.info("==============>通过openid获取微信用户信息，保存微信用户信息异常=============="+e);
		}
		
	}
	//跳转到充值页面
	@RequestMapping(value = "recharge")
	public String goDoctorDetail(HttpServletRequest request,HttpServletResponse response) {
		
		return "common/recharge";
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
	    	log.info("================>请求验证失败==================");
	    	return jsonResult;
	     }
		//3.主方法
		jsonResult=mainMethod(request,response,json);
		log.info("================>返回前端：{},",jsonResult);
		return jsonResult;
	}
	
	
	
	private JSONObject mainMethod(HttpServletRequest request,HttpServletResponse response, JSONObject json) {
		JSONObject jsonResult=new JSONObject();
		String method = json.getString("method");//方法 
		
		if(method.equals("1")) {
			//log.info("===============验证身份证号码=========》请求参数：{},",json);
			jsonResult=validateIDCardNo(json);
		}else if(method.equals("2")) {
			//log.info("===============验证手机号码=======》请求参数：{},",json);	
			jsonResult=validateMobile(json);
		}else if(method.equals("3")) {
			//log.info("===============查询患者信息=======》请求参数：{},",json);	
			jsonResult=queryPatientOnly(request,response,json);
		}else if(method.equals("4")) {
			//log.info("===============（查询患者信息）签约建档=======》请求参数：{},",json);	
			jsonResult=queryPatientAndRegist(request,response,json);
		}else if(method.equals("5")) {
			//log.info("===============绑卡（有卡的用户添加诊疗卡）=======》请求参数：{},",json);	
			jsonResult=toBindingInfo(request,response,json);
		}else if(method.equals("6")){
			//log.info("=====查询余额====>请求参数：{},"+json);
			jsonResult=queryBalance(json,request,response);
		}else if(method.equals("7")){
			//log.info("=====获取新闻列表：{},"+json);
			jsonResult=queryAllNews(json,request,response);
		}else if(method.equals("8")){
			//log.info("=====获取新闻详情：{},"+json);
			jsonResult=queryNewsDetailById(json,request,response);
		}else if(method.equals("9")){
			//log.info("=====获取二维码信息：{},"+json);
			jsonResult=queryQrCodeInfo(json,request,response);
		}else if(method.equals("10")){
			//log.info("=====获取充值结果：{},"+json);
			jsonResult=queryRechargeResult(json,request,response);
		}else {
			//log.info("===============请求方法错误================》请求参数：{}",json);
			jsonResult.put("code", 9001);
			jsonResult.put("msg", "请求方法错误");
			jsonResult.put("data", "");
		}
		return jsonResult;
	}
	
	/**
	    * 获取充值结果
	 *@author Allen
	 *@date 2020年3月26日 上午11:37:58 
	 *@param json
	 *@param request
	 *@param response
	 *@return
	 */
	private JSONObject queryRechargeResult(JSONObject json, HttpServletRequest request, HttpServletResponse response) {
		return comService.queryRechargeResult(json,request,response);
	}


	private JSONObject queryQrCodeInfo(JSONObject json, HttpServletRequest request, HttpServletResponse response) {
		return comService.queryQrCodeInfo(json,request,response);
	}


	/**
	 *获取新闻详情
	 *@author Allen
	 *@date 2020年3月5日 下午3:52:17 
	 *@param json
	 *@param request
	 *@param response
	 *@return
	 */
	private JSONObject queryNewsDetailById(JSONObject json, HttpServletRequest request, HttpServletResponse response) {
		return comService.queryNewsDetailById(json,request,response);
	}


	/**
	 * 获取新闻列表
	 *@author Allen
	 *@date 2020年3月4日 上午11:15:01 
	 *@param json
	 *@param request
	 *@param response
	 *@return
	 */
	private JSONObject queryAllNews(JSONObject json, HttpServletRequest request, HttpServletResponse response) {
		return comService.queryAllNews(request,response);
	}


	private JSONObject queryBalance(JSONObject json, HttpServletRequest request, HttpServletResponse response) {
		return comService.queryBalance(request,response);
	}


	//微信用户绑定患者用户信息
	private JSONObject toBindingInfo(HttpServletRequest request, HttpServletResponse response, JSONObject json) {
		return comService.toBindingInfo(request,response,json);
	}

	/**
	 * 查询患者信息，签约建档
	 * @param request
	 * @param response
	 * @param json
	 * @return
	 */
	private JSONObject queryPatientAndRegist(HttpServletRequest req, HttpServletResponse res, JSONObject json) {
		
		return comService.queryPatientAndRegist(req,res,json);
	}

	/**
	 * 查询患者信息
	 * @param response 
	 * @param request 
	 * @param json
	 * @return
	 * @throws IOException 
	 */
	private JSONObject queryPatientOnly(HttpServletRequest req, HttpServletResponse res, JSONObject json) {		
		
		return comService.queryPatientOnly(req,res,json);
	}

	

	/**
	    * 验证手机号码
	 *@author Allen
	 *@date 2019年12月28日 下午3:57:11 
	 *@param json
	 *@return
	 */
	private JSONObject validateMobile(JSONObject json) {
		JSONObject jsonResult=new JSONObject();	
		//1.校验联系人手机号码
		boolean moblie = RegUtils.isMoblie(json.getString("mobile"));
		if(moblie==false) {
			jsonResult.put("code", 9001);
			jsonResult.put("msg", "请输入正确的手机号码!");
			return jsonResult;
		}
		jsonResult.put("code", 0);
		jsonResult.put("msg", "手机号码正确");
		return jsonResult;
	}
	


	/**
	  * 验证身份证号码
	 *@author Allen
	 *@date 2019年12月28日 下午3:57:23 
	 *@param json
	 *@return
	 */
	private JSONObject validateIDCardNo(JSONObject json) {
		JSONObject jsonResult=new JSONObject();	
		//2.校验身份证
		
			String passengerCardNo = json.getString("id_no");
			boolean validateCard = IdcardUtils.validateCard(passengerCardNo);
			if(validateCard==false) {
				jsonResult.put("code", 9001);
				jsonResult.put("msg", "请输入正确的身份证号码!");
				return jsonResult;
			}
		
		jsonResult.put("code", 0);
		jsonResult.put("msg", "身份证号码正确");
		return jsonResult;
	}
	
}
