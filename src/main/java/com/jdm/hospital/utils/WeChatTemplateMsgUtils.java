package com.jdm.hospital.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.jdm.hospital.common.domain.HisConfig;
import com.jdm.hospital.common.domain.Patient;
import com.jdm.hospital.common.domain.WeChatOrder;
import com.jdm.hospital.gh.domain.GHInfo;
import com.jdm.wechat.common.dao.WxUserDao;
import com.jdm.wechat.common.domain.AccessToken;
import com.jdm.wechat.common.domain.WeChatUser;
import com.jdm.wechat.common.domain.WxConfig;
import com.jdm.wechat.utils.AccessTokenSchedueld;
import com.jdm.wechat.utils.HttpRequest;

/**
 * 发送模板消息工具类
 * @author Allen
 */
@Component
public class WeChatTemplateMsgUtils {
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	@Autowired
	WxConfig wxConfig;
	@Autowired
	HisConfig hisconfig;
	
	@Autowired 
	WxUserDao wxUserDao;
	@Autowired
	AccessTokenSchedueld accessTokenSchedueld;
	
	//挂号模板消息response.getString("PiaoHao");//票号
	public void sendGHTemp(GHInfo ghInfo,String openId, JSONObject response) {
		try {
			SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
			AccessToken accessToken=wxUserDao.getAccessToken();
			if(accessToken.getAccesstoken().isEmpty()) {//如果acsesstoken为空，则主动更新获取
				log.info("====acsesstoken为空，主动获取=======");
				accessTokenSchedueld.getAccessToken();
				accessToken=wxUserDao.getAccessToken();
			}
			
			String postUrl = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=" + accessToken.getAccesstoken();
			//==============================================
			String resultMsg = response.getString("resultCode").equals("1")?"成功":"失败";
			
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("touser", openId.trim());//
			jsonObject.put("template_id",wxConfig.getGhTemp().trim());
			jsonObject.put("url", wxConfig.getProjectUrl()+"/hospital/gh/ghDetail?order_id="+ghInfo.getOrderCode());
 
			JSONObject data = new JSONObject();
			JSONObject first = new JSONObject();
			//first.put("value", "(测试阶段，不能就诊，给您带来不便，请谅解),门诊挂号："+resultMsg);
			first.put("value", "门诊挂号："+resultMsg);
			first.put("color", "#173177");
			JSONObject keyword1 = new JSONObject();
			keyword1.put("value", ghInfo.getUserName().trim());//就诊人
			keyword1.put("color", "#173177");
			JSONObject keyword2 = new JSONObject();
			keyword2.put("value", ghInfo.getCardNo().trim());//卡号
			keyword2.put("color", "#173177");
			JSONObject keyword3 = new JSONObject();
			keyword3.put("value", ghInfo.getDeptName().trim());//科室
			keyword3.put("color", "#173177");
			JSONObject keyword4 = new JSONObject();
			keyword4.put("value", ghInfo.getDoctorName().trim()+" 排队号："+"第"+piaoHao(response.getString("PiaoHao"),ghInfo.getPeriod())+"号");//医生名称：+",排队号："+response.getString("PiaoHao")
			keyword4.put("color", "#173177");
			JSONObject keyword5 = new JSONObject();
			keyword5.put("value",sdf.format(new Date())+"("+ ghInfo.getPeriod()+")");//时段
			keyword5.put("color", "#173177");
			JSONObject remark = new JSONObject();
			remark.put("value", "就诊时请携带好您的身份证，如有疑问请询问医院相关工作人员！");
			remark.put("color", "#173177");
			
			data.put("first",first);
			data.put("keyword1",keyword1);
			data.put("keyword2",keyword2);
			data.put("keyword3",keyword3);
			data.put("keyword4",keyword4);
			data.put("keyword5",keyword5);
			data.put("remark",remark);
 
			jsonObject.put("data", data);
	  
       String retInfo = HttpRequest.sendPostJson(postUrl, jsonObject.toJSONString());
       log.info("============>挂号，发送消息模板返回{},", retInfo);
       JSONObject parseObject = JSONObject.parseObject(retInfo);
//       if(!parseObject.getString("errcode").equals("0")) {
//    	   //accesstoken失效的话，主动创建accesstoken,再发一次
//    	   accessTokenSchedueld.getAccessToken();
//    	   //sendGHTemp(ghInfo,openId, response);
//       }
	} catch (Exception e) {
			 log.info("============>挂号消息模板发送异常，{}", e);
			e.printStackTrace();
	}
  }
	
	//排队号转换
	private String piaoHao(String piaoHao, String period) {
		String piaohao="";
		//上午号：1-4999   上午加号：5000-9999下午：10000-14999  下午加号：15000-19999
		if(period.equals("下午")) {
			piaohao=(Integer.parseInt(piaoHao)-9999)+"";
		}else {
			piaohao=piaoHao;
		}
		return piaohao;
	}

	/**
	 * 发送预约挂号消息
	 * @param json 
	 * @param json
	 * @param patient
	 * @param openid
	 */
	public void sendYYGHTemp(JSONObject json, GHInfo ghinfo,String openid, JSONObject response) {
		try {
				AccessToken accessToken=wxUserDao.getAccessToken();
				if(accessToken.getAccesstoken().isEmpty()) {//如果acsesstoken为空，则主动更新获取
					log.info("====acsesstoken为空，主动获取=======");
					accessTokenSchedueld.getAccessToken();
					accessToken=wxUserDao.getAccessToken();
				}
				
				String postUrl = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=" + accessToken.getAccesstoken();
				//==============================================
				String resultMsg = response.getString("resultCode").equals("1")?"成功":"失败";
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("touser", openid.trim());//
				jsonObject.put("template_id",wxConfig.getYyghTemp().trim());
				jsonObject.put("url", wxConfig.getProjectUrl()+"/hospital/yygh/ghDetail?order_id="+ghinfo.getOrderCode()+"&time="+json.getString("time"));
	 
				JSONObject data = new JSONObject();
				JSONObject first = new JSONObject();
				first.put("value", "预约挂号："+resultMsg);
				//first.put("value", "(测试阶段，不能就诊，给您带来不便，请谅解),预约挂号："+resultMsg);
				first.put("color", "#173177");
				JSONObject keyword1 = new JSONObject();
				keyword1.put("value", hisconfig.getHosname().trim());//医院名称
				keyword1.put("color", "#173177");
				
				JSONObject keyword2 = new JSONObject();
				keyword2.put("value", ghinfo.getDeptName().trim());//科室
				keyword2.put("color", "#173177");
				
				JSONObject keyword3 = new JSONObject();
				//keyword3.put("value",  ghinfo.getDoctorName().trim());//医生名称
				keyword3.put("value", ghinfo.getDoctorName().trim()+" 排队号："+"第"+piaoHao(response.getString("PiaoHao"),ghinfo.getPeriod())+"号");//医生名称：+",排队号："+response.getString("PiaoHao")
				keyword3.put("color", "#173177");
				
				JSONObject keyword4 = new JSONObject();
				keyword4.put("value", ghinfo.getUserName().trim());//就诊人
				keyword4.put("color", "#173177");
				
				JSONObject keyword5 = new JSONObject();
				keyword5.put("value",ghinfo.getCondate()+"("+ghinfo.getPeriod()+")");//预约就诊时间
				keyword5.put("color", "#173177");
				
				JSONObject remark = new JSONObject();
				remark.put("value", "就诊时请携带好您的身份证，如需取消预约，点击查看详情进行退号，如有疑问请询问医院相关工作人员！");
				remark.put("color", "#173177");
				
				data.put("first",first);
				data.put("keyword1",keyword1); 
				data.put("keyword2",keyword2);
				data.put("keyword3",keyword3);
				data.put("keyword4",keyword4);
				data.put("keyword5",keyword5);
				data.put("remark",remark);
	 
				jsonObject.put("data", data);
				 log.info("============>模板消息json:{},", jsonObject.toString().trim());
	       String retInfo = HttpRequest.sendPostJson(postUrl, jsonObject.toString().trim());
	       log.info("============>预约挂号，发送消息模板返回:{},", retInfo);
//	       JSONObject parseObject = JSONObject.parseObject(retInfo);
//	       if(!parseObject.getString("errmsg").equals("ok")) {
//	    	   //accesstoken失效的话，主动创建accesstoken,再发一次
//	    	   accessTokenSchedueld.getAccessToken();
//	    	   sendYYGHTemp(ghinfo, openid,response);
//	       }
	      
		} catch (Exception e) {
				 log.info("============>预约挂号消息模板发送异常，{}", e);
				e.printStackTrace();
		}

	}
	
	
	/**
	 * 发送门诊缴费模板消息
	 * prescriptionMoney,
	 * @param json 
	 * @param patient
	 * @param response
	 * @param openid
	 */
	public void sendMZJFTemp(JSONObject json, Patient patient, JSONObject response, String openid) {
		try {
			AccessToken accessToken=wxUserDao.getAccessToken();
			if(accessToken.getAccesstoken().isEmpty()) {//如果acsesstoken为空，则主动更新获取
				log.info("====acsesstoken为空，主动获取=======");
				accessTokenSchedueld.getAccessToken();
				accessToken=wxUserDao.getAccessToken();
			}
			
			String postUrl = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=" + accessToken.getAccesstoken();
			//==============================================
			String resultMsg = response.getString("resultCode").equals("1")?"成功":"失败";
			String sjh="";
			String prescriptionMoney="";
			if(response.getString("resultCode").equals("1")) {
				sjh=response.getString("SJH");
				prescriptionMoney=response.getString("prescriptionMoney");
			}
			
			
			JSONObject object = (JSONObject) json.getJSONArray("data").get(0);
			String sqMzDocPatientId = object.getString("sqMzDocPatientId");
			String sqMzDocPatientItemNum =object.getString("sqMzDocPatientItemNum");
			String myType =object.getString("myType");
			
			
			String url=wxConfig.getProjectUrl()+"/hospital/mzjf/jfDetail?myType="+
											myType+"&sqMzDocPatientId="+sqMzDocPatientId+
											"&sqMzDocPatientItemNum="+sqMzDocPatientItemNum;
			log.info("=======>缴费详情url:{},",url);
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("touser", openid.trim());//
			jsonObject.put("template_id",wxConfig.getMzjf().trim());
			jsonObject.put("url", url);
			log.info("==============缴费明细url:{},",url);
			JSONObject data = new JSONObject();
			JSONObject first = new JSONObject();
			//first.put("value", "(测试阶段，不能就诊，给您带来不便，请谅解)，门诊缴费："+resultMsg);
			first.put("value", "门诊缴费："+resultMsg);
			first.put("color", "#173177");
			JSONObject keyword1 = new JSONObject();
			keyword1.put("value", patient.getUserName());//姓名
			keyword1.put("color", "#173177");
			
			JSONObject keyword2 = new JSONObject();
			keyword2.put("value", patient.getCardNo());//卡号
			keyword2.put("color", "#173177");
			
			JSONObject keyword3 = new JSONObject();
			keyword3.put("value", patient.getIdCardNo());//身份证号
			keyword3.put("color", "#173177");
			
			JSONObject keyword4 = new JSONObject();
			keyword4.put("value", prescriptionMoney+"(元)");//缴费金额
			keyword4.put("color", "#173177");
			
			JSONObject keyword5 = new JSONObject();
			keyword5.put("value",sjh);//收据号
			keyword5.put("color", "#173177");
			
			JSONObject remark = new JSONObject();
			remark.put("value", "就诊时请携带好您的身份证，如有疑问请询问医院相关工作人员！");
			remark.put("color", "#173177");
			
			data.put("first",first);
			data.put("keyword1",keyword1); 
			data.put("keyword2",keyword2);
			data.put("keyword3",keyword3);
			data.put("keyword4",keyword4);
			data.put("keyword5",keyword5);
			data.put("remark",remark);
 
			jsonObject.put("data", data);
			log.info("============>模板消息json:{},", jsonObject.toString().trim());
       String retInfo = HttpRequest.sendPostJson(postUrl, jsonObject.toString().trim());
       log.info("============>门诊缴费，发送消息模板返回{},", retInfo);
//       JSONObject parseObject = JSONObject.parseObject(retInfo);
//       if(!parseObject.getString("errmsg").equals("ok")) {
//    	   //accesstoken失效的话，主动创建accesstoken,再发一次
//    	   accessTokenSchedueld.getAccessToken();
//    	   sendMZJFTemp(json,patient,response, openid);
//       }
      
	} catch (Exception e) {
			 log.info("============>门诊缴费，消息模板发送异常，{}", e);
			e.printStackTrace();
	}
		
	}	
	
	
	
	/**
	 * 预交金充值
	 * @param patient
	 * @param response
	 * @param openid
	 */
	public void sendYjjcz(Patient patient, JSONObject response, String openid) {
		try {
			AccessToken accessToken=wxUserDao.getAccessToken();
			if(accessToken.getAccesstoken().isEmpty()) {//如果acsesstoken为空，则主动更新获取
				log.info("====acsesstoken为空，主动获取=======");
				accessTokenSchedueld.getAccessToken();
				accessToken=wxUserDao.getAccessToken();
			}
			
			String postUrl = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=" + accessToken.getAccesstoken();
			//==============================================
			SimpleDateFormat sdf =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String resultMsg = response.getString("resultCode").equals("1")?"成功":"失败";
			String amount="";
			if(response.getString("resultCode").equals("1")) {
				amount=response.getString("amount");
			}
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("touser", openid.trim());//
			jsonObject.put("template_id",wxConfig.getYjj().trim());
			jsonObject.put("url", wxConfig.getProjectUrl()+"/hospital/person/personCenter");
			
			JSONObject data = new JSONObject();
			JSONObject first = new JSONObject();
			//first.put("value", "(测试阶段，不能就诊，给您带来不便，请谅解)，预交金充值："+resultMsg);
			first.put("value", "预交金充值："+resultMsg);
			first.put("color", "#173177");
			JSONObject keyword1 = new JSONObject();
			keyword1.put("value", patient.getUserName());//姓名
			keyword1.put("color", "#173177");
			
			JSONObject keyword2 = new JSONObject();
			keyword2.put("value", patient.getPatientId());//患者ID
			keyword2.put("color", "#173177");
			
			JSONObject keyword3 = new JSONObject();
			keyword3.put("value", "门诊");//预交金类型
			keyword3.put("color", "#173177");
			
			JSONObject keyword4 = new JSONObject();
			keyword4.put("value", amount+"(元)");//金额
			keyword4.put("color", "#173177");
			
			JSONObject keyword5 = new JSONObject();
			keyword5.put("value",sdf.format(new Date()));//时间
			keyword5.put("color", "#173177");
			
			JSONObject remark = new JSONObject();
			remark.put("value", "点击详情查看余额");
			remark.put("color", "#173177");
			
			data.put("first",first);
			data.put("keyword1",keyword1); 
			data.put("keyword2",keyword2);
			data.put("keyword3",keyword3);
			data.put("keyword4",keyword4);
			data.put("keyword5",keyword5);
			data.put("remark",remark);
 
			jsonObject.put("data", data);
			log.info("============>模板消息json:{},", jsonObject.toString().trim());
       String retInfo = HttpRequest.sendPostJson(postUrl, jsonObject.toString().trim());
       log.info("============>预交金充值，发送消息模板返回{},", retInfo);
//       JSONObject parseObject = JSONObject.parseObject(retInfo);
//       if(!parseObject.getString("errmsg").equals("ok")) {
//    	   //accesstoken失效的话，主动创建accesstoken,再发一次
//    	   accessTokenSchedueld.getAccessToken();
//    	   sendYjjcz(patient,response,openid);
//       }
      
	} catch (Exception e) {
			 log.info("============>预交金充值，消息模板发送异常，{}", e);
			e.printStackTrace();
	}
}
	
	
	/**
	 * 发送住院充值的模板消息
	 * @param patient
	 * @param openid
	 * @param response
	 */
	public void sendZYCZTemp(Patient patient, WeChatOrder wechatOrder, JSONObject response, WeChatUser weChatUser) {
		try {
				AccessToken accessToken=wxUserDao.getAccessToken();
				if(accessToken.getAccesstoken().isEmpty()) {//如果acsesstoken为空，则主动更新获取
					log.info("====acsesstoken为空，主动获取=======");
					accessTokenSchedueld.getAccessToken();
					accessToken=wxUserDao.getAccessToken();
				}
				
				String postUrl = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=" + accessToken.getAccesstoken();
				//==============================================
				SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String resultMsg = response.getString("resultCode").equals("1")?"成功":"失败";
				String amount="";
				if(response.getString("resultCode").equals("1")) {
					amount=response.getJSONObject("resultObjects").getString("Amount");
				}
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("touser", weChatUser.getOpenid().trim());//
				jsonObject.put("template_id",wxConfig.getZycz().trim());
				jsonObject.put("url", wxConfig.getProjectUrl()+"/hospital/zycz/zyInfo?zyh="+wechatOrder.getZyh());
				
				JSONObject data = new JSONObject();
				JSONObject first = new JSONObject();
				//first.put("value", "(测试阶段，不能就诊，给您带来不便，请谅解)，住院充值："+resultMsg);
				first.put("value", "住院充值："+resultMsg);
				first.put("color", "#173177");
				JSONObject keyword1 = new JSONObject();
				keyword1.put("value", wechatOrder.getZyh());//住院号
				keyword1.put("color", "#173177");
				
				JSONObject keyword2 = new JSONObject();
				keyword2.put("value", wechatOrder.getSqBaId());//住院号内部号
				keyword2.put("color", "#173177");
				
				JSONObject keyword3 = new JSONObject();
				keyword3.put("value", amount+"(元)");//金额
				keyword3.put("color", "#173177");
				
				JSONObject keyword4 = new JSONObject();
				keyword4.put("value", sdf.format(new Date()));//操作时间
				keyword4.put("color", "#173177");
				
				JSONObject remark = new JSONObject();
				remark.put("value", "点击详情查看住院信息及详情");
				remark.put("color", "#173177");
				
				data.put("first",first);
				data.put("keyword1",keyword1); 
				data.put("keyword2",keyword2);
				data.put("keyword3",keyword3);
				data.put("keyword4",keyword4);
				data.put("remark",remark);
	 
				jsonObject.put("data", data);
				log.info("============>模板消息json:{},", jsonObject.toString().trim());
	       String retInfo = HttpRequest.sendPostJson(postUrl, jsonObject.toString().trim());
	       log.info("============>住院充值，发送消息模板返回:{},", retInfo);
//	       JSONObject parseObject = JSONObject.parseObject(retInfo);
//	       if(!parseObject.getString("errmsg").equals("ok")) {
//	    	   //accesstoken失效的话，主动创建accesstoken,再发一次
//	    	   accessTokenSchedueld.getAccessToken();
//	    	   sendZYCZTemp(patient,json,response,weChatUser);
//	       }
	       
		  } catch (Exception e) {
				 log.info("============>住院充值充值，消息模板发送异常，{}", e);
				e.printStackTrace();
		}
	  }
		
	
		public static void main(String[] args) {
			Patient patient=new Patient();
			patient.setUserName("吊毛沈文涛");
			patient.setPatientId("66666666");
			try {
				String postUrl = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=" + 
				"33_QE-vdBUCCoY2LBkgUx4_5C0vdliBypnAuwL_NHYATLdwZbNEayMIFevWLUGUcVQrv3TaGYupcdNYvT2OPTuzpnue43LZKhKR-7f29-27w5y-rKtxi9MwpCHOzUOrEc5DZ3RnNIuPftfrNnveYGLjACAKSX";
				//==============================================
				SimpleDateFormat sdf =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String resultMsg ="吊毛沈文涛";
				
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("touser", "oKob-vkVSzKpFgIWlNtDuZIpEOEY");//
				jsonObject.put("template_id","FBP0HCo1lwz83k5ahS7ElVraycvQ2B88cBC2nY359Es");
				jsonObject.put("url", "https://mp.weixin.qq.com/s?__biz=MzIzOTU5MDE1NA==&tempkey=MTA2MV9Gc0JGUElMclp1MU9GbjVvMTY4X3VHSE9VZGlvOEhybjZoMUdsdXNGckVIQTk5OXJXNkp4ejExNkxYaUVNRkozNVZqSFN3aEFUSXMwN21ESVZOZ2FhS0JKN3l0dmxWYVZmWXVVbkdZR2xXMGVyb01LSWZOOFU3WFVzNTItZm43aVhqZkxzV1dDVzlMVG5vRFhOOFNxdmpVZzlPT0VScTRwd2o4VVVnfn4%3D&chksm=69268f1f5e510609c57fb67466b2ad4963715b1620a139d1cc6fb5c94e428aa758ebbed68881&scene=0&xtrack=1&previewkey=m%252BJU%252FxPW%252F9ybpLqhsXQTgMNS9bJajjJKzz%252F0By7ITJA%253D&key=68bdc2143bbbab7a125f45877d40900284458b6d600809f9ef3b6ec891b66ea9faa1db3c19f9dcb0b4b45f706bfad4f39d2d75cc600333e46bf74fa7ff981a62d3f2f5a557d4bf9f7cd9f6b4c91db38d&ascene=1&uin=Mjc1OTM5MDEyMQ%3D%3D&devicetype=Windows+10+x64&version=62090070&lang=zh_CN&exportkey=AQadHJ1HQu6qWP372zIoaLU%3D&pass_ticket=mT4U2F6ytxrhsM0L%2BDlZT5BRtEraFCHqYvp%2BDDqt8ypgLnVGPtb2NZpUNtRZnCgj");
				
				JSONObject data = new JSONObject();
				JSONObject first = new JSONObject();
				//first.put("value", "(测试阶段，不能就诊，给您带来不便，请谅解)，预交金充值："+resultMsg);
				first.put("value", "预交金充值："+resultMsg);
				first.put("color", "#173177");
				JSONObject keyword1 = new JSONObject();
				keyword1.put("value", patient.getUserName());//姓名
				keyword1.put("color", "#173177");
				
				JSONObject keyword2 = new JSONObject();
				keyword2.put("value", patient.getPatientId());//患者ID
				keyword2.put("color", "#173177");
				
				JSONObject keyword3 = new JSONObject();
				keyword3.put("value", "门诊");//预交金类型
				keyword3.put("color", "#173177");
				
				JSONObject keyword4 = new JSONObject();
				keyword4.put("value", "10000(亿元)");//金额
				keyword4.put("color", "#173177");
				
				JSONObject keyword5 = new JSONObject();
				keyword5.put("value",sdf.format(new Date()));//时间
				keyword5.put("color", "#173177");
				
				JSONObject remark = new JSONObject();
				remark.put("value", "点击详情查看余额");
				remark.put("color", "#173177");
				
				data.put("first",first);
				data.put("keyword1",keyword1); 
				data.put("keyword2",keyword2);
				data.put("keyword3",keyword3);
				data.put("keyword4",keyword4);
				data.put("keyword5",keyword5);
				data.put("remark",remark);
	 
				jsonObject.put("data", data);
	            String retInfo = HttpRequest.sendPostJson(postUrl, jsonObject.toString().trim());
	            System.out.println(retInfo);
		    } catch (Exception e) {
				e.printStackTrace();
		}
			
			
		}
	
	
	}

	













