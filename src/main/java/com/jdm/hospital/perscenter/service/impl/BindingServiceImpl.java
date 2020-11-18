package com.jdm.hospital.perscenter.service.impl;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.jdm.hospital.common.dao.CommonDao;
import com.jdm.hospital.common.domain.HisConfig;
import com.jdm.hospital.common.domain.Patient;
import com.jdm.hospital.perscenter.controller.BindingController;
import com.jdm.hospital.perscenter.dao.BindingDao;
import com.jdm.hospital.perscenter.service.BindingService;
import com.jdm.hospital.utils.HisReqClientUtil;
import com.jdm.hospital.utils.IdcardUtils;
import com.jdm.hospital.utils.RegUtils;
import com.jdm.hospital.utils.WeChatTemplateMsgUtils;
import com.jdm.wechat.common.domain.WeChatUser;
import net.sf.json.JSONArray;

@Service
public class BindingServiceImpl implements BindingService {
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	@Autowired
	BindingDao bindingDao;
	@Autowired
	HisConfig hisConfig;
	@Autowired
	CommonDao commonDao;
	@Autowired
	WeChatTemplateMsgUtils weChatTemplateMsgUtils;
	@Autowired
	BindingController bc;
	
	
	/**
	 *切换绑定诊疗卡
	 */
	@Override
	@Transactional
	public JSONObject bingDingCardInfo(JSONObject json, HttpServletRequest request, HttpServletResponse response) {
		JSONObject jsonResult=new JSONObject();
		try {
			HttpSession session = request.getSession();
			WeChatUser weChatUser = (WeChatUser) session.getAttribute("weChatUser");
			//测试数据
//			WeChatUser weChatUser =new WeChatUser();
//			weChatUser.setId(1);
			String cardNo=json.getString("cardNo");//卡号
			String isBinding =json.getString("isBinding");//1-绑定 0-未绑定
			
			
			//1.判断session中的WeChatUser是否过期
			if(weChatUser==null) {
				jsonResult.put("code", 1001);
				jsonResult.put("msg", "获取微信信息失败");
				log.info("==============>切换绑定诊疗卡，绑卡session过期：{}",cardNo);
				return jsonResult;
			}
			
			//一.现根据卡号查询该诊疗卡信息
			Patient patient=bindingDao.queryPatientByCardNoAndWid(cardNo,weChatUser.getId());
			if(patient==null) {
				jsonResult.put("code", 1001);
				jsonResult.put("msg", "诊疗卡号错误");
				log.info("==============>诊疗卡号错误,{},",cardNo);
				return jsonResult;
			}
			
			//二，切换绑卡,//1-绑定 0-未绑定
			if(isBinding.equals("1")) {//开启改卡设为默认绑定卡
				//1.解绑所有的卡
				if(bindingDao.untyingAllPatientByWid(weChatUser.getId())<=0) {
					jsonResult.put("code", 1001);
					jsonResult.put("msg", "解绑所有的卡失败");
					log.info("==============>解绑所有的卡失败：{}",cardNo);
					return jsonResult;
				}
				
				//2.将传入的卡号状态改为绑定
				if(bindingDao.updateBindingStausByCardNo(cardNo,isBinding)<=0) {
					jsonResult.put("code", 1001);
					jsonResult.put("msg", "绑卡失败");
					log.info("==============>切换绑定诊疗卡，绑卡失败：{}",cardNo);
					return jsonResult;
				}
				//3.绑卡成功后，将patient存入session
				session.setAttribute("patient", patient);
				jsonResult.put("code", 0);
				jsonResult.put("msg", "绑卡成功");
				log.info("==============>切换绑定诊疗卡，绑卡成功：{}",cardNo);
			}else {//关闭诊疗卡卡
				if(bindingDao.updateBindingStausByCardNo(cardNo,isBinding)>0) {
					session.setAttribute("patient", null);//清空patient
					jsonResult.put("code", 0);
					jsonResult.put("msg", "解绑成功");
					log.info("==============>切换绑定诊疗卡，解绑成功：{}",cardNo);
				}else {
					jsonResult.put("code", 1001);
					jsonResult.put("msg", "解绑失败");
					log.info("==============>切换绑定诊疗卡，解绑失败：{}",cardNo);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			jsonResult.put("code", 1001);
			jsonResult.put("msg", "切换绑定诊疗卡异常");
			log.info("==============>切换绑定诊疗卡异常：{}",e);
		}
		return jsonResult;
	}

	/**
	 * 查询诊疗卡列表
	 */
	@Override
	public JSONObject queryCardList(JSONObject json, HttpServletRequest req, HttpServletResponse res) {
		JSONObject jsonResult=new JSONObject();
		
		try {
			HttpSession session = req.getSession();
			WeChatUser weChatUser = (WeChatUser) session.getAttribute("weChatUser");
			while(weChatUser==null) {
				log.info("==============>获取诊疗卡列表，无微信信息，循环获取微信用户信息=========");
				bc.goBingDing(req, res);
			}
			
			//通过wid获取patientList
			JSONArray ja=new JSONArray();
			List<Patient> patientList=bindingDao.queryPatientList(weChatUser.getId());//
			log.info("==============>诊疗卡列表：{}",patientList);
			if(patientList.size()>0) {
				for(int i=0;i<patientList.size();i++) {
					JSONObject jo=new JSONObject();
					Patient patient = patientList.get(i);
					jo.put("name", patient.getUserName());//姓名
					jo.put("cardNo", patient.getCardNo());//卡号
					jo.put("idCardNo", patient.getIdCardNo());//身份证号
					jo.put("mobile", patient.getMobile());//手机号码
					jo.put("status", patient.getStatus());//1-开启 0-禁用
					jo.put("isBinding", patient.getIsBinding());//1-绑定 0-未绑定
					ja.add(jo);
				}
			}
			jsonResult.put("code", 0);
			jsonResult.put("msg", "获取诊疗卡成功");
			jsonResult.put("data", ja);
			
		} catch (Exception e) {
			e.printStackTrace();
			log.info("==============>查询诊疗卡列表异常：{}",e);
			jsonResult.put("code", 1001);
			jsonResult.put("msg", "查询诊疗卡列表异常"+e);
		}
		return jsonResult;
	}
	
	/**
	 * 绑定新诊疗卡
	 */
	@Override
	public JSONObject bingDingNewCardInfo(JSONObject json, HttpServletRequest req, HttpServletResponse res) {
		JSONObject jsonResult=new JSONObject();
		JSONObject response=new JSONObject();
		HttpSession session = req.getSession();
		WeChatUser weChatUser = (WeChatUser) session.getAttribute("weChatUser");
		String cardNo = json.getString("cardNo");//卡号(只绑定诊疗卡)
		String cardNoType=StringUtils.EMPTY;
		//1.校验姓名和手机号码
		JSONObject jr=validateNameAndMobile(json);
		if(!jr.getString("code").equals("0")) {
			log.info("==============>绑定诊疗卡校验，姓名，手机号失败{},",jr.getString("msg"));
			jsonResult.put("code", 1001);
			jsonResult.put("msg", jr.getString("msg"));
			return jsonResult;
		}
		//2.判断卡类型
		boolean validateCard = IdcardUtils.validateCard(cardNo);
		if(validateCard) {
			cardNoType="2";
		}else {
			cardNoType="0";
		}
		
		try {
        	String request="<xml>" + 
    				"<optioncode>8005</optioncode> " + //请求方法code
    				"<TermNo>"+hisConfig.getAccount()+"</TermNo>" + 
        			"<QuDao>"+hisConfig.getType()+"</QuDao>" + 
        			"<Pwd>"+hisConfig.getPwd()+"</Pwd>" + 
    				"  <parameters>" + 
    				"    <CardNo>"+cardNo+"</CardNo> " + //诊疗卡卡号
    				"    <CardNoType>"+cardNoType+"</CardNoType>" + //卡类型
    				"  </parameters>" + 
    				"</xml>";
        	log.info("==============>绑定新诊疗卡，查询患者信息，请求his入参：{},"+request);
        	response=HisReqClientUtil.getHisResponseCXF(request,hisConfig.getHisUrl());
        	log.info("==============>绑定新诊疗卡，查询患者信息，请求his返参：{},",response); 
        	//如果his中存在用户输入的卡号
        	if(response.getString("resultCode").equals("1")) {
        		//1.先通过该卡号查询数据库中是否存在该卡号
        		Patient patient=bindingDao.queryPatientByCardNoAndWid(cardNo,weChatUser.getId());
        		if(patient==null) {
        			log.info("=====绑定诊疗卡，数据库无患者信息，保持患者信息======");
        			//1.保存该患者信息
        			SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        			String idNo = response.getString("IDNo");//1.身份证号
        			Patient sp=new Patient();
        			sp.setAddress(response.getString("Address"));//地址
        			sp.setCardNo(response.getString("CardNo"));//2.卡号
        			sp.setCreateTime(sdf.format(new Date()));//创建时间
        			sp.setIdCardNo(idNo);//身份证
        			sp.setMobile(response.getString("Phone1")==""?json.getString("mobile"):response.getString("Phone1"));//手机号码
        			sp.setPatientId(response.getString("PatientID")==""?"":response.getString("PatientID"));
        			sp.setSex(response.getString("MySex"));//性别
        			sp.setBirthday(idNo==""?"":IdcardUtils.getBirthByIdCard(idNo));//生日
        			sp.setUserName(response.getString("PatientName")==""?json.getString("name"):response.getString("PatientName"));//姓名           
        			sp.setWid(weChatUser.getId());
        			//2.将该微信号下的其他卡号解绑
        			bindingDao.untyingAllPatientByWid(weChatUser.getId());
        			//4.保存患者信息
        			commonDao.savePatient(sp);
        			//5.通过插入的患者信息返回的主键id，获取患者信息
        			Patient queryPatientByID = commonDao.queryPatientByID(sp.getId());
        			//3.将传入的卡号状态改为绑定
        			if(bindingDao.updateBindingStausByCardNo(queryPatientByID.getCardNo(),"1")<=0) {
        				jsonResult.put("code", 1001);
        				jsonResult.put("msg", "绑卡失败");
        				log.info("==============>切换绑定诊疗卡，绑卡失败：{}",cardNo);
        				return jsonResult;
        			}
        			//6.session中保存患者信息
        			session.setAttribute("patient", queryPatientByID);//将患者信息存入session
        		}
        		
        		
        		
        		jsonResult.put("code", 0);	
        		jsonResult.put("msg", "绑定诊疗卡成功");	
        		jsonResult.put("cardNo", cardNo);	
	    		log.info("==============>绑定新诊疗卡成功,卡号：{},",cardNo);
	         }else {
	        	 //his中找不到，绑卡新诊疗卡失败
	        	 jsonResult.put("code", 1001);	
	        	 jsonResult.put("msg", response.getString("msg"));	
	        	 jsonResult.put("cardNo", cardNo);	
	        	 log.info("==============>"+response.getString("msg")+",卡号：{},",cardNo);
	         }
		} catch (Exception e) {
			e.printStackTrace();
			log.info("==============>绑定新诊疗卡异常：{}",e);
			jsonResult.put("code", 1001);
			jsonResult.put("msg", "绑定新诊疗卡异常"+e);
		}
		
		return jsonResult;
	}
	
	//校验姓名，身份证，手机号码
		private JSONObject validateNameAndMobile(JSONObject json) {
			JSONObject jsonResult=new JSONObject();	
			//1.校验联系人手机号码
			boolean moblie = RegUtils.isMoblie(json.getString("mobile"));
			if(moblie==false) {
				jsonResult.put("code", 1001);
				jsonResult.put("msg", "请输入正确的手机号码!");
				return jsonResult;
			}
			//2.校验联系人姓名
			boolean name = RegUtils.isCHINESE((json.getString("name")));
			if(name==false) {
				jsonResult.put("code", 1001);
				jsonResult.put("msg", "请输入中文姓名!");
				return jsonResult;
			}
			
			jsonResult.put("code", 0);
			jsonResult.put("msg", "校验手机号和身份证成功");
			
			return jsonResult;
			}
		
		
}
