package com.jdm.hospital.perscenter.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jdm.hospital.common.dao.CommonDao;
import com.jdm.hospital.common.domain.HisConfig;
import com.jdm.hospital.common.domain.Patient;
import com.jdm.hospital.common.domain.Recode;
import com.jdm.hospital.common.service.ComService;
import com.jdm.hospital.perscenter.dao.PersonalCenterDao;
import com.jdm.hospital.perscenter.service.PersonalCenterService;
import com.jdm.hospital.utils.HisReqClientUtil;
import com.jdm.hospital.utils.IdcardUtils;
import com.jdm.hospital.utils.WeChatTemplateMsgUtils;
import com.jdm.wechat.common.dao.WxUserDao;
import com.jdm.wechat.common.domain.WeChatUser;

@Service
public class PersonalCenterServiceImpl implements PersonalCenterService {
	
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	@Autowired
	PersonalCenterDao personalCenterDao;
	@Autowired
	HisConfig hisConfig;
	@Autowired
	CommonDao commonDao;
	@Autowired
	WeChatTemplateMsgUtils weChatTemplateMsgUtils;
	@Autowired
	ComService comService;
	@Autowired
	WxUserDao wxUserDao;
	
	/**
	 * 个人信息
	 */
	@Override
	public JSONObject personalInfo(JSONObject json, HttpServletRequest req, HttpServletResponse res) {
		JSONObject jsonResult=new JSONObject();
		JSONObject response=new JSONObject();
		HttpSession session = req.getSession();
		Patient patient = (Patient) session.getAttribute("patient");
		WeChatUser weChatUser = (WeChatUser) session.getAttribute("weChatUser");
		String cardNo = patient.getCardNo();//卡号
		String cardNoType="";//卡类型
		boolean validateCard = IdcardUtils.validateCard(cardNo);//校验是不是身份证
		//卡号的类型 0或空：真实卡号1：M1卡的卡号—内部Id 2：2代身份证内部号
		if(validateCard) {
			cardNoType="2";
		}else {
			cardNoType="0";
		}
		
		try {
        	String request="<xml>" + 
    				"<optioncode>8005</optioncode> " + //请求方法code
    				"<TermNo>"+hisConfig.getAccount()+"</TermNo>" +  //终端号
					"<QuDao>"+hisConfig.getType()+"</QuDao>" + //渠道参数:默认空---询问医院
					"<Pwd>"+hisConfig.getPwd()+"</Pwd>" +//医院内部就诊卡磁卡3道数据(必须填写)银行卡、身份证直接作为一卡通时不需要,M1：6位随机码
    				"  <parameters>" + 
    				"    <CardNo>"+cardNo+"</CardNo> " + //卡号
    				"    <CardNoType>"+cardNoType+"</CardNoType>" + //卡类型
    				"  </parameters>" + 
    				"</xml>";
        	
        	log.info("==============>查询患者信息,请求his入参：{},"+request);
        	response=HisReqClientUtil.getHisResponseCXF(request,hisConfig.getHisUrl());
        	log.info("==============>查询患者信息,请求his返参：{},",response);
        	if(response.getString("resultCode").equals("1")) {
        		log.info("==============>查询患者信息成功,his返回：{},",response);
        		JSONObject  jsonQr=new JSONObject();
        		jsonQr.put("method", 9);
        		
        		JSONObject queryQrCodeInfo = comService.queryQrCodeInfo(json, req, res);
        		if(queryQrCodeInfo.getString("code").equals("0")) {
        			log.info("==============>获取二维码成功：{},",queryQrCodeInfo);
        			response.put("qrCode", queryQrCodeInfo.getString("data"));
        		}else {
        			log.info("==============>获取二维码失败：{},",queryQrCodeInfo);
        			response.put("qrCode", "");
        		}
        		//===============微信头像等信息======
        		response.put("headImgUrl", weChatUser.getHeadImgUrl());
        		response.put("nickname", weChatUser.getNickname());
        		response.put("sex", patient.getSex());
        		response.put("address", weChatUser.getCountry()+weChatUser.getProvince()+weChatUser.getCity());
        		//===============================
				jsonResult.put("code", 0);
				jsonResult.put("msg", response.getString("resultDesc"));
				jsonResult.put("data", response);
				
				
			}else {
				log.info("==============>查询患者信息失败(无患者信息)：{}",response.getString("resultDesc"));
				jsonResult.put("code", 1001);
				jsonResult.put("msg", "查询患者信息失败:"+response.getString("resultDesc"));
				jsonResult.put("data", "");
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.info("==============>查询患者信息失败：{}",response);
			jsonResult.put("code", 1001);
			jsonResult.put("msg", "查询患者信息异常"+e);
		}
		return jsonResult;
	}

	
	/**
	 * 历史操作记录
	 */
	@Override
	public JSONObject queryRecodeList(JSONObject json, HttpServletRequest request, HttpServletResponse response) {
		
		JSONObject jsonResult=null;
		try {
			jsonResult = new JSONObject();
			HttpSession session = request.getSession();
			WeChatUser weChatUser = (WeChatUser) session.getAttribute("weChatUser");
			Patient patient = (Patient) session.getAttribute("patient");
			//查询当前微信用户+当前患者的消费记录
			Map<String,Object> map=new HashMap<String, Object>();
			map.put("wid", weChatUser.getId());
			map.put("pid", patient.getId());
			
			List<Recode> recodeList= personalCenterDao.queryRecodeList(map);
			JSONArray dataArr=new JSONArray();
			for(int i=0;i<recodeList.size();i++) {
				Recode recode = recodeList.get(i);
				JSONObject jsonRecode=new JSONObject();
				jsonRecode.put("num", i+1);//序号
				jsonRecode.put("name", recode.getName());//姓名
				jsonRecode.put("cardNo", recode.getCardNo());//卡号
				jsonRecode.put("idCardNo", recode.getIdCardNo());//身份证号
				jsonRecode.put("orderCode", recode.getOrderCode());//订单号
				jsonRecode.put("bType", bType(recode.getBType()));//业务类型
				jsonRecode.put("amount", recode.getAmount());//金额
				jsonRecode.put("createTime", recode.getCreateTime());//时间
				jsonRecode.put("msg", recode.getMsg());//消息
				jsonRecode.put("status", recode.getStatus());
				dataArr.add(jsonRecode);
			}
			
			jsonResult.put("code", 0);
			jsonResult.put("msg", "获取记录成功");
			jsonResult.put("data", dataArr);
			log.info("==============>获取记录成功：{},",jsonResult);
			
			
		} catch (Exception e) {
			log.info("==============>获取记录异常：{},",e);
			jsonResult.put("code", 0);
			jsonResult.put("msg", "获取记录失败");
			jsonResult.put("data", new JSONArray());
		}
		
		
		return jsonResult;
	}

	
	private String bType(String bType) {
		String msg="未知";
		switch (bType) {
		case "GH":
			msg="挂号";
			break;
		case "YYGH":
			msg="预约挂号";
			break;
		case "MZJF":
			msg="门诊缴费";
			break;
		case "YJJCZ":
			msg="预交金充值";
			break;
		case "ZYCZ":
			msg="住院充值";
			break;
		default:
			break;
		}
		
		return msg;
	}

	/**
	 * 获取就诊码
	 */
	@Override
	public JSONObject queryQRCode(JSONObject json, HttpServletRequest req, HttpServletResponse res) {
		log.info("====================获取就诊码========================");
		JSONObject jsonResult=new JSONObject();
		JSONObject response=new JSONObject();
		HttpSession session = req.getSession();
		WeChatUser weChatUser = (WeChatUser) session.getAttribute("weChatUser");
		//通过wid获取patient信息
		Patient patient=wxUserDao.getPatientByWid(weChatUser.getId());
		
		//测试数据
		//Patient patient=new Patient();
		//patient.setCardNo("X2020020800001");
		log.info("weChatUser:{},patient:{},",weChatUser,patient);
		if(patient==null) {
			log.info("==============>获取就诊码失败，无患者信息=============");
			jsonResult.put("code", 1001);
			jsonResult.put("msg", "获取就诊码失败");
			jsonResult.put("data", new JSONArray());
			return jsonResult;
		}
		
		try {
        	String request="<xml>" + 
    				"<optioncode>8506</optioncode> " + //请求方法code
    				"<TermNo>"+hisConfig.getAccount()+"</TermNo>" +  //终端号
					"<QuDao>"+hisConfig.getType()+"</QuDao>" + //渠道参数:默认空---询问医院
					"<Pwd>"+hisConfig.getPwd()+"</Pwd>" +//医院内部就诊卡磁卡3道数据(必须填写)银行卡、身份证直接作为一卡通时不需要,M1：6位随机码
    				"  <parameters>" + 
    				"    <MyType>1</MyType> " + //卡号
    				"    <MyId>"+patient.getCardNo()+"</MyId> " + //
    				"    <MyValid>10</MyValid> " + //
    				"  </parameters>" + 
    				"</xml>";
        	//3.查询卡余额
        	log.info("==============>获取就诊码，请求his入参：{},"+request);
        	response=HisReqClientUtil.getHisResponseCXF(request,hisConfig.getHisUrl());
        	log.info("==============>获取就诊码，请求his返参：{},",response);
        	if(response.getString("resultCode").equals("1")) {
				jsonResult.put("code", 0);
				jsonResult.put("msg", response.getString("resultDesc"));
				jsonResult.put("cardEwm", response.getString("CardEwm"));
			}else {
				log.info("==============>获取就诊码失败：{}",response.getString("resultDesc"));
				jsonResult.put("code", 1001);
				jsonResult.put("msg", response.getString("resultDesc"));
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.info("==============>获取就诊码异常：{}",response);
			jsonResult.put("code", 1001);
			jsonResult.put("msg", "获取就诊码异常");
		}
		return jsonResult;
	}
	
	
	
}
