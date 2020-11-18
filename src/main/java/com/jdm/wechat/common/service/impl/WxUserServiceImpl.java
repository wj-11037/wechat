package com.jdm.wechat.common.service.impl;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson.JSONObject;
import com.jdm.hospital.common.domain.Patient;
import com.jdm.hospital.perscenter.dao.BindingDao;
import com.jdm.wechat.common.dao.WxUserDao;
import com.jdm.wechat.common.domain.WeChatUser;
import com.jdm.wechat.common.domain.WxConfig;
import com.jdm.wechat.common.service.WxUserService;
import com.jdm.wechat.utils.HttpRequest;


@Service
public class WxUserServiceImpl implements WxUserService{
	
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	@Autowired
	WxConfig wxConfig;
	@Autowired
	WxUserDao wxUserDao;
	@Autowired
	BindingDao bindingDao;
	
	/**
	 * 获取微信用户信息
	 */
	@Override
	public void getWeChatInfo(HttpServletRequest request, HttpServletResponse response){
		
		
		
		HttpSession session = request.getSession();
		String path = (String) session.getAttribute("path");//访问路径
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
				//获取微信用户信息后，保存微信信息,跳转注册页面去注册
				//session.setAttribute("weChatUser", weChatUser);
				queryAndsaveWeChatUserInfo(accessToken,openid,request,response);
				return;
			}
			//更新用户信息
			updateWeChatUserInfo(accessToken,openid);
			
			//==================3.通过微信用户id 获取患者信息 (开启且绑定)===========================
			Patient patient=wxUserDao.getPatientByWid(weChatUser.getId());
			//List<Patient> patientList = bindingDao.queryPatientList(weChatUser.getId());
			if(patient==null) {
				session.setAttribute("weChatUser", weChatUser);
				response.sendRedirect("/page/toRegist");
				return;
			}
			//================4.获取到患者信息后，将患者信息保存============================
			log.info("==============>session保存患者信息：{}，微信用户信息:{}=====>",patient,weChatUser);
			session.setAttribute("patient", patient);//将患者信息保存到session中
			session.setAttribute("weChatUser", weChatUser);
			response.sendRedirect(path);//跳转到业务访问的路径
			
		 } catch (Exception e) {
			e.printStackTrace();
			log.info("==============>获取用户信息信息异常=====>"+e);
		}
	}
	
	/**
	 * 更新用户信息
	 * @param accessToken
	 * @param openid
	 */
	private void updateWeChatUserInfo(JSONObject accessToken, String openid) {
		 String access_token = accessToken.getString("access_token");
	        String infoUrl = "https://api.weixin.qq.com/sns/userinfo?access_token="+access_token
	        					 + "&openid="+openid
	        					 + "&lang=zh_CN";
	        String userInfoStr="";
			try {
				userInfoStr = HttpRequest.sendGet(infoUrl);
				JSONObject userInfoJson=JSONObject.parseObject(userInfoStr);
				log.info("==============>更新微信用户信息，返回json:{}",userInfoJson);
				updateWechatUserInfo(userInfoJson);
			} catch (IOException e) {
				log.info("==============>更新微信用户信息异常==============");
			}
		}
	
	/**
	 * 有微信用户信息，更新用户信息
	 * @param userInfoJson
	 */
	private void updateWechatUserInfo(JSONObject userInfoJson) {
		WeChatUser wxuser =null;
		SimpleDateFormat sdf=new SimpleDateFormat("yyy-MM-dd HH:mm:ss");
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
			wxuser.setUpdateTime(sdf.format(new Date()));
			wxUserDao.updateWechatUserInfo(wxuser);//更新微信用户信息
			log.info("==============>更新微信用户的相关信息SUCCESS:{},",wxuser);
		} catch (Exception e) {
			log.info("==============>更新微信用户信息异常=============="+e);
		}
		
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
			try {
				response.sendRedirect("/page/toRegist");
			} catch (IOException e) {
				log.error("获取微信用户信息，保存用户信息时异常：{},",e);
			}
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
			response.sendRedirect("/page/toRegist");
		} catch (Exception e) {
			log.info("==============>通过openid获取微信用户信息，保存微信用户信息异常=============="+e);
		}
		
	}

	
	
	
	
	/**
	 * 绑定诊疗卡获取微信用户信息 
	 */
	@Override
	public String getbdweChatUserInfo(HttpServletRequest request, HttpServletResponse response) {
		HttpSession session = request.getSession();
			//==================1.通过code获取AccessToken===================
			String code = request.getParameter("code");
			log.info("==============>绑定诊疗卡,获取微信用户信息=======");
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
					log.info("==============>绑定诊疗卡,无微信用户信息，获取微信用户信息=======");
					saveWeChatUser(accessToken,openid,request,response);
				}else {
					log.info("==============>绑定诊疗卡,有微信用户信息，session保存微信用户信息=======");
					session.setAttribute("weChatUser", weChatUser);//将微信用户信息保存至session中
				}
			} catch (IOException e) {
				
				e.printStackTrace();
			}
			return "binding/binding";
	}
	
	/**
	 * 查询微信用户信息
	 * @param accessToken
	 * @param openid
	 * @param request
	 * @param response
	 */
	private void saveWeChatUser(JSONObject accessToken, String openid, HttpServletRequest request,
			HttpServletResponse response) {
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

			HttpSession session = request.getSession();
			SimpleDateFormat sdf=new SimpleDateFormat("yyy-MM-dd HH:mm:ss");
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
			} catch (Exception e) {
				log.info("==============>绑定诊疗卡获取微信用户信息,异常=============="+e);
			}
		} catch (IOException e) {
			log.info("==============>绑定诊疗卡获取微信用户信息异常==============");
		}
		
	}

}
