package com.jdm.hospital.common.service.impl;


import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
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

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jdm.hospital.common.dao.CommonDao;
import com.jdm.hospital.common.domain.ContentDO;
import com.jdm.hospital.common.domain.HisConfig;
import com.jdm.hospital.common.domain.Patient;
import com.jdm.hospital.common.domain.Recode;
import com.jdm.hospital.common.service.ComService;
import com.jdm.hospital.perscenter.dao.BindingDao;
import com.jdm.hospital.utils.HisReqClientUtil;
import com.jdm.hospital.utils.IdcardUtils;
import com.jdm.hospital.utils.OrderCodeUtil;
import com.jdm.hospital.utils.RegUtils;
import com.jdm.wechat.common.domain.WeChatUser;
import com.jdm.wechat.common.domain.WxConfig;



@Service
public class ComServiceImpl implements ComService {
	
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	@Autowired
	HisConfig hisConfig;
	@Autowired
	CommonDao commonDao;
	@Autowired
	WxConfig wxConfig;
	@Autowired
	BindingDao bindingDao;
	
	
	
	/**
	 * 查询患者信息，签约建档
	 */
	@Override
	public JSONObject queryPatientAndRegist(HttpServletRequest req, HttpServletResponse res, JSONObject json) {
		JSONObject jsonResult=new JSONObject();
		JSONObject response=new JSONObject();
		String cardNo = json.getString("cardNo");//卡号(身份证号)
		String cardNoType = json.getString("cardNoType");//卡类型
		String ageType = json.getString("ageType");
		
		HttpSession session = req.getSession();
		String path= (String) session.getAttribute("path");//请求的路径
		WeChatUser weChatUser=(WeChatUser) session.getAttribute("weChatUser");
		
		//=========1.校验身份证=================
		if(cardNoType.equals("2")) {
			boolean validateCard = IdcardUtils.validateCard(cardNo);
			if(validateCard==false) {
				jsonResult.put("code", 1001);
				jsonResult.put("msg", "请输入正确的身份证号码");
				return jsonResult;
			}
		}
		try {
			
        	String request="<xml>" + 
    				"<optioncode>8005</optioncode> " + //请求方法code
    				"<TermNo>"+hisConfig.getAccount()+"</TermNo>" + 
        			"<QuDao>"+hisConfig.getType()+"</QuDao>" + 
        			"<Pwd>"+hisConfig.getPwd()+"</Pwd>" + 
    				"  <parameters>" + 
    				"    <CardNo>"+cardNo+"</CardNo> " + //卡号(身份证号码)
    				"    <CardNoType>"+cardNoType+"</CardNoType>" + //卡类型
    				"  </parameters>" + 
    				"</xml>";
        	log.info("================>查询患者信息，请求his入参：{},"+request);
        	response=HisReqClientUtil.getHisResponseCXF(request,hisConfig.getHisUrl());
        	log.info("================>查询患者信息，请求his返参：{},",response);
        	
        	if(response.getString("resultCode").equals("1")) {
        		log.info("================>查询患者信息成功，绑卡======");
        		String idNo = response.getString("IDNo");
        		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        		//通过身份证号查询患者信息成功,直接绑卡
        		Patient patient=new Patient();
        		patient.setAddress(response.getString("Address"));//地址
        		patient.setCardNo(response.getString("CardNo"));//卡号
        		patient.setCreateTime(sdf.format(new Date()));//创建时间
        		patient.setIdCardNo(idNo);//身份证
        		patient.setBirthday(idNo==""?"":IdcardUtils.getBirthByIdCard(idNo));//生日
        		patient.setMobile(response.getString("Phone1")==""?json.getString("mobile"):response.getString("Phone1"));//手机号码
        		patient.setPatientId(response.getString("PatientID")==""?"":response.getString("PatientID"));
        		patient.setSex(response.getString("MySex"));//性别
        		patient.setUserName(response.getString("PatientName")==""?json.getString("name"):response.getString("PatientName"));//姓名        		
        		patient.setWid(weChatUser.getId());
        		patient.setAgeType(ageType); 
        		//1.通过卡号和wid查询数据库是否有绑定的患者信息
        		Patient qpb = commonDao.queryPatientByCardNoAndWid(response.getString("CardNo"),weChatUser.getId());
        		if(qpb==null) {
        			commonDao.savePatient(patient);
        			//3通过插入的患者信息返回的主键id，获取患者信息
        			Patient queryPatientByID = commonDao.queryPatientByID(patient.getId());
        			//4.session中保存患者信息
        			session.setAttribute("patient", queryPatientByID);//将患者信息存入session
        		}else {
        			session.setAttribute("patient", qpb);//将患者信息存入session
        		}
    			//===========================
    			jsonResult.put("code", 0);
        		jsonResult.put("msg", response.getString("resultDesc"));
        		jsonResult.put("url", wxConfig.getProjectUrl()+"/common/index");
        	}else {
        		log.info("================>查询患者信息失败(无患者信息)，调his接口注册：{}",response);
        		//调his注册
        		JSONObject regisHisResult = toRegisHis(req, res,json);
        		if(regisHisResult.getString("code").equals("0")) {
        			log.info("================>调his接口注册成功：{}",regisHisResult);
        			JSONObject savePatientInfo = savePatientInfo(json,regisHisResult,req,res);
        			jsonResult.put("code", 0);
            		jsonResult.put("msg", savePatientInfo.getString("msg"));
            		jsonResult.put("url", savePatientInfo.getString("url"));
        		}else {
        			log.info("================>调his接口注册失败：{}",regisHisResult);
        			jsonResult.put("code", response.getString("resultCode"));
            		jsonResult.put("msg", response.getString("resultDesc"));
            		jsonResult.put("url",wxConfig.getProjectUrl()+path);
        		}
        	}
        } catch (Exception e) {
            e.printStackTrace();
            log.info("================>查询患者信息异常：{},",e);
            jsonResult.put("code", 1001);
    		jsonResult.put("msg", response.getString("resultDesc"));
    		
        }
		return jsonResult;
	}
	
	//注册成功后保存患者信息
	private JSONObject savePatientInfo(JSONObject jsonreq,JSONObject jsonhis, HttpServletRequest req, HttpServletResponse res) throws UnsupportedEncodingException {
		JSONObject jsonResult=new JSONObject();
		String path =StringUtils.EMPTY;
		try {
			log.debug("==============保存患者信息==================");
			//注册成功,1.保存患者信息2.查询患者信息，存到session，3.跳到请求的业务路径下
			SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String idNo = jsonreq.getString("cardNo");//身份证号
			String ageType = jsonreq.getString("ageType");
			String cardNo = jsonhis.getJSONObject("data").getString("CardNo");//卡号
			String patientId = jsonhis.getJSONObject("data").getString("PatientId");//患者ID
			
			HttpSession session = req.getSession();
			path= (String) session.getAttribute("path");//请求的路径
			WeChatUser weChatUser = (WeChatUser) session.getAttribute("weChatUser");//微信用户
			
			Patient patient=new Patient();
			patient.setWid(weChatUser.getId());
			patient.setCardNo(cardNo);//1.卡号
			patient.setIdCardNo(idNo);//2.身份证
			patient.setBirthday(idNo==""?"":IdcardUtils.getBirthByIdCard(idNo));//生日
			patient.setMobile(jsonreq.getString("mobile"));//3.手机
			patient.setPatientId(patientId);//4.患者ID
			patient.setSex(idNo==""?"":IdcardUtils.getGenderByIdCard(idNo));//5.性别
			patient.setUserName(jsonreq.getString("name"));//6.姓名
			patient.setAddress(IdcardUtils.getCountyByIdCard(idNo));//7.住址
			patient.setCreateTime(sdf.format(new Date()));//创建时间
			patient.setAgeType(ageType);
			//1保存用户
			commonDao.savePatient(patient);
			//2通过插入的患者信息返回的主键id，获取患者信息
			Patient pt=commonDao.queryPatientByID(patient.getId());
			//3.session中保存患者信息
			session.setAttribute("patient", pt);
			//4.跳转到业务路径
			jsonResult.put("code", 0);
    		jsonResult.put("msg", "注册成功");
    		jsonResult.put("url", wxConfig.getProjectUrl()+"common/index");
		} catch (Exception e) {
			e.printStackTrace();
			log.error("================>注册，保存患者信息异常：{},",jsonhis);
			jsonResult.put("code", 1001);
    		jsonResult.put("msg", "注册异常");
    		jsonResult.put("url", wxConfig.getProjectUrl()+"common/index");
		}
		return jsonResult;
		
	}


	/**
	 * 注册签约
	 */
	@Override
	public JSONObject toRegisHis(HttpServletRequest req, HttpServletResponse res, JSONObject json) {

		JSONObject jsonResult=new JSONObject();
		JSONObject response=new JSONObject();
		
		//=========1.校验身份证，姓名，手机号=================
		JSONObject jr=validateNameAndIDNoAndMobile(json);
		if(!jr.getString("code").equals("0")) {
			log.info("================>注册校验身份证，姓名，手机号失败{},",jr.getString("msg"));
			jsonResult.put("code", 1001);
			jsonResult.put("msg", jr.getString("msg"));
			return jsonResult;
		}
		
		try {
			String idNo = json.getString("cardNo");//身份证号码
			String mobile = json.getString("mobile");//手机号码
        	String request="<xml>" + 
        				"<optioncode>8002</optioncode>" + 
	        			"<TermNo>"+hisConfig.getAccount()+"</TermNo>" + 
	        			"<QuDao>"+hisConfig.getType()+"</QuDao>" + 
	        			"<Pwd>"+hisConfig.getPwd()+"</Pwd>" + 
	        			"<parameters>" + 
		        			"<PatientName>"+json.getString("name")+"</PatientName>" + //姓名
		        			"<SexFlag>"+IdcardUtils.getNumberGenderByIdCard(idNo)+"</SexFlag>" + //1：男;2：女
		        			"<BirthDay>"+IdcardUtils.getBirthByIdCard(idNo)+"</BirthDay>" + //生日
		        			"<IDNo>"+idNo+"</IDNo>" + //身份证号码
		        			"<IDType/>" + 
		        			"<CardNo>"+idNo+"</CardNo>" + //卡号
		        			"<Nation></Nation>" + //民族
		        			"<Phone>"+mobile+"</Phone>" + //手机
		        			"<Address>"+IdcardUtils.getCountyByIdCard(idNo)+"</Address>" + //地址
		        			"<CardNoType>2</CardNoType>" + //卡类型1：M1卡的卡号—内部Id2：2代身份证内部号3：虚拟建卡
		        			"<Amount>0</Amount>" + //充值金额 单位：元
		        			"<PayWay>4</PayWay>" + //付款方式
		        			"<BankCardNo/>" + 
		        			"<SerialNumber>"+OrderCodeUtil.createOrderCode()+"</SerialNumber>" + //银行交易唯一流水号
		        			"<DingDanHao/>" + //订单号
		        			"<HelpCode/>" + //病人姓名助记符
	        			"</parameters>" + 
        			"</xml>";
        	log.info("================>注册签约，请求his入参:{},",request);
        	response=HisReqClientUtil.getHisResponseCXF(request.trim(),hisConfig.getHisUrl());
        	log.info("================>注册签约，his返回：{},",response);
        	if(response.getString("resultCode").equals("1")) {
        		jsonResult.put("code", 0);
        		jsonResult.put("msg", response.getString("resultDesc"));
        		jsonResult.put("data", response.getJSONObject("resultObjects"));
        		
        	}else {
        		jsonResult.put("code", response.getString("resultCode"));
        		jsonResult.put("msg", response.getString("resultDesc"));
        		jsonResult.put("data", new JSONArray());
        	}
        } catch (Exception e) {
            e.printStackTrace();
            log.info("================>注册异常：{},",e);
            jsonResult.put("code", response.getString("resultCode"));
    		jsonResult.put("msg", response.getString("resultDesc"));
        }
		
		return jsonResult;
	}
	
	
	//校验姓名，身份证，手机号码
	private JSONObject validateNameAndIDNoAndMobile(JSONObject json) {
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
		//3.校验身份证
		String idno = json.getString("cardNo");
		boolean validateCard = IdcardUtils.validateCard(idno);
		if(validateCard==false) {
			jsonResult.put("code", 1001);
			jsonResult.put("msg", "请输入正确的身份证号码!");
			return jsonResult;
		}
		jsonResult.put("code", 0);
		jsonResult.put("msg", "校验手机号和身份证成功");
		
		return jsonResult;
		}
		
	/**
	 * 绑定卡信息（有卡患者添加诊疗卡）
	 */
	@Override
	public JSONObject toBindingInfo(HttpServletRequest req, HttpServletResponse res, JSONObject json) {
			JSONObject jsonResult=new JSONObject();	
			JSONObject response=new JSONObject();	
			
			String name=json.getString("name");//姓名
			String cardNoType = json.getString("cardNoType");//卡类型
			String cardNo = json.getString("cardNo");//卡号
			String ageType = json.getString("ageType");//年龄类型
			
			
			if(cardNoType.equals("2")) {
				boolean validateCard = IdcardUtils.validateCard(cardNo);
				if(validateCard==false) {
					jsonResult.put("code", 1001);
					jsonResult.put("msg", "请输入正确的身份证号码");
					return jsonResult;
				}
			}
			
			HttpSession session = req.getSession();
			WeChatUser weChatUser = (WeChatUser) session.getAttribute("weChatUser");//微信用户
			
			
			//2.调his接口通过传入的卡号查询患者信息
			try {
				String request="<xml>" + 
						"<optioncode>8005</optioncode> " + //请求方法code
						"<TermNo>"+hisConfig.getAccount()+"</TermNo>" + 
	        			"<QuDao>"+hisConfig.getType()+"</QuDao>" + 
	        			"<Pwd>"+hisConfig.getPwd()+"</Pwd>" + 
						"  <parameters>" + 
						"    <CardNo>"+cardNo+"</CardNo> " + //卡号(身份证号码)
						"    <CardNoType>"+cardNoType+"</CardNoType>" + //卡类型
						"  </parameters>" + 
						"</xml>";
				log.info("================>绑卡，查询患者信息，请求his入参：{},"+request);
	        	response=HisReqClientUtil.getHisResponseCXF(request,hisConfig.getHisUrl());
	        	//String sssss="{\"Address\":\"湖南省湘西土家族苗族自治州永顺县\",\"SqCardOperNum4Yj\":\"3\",\"resultCode\":\"1\",\"Birthday\":\"1988-08-10\",\"PatientName\":\"鲁智深\",\"resultDesc\":\"成功\",\"MySex\":\"男\",\"RemainMoney\":\"106.30\",\"CardNo\":\"E43312719880810801X\",\"Phone1\":\"17680169321\",\"PatientID\":\"2020010400001\",\"resultObjects\":[],\"RandomNum\":[],\"IDNo\":\"43312719880810801X\"}";
				//response=JSONObject.parseObject(sssss);
	        	
	        	log.info("================>绑卡，查询患者信息，请求his返参：{},",response);
	        	
				if(response.getString("resultCode").equals("1")) {
					log.info("================>绑卡前查询his患者信息成功,再绑定患者信息=====");
					String idNo = response.getString("IDNo");//身份证号码
					
					String patientName = response.getString("PatientName");//患者姓名
					SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					
					if(!patientName.equals(name)) {
						jsonResult.put("code", 1001);
						jsonResult.put("msg", "输入信息不符");
						return jsonResult;
					}
					
					Patient patient=new Patient();
					patient.setWid(weChatUser.getId());
					patient.setCardNo(response.getString("CardNo"));//1.卡号
					patient.setIdCardNo(idNo);//2.身份证
					patient.setBirthday(idNo==""?"":IdcardUtils.getBirthByIdCard(idNo));//生日
					patient.setMobile(response.getString("Phone1"));//3.手机
					patient.setPatientId(response.getString("PatientID"));//4.患者ID
					patient.setSex(response.getString("MySex"));//5.性别
					patient.setUserName(response.getString("PatientName"));//6.姓名
					patient.setAddress(response.getString("Address"));//7.住址					
					patient.setCreateTime(sdf.format(new Date()));//创建时间
					patient.setAgeType(ageType);//年龄类型
					//1.查询数据库中有无该患者信息,卡号+微信id
					List<Patient> qpbcaw = commonDao.queryPatientListByidCardAndWid(response.getString("CardNo"), weChatUser.getId());
					log.info("================>查询数据库中患者信息：",qpbcaw);
					if(qpbcaw.size()==0) {
						log.info("================>数据库中无患者信息，保存患者信息");
						
						//1.解绑改微信号下的所有卡
						if(bindingDao.untyingAllPatientByWid(weChatUser.getId())<=0) {
							jsonResult.put("code", 1001);
							jsonResult.put("msg", "绑卡失败");
							log.info("================>有卡用户，解绑该微信用户下的所有卡失败：{}",cardNo);
							return jsonResult;
						}
						//2.保存用户
						commonDao.savePatient(patient);
						//3通过插入的患者信息返回的主键id，获取患者信息
						Patient pt=commonDao.queryPatientByID(patient.getId());
						//4.session中保存患者信息
						session.setAttribute("patient", pt);
					}
					
					jsonResult.put("code", 0);
					jsonResult.put("msg", "绑卡成功");
					
					
				}else {
					log.info("================>绑卡(有卡用户)查询患者信息失败：{}",response);
					jsonResult.put("code", 1001);
					jsonResult.put("msg", "绑卡失败");
				}
			} catch (Exception e) {
				e.printStackTrace();
				log.info("==========绑卡查询患者信息异常：{}",response);
				jsonResult.put("code", 1001);
				jsonResult.put("msg", "绑卡异常"+e);
			}
			return jsonResult;
		}
		
		
		/**
		 * 预交金充值
		 */
		@Override
		public JSONObject toRecharge(HttpServletRequest req, HttpServletResponse res, JSONObject json) {
			JSONObject jsonResult=new JSONObject();	
			JSONObject response=new JSONObject();
			HttpSession session = req.getSession();
			Patient patient = (Patient) session.getAttribute("patient");
			String orderCode = OrderCodeUtil.createOrderCode();
			
			
			try {
				String request="<xml>" + 
						"<optioncode>10004</optioncode> " + //请求方法code,11004-支付宝，10004-微信
						"<TermNo>"+hisConfig.getAccount()+"</TermNo>" +  //终端号
						"<QuDao>"+hisConfig.getType()+"</QuDao>" + //渠道参数:默认空---询问医院
						"<Pwd>"+hisConfig.getPwd()+"</Pwd>" +//医院内部就诊卡磁卡3道数据(必须填写)银行卡、身份证直接作为一卡通时不需要,M1：6位随机码
						"<parameters>" + 
							"<CardNo>"+patient.getCardNo()+"</CardNo> " + //卡号(身份证号码)
							"<IDType/>" + //卡类型
							"<Amount>"+json.getString("amount")+"</Amount> " + //金额（元）
							"<PayWay>2</PayWay> " + //付款方式 1：现金;2：非现金  
							"<BankCardNo/>" + //银行卡号
							"<SerialNumber>"+orderCode+"</SerialNumber> " + //流水号
							"<DingDanHao>"+orderCode+"</DingDanHao> " + //订单号
						"</parameters>" + 
						"</xml>";
				log.info("================>预交金充值，请求his入参：{},"+request);
	        	response=HisReqClientUtil.getHisResponseCXF(request,hisConfig.getHisUrl());
	        	log.info("================>预交金充值，请求his返参：{},",response);
				if(response.getString("resultCode").equals("1")) {
					log.info("================>预交金充值成功,his返回：{},",response);
					jsonResult.put("code", 0);
					jsonResult.put("msg", response.getString("resultDesc"));
					jsonResult.put("data", response);
					jsonResult.put("orderCode", orderCode);
				}else {
					log.info("================>预交金充值失败：{}",response.getString("resultDesc"));
					jsonResult.put("code", 1001);
					jsonResult.put("msg", "预交金充值失败:"+response.getString("resultDesc"));
					jsonResult.put("data", new JSONArray());
					jsonResult.put("orderCode", orderCode);
				}
			} catch (Exception e) {
				e.printStackTrace();
				log.info("================>预交金充值失败：{}",response);
				jsonResult.put("code", 1001);
				jsonResult.put("msg", "预交金充值异常"+e);
			}finally {
				//1.保存金额，操作类型等信息，方便对账
				SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				Recode rc=new Recode();
				rc.setAmount(new BigDecimal(json.getString("amount")));//充值金额
				rc.setCardNo(patient.getCardNo());//卡号
				rc.setBType("YJJCZ");//业务类型
				rc.setCreateTime(sdf.format(new Date()));//创建时间
				rc.setIdCardNo(patient.getIdCardNo());//身份证号码
				rc.setName(patient.getUserName());//姓名
				rc.setPid(patient.getId());//pid
				rc.setWid(patient.getWid());//wid
				
				log.info("================>预交金充值：{},",rc);
				commonDao.saveRecode(rc);
			}
			return jsonResult;
		}
		
		
		

		/**
		 * 查询患者信息
		 */
		@Override
		public JSONObject queryPatientOnly(HttpServletRequest req, HttpServletResponse res, JSONObject json) {
			JSONObject jsonResult=new JSONObject();
			JSONObject response=new JSONObject();
			String cardNo = json.getString("cardNo");//卡号
			String cardNoType = json.getString("cardNoType");//卡类型
			//=========1.校验身份证，姓名，手机号=================
			if(cardNoType.equals("2")) {
				boolean validateCard = IdcardUtils.validateCard(cardNo);
				if(validateCard==false) {
					jsonResult.put("code", 1001);
					jsonResult.put("msg", "请输入正确的身份证号码");
					return jsonResult;
				}
			}
			
			try {
	        	String request="<xml>" + 
	    				"<optioncode>8005</optioncode> " + //请求方法code
	    				"<TermNo>"+hisConfig.getAccount()+"</TermNo>" +  //终端号
						"<QuDao>"+hisConfig.getType()+"</QuDao>" + //渠道参数:默认空---询问医院
						"<Pwd>"+hisConfig.getPwd()+"</Pwd>" +//医院内部就诊卡磁卡3道数据(必须填写)银行卡、身份证直接作为一卡通时不需要,M1：6位随机码
	    				"  <parameters>" + 
	    				"    <CardNo>"+cardNo+"</CardNo> " + //卡号(身份证号码)
	    				"    <CardNoType>"+cardNoType+"</CardNoType>" + //卡类型
	    				"  </parameters>" + 
	    				"</xml>";
	        	//3.先根据用户的信息查询his有误记录
	        	log.info("================>查询患者信息,请求his入参：{},"+request);
	        	response=HisReqClientUtil.getHisResponseCXF(request,hisConfig.getHisUrl());
	        	log.info("================>查询患者信息,请求his返参：{},",response);
	        	if(response.getString("resultCode").equals("1")) {
	        		log.info("================>查询患者信息成功,his返回：{},",response);
					jsonResult.put("code", 0);
					jsonResult.put("msg", response.getString("resultDesc"));
					jsonResult.put("data", response);
				}else {
					log.info("================>查询患者信息失败(无患者信息)：{}",response.getString("resultDesc"));
					jsonResult.put("code", 1001);
					jsonResult.put("msg", "查询患者信息失败:"+response.getString("resultDesc"));
					jsonResult.put("data", new JSONArray());
				}
			} catch (Exception e) {
				e.printStackTrace();
				log.info("================>查询患者信息失败：{}",response);
				jsonResult.put("code", 1001);
				jsonResult.put("msg", "查询患者信息异常"+e);
			}
			return jsonResult;
	    }
		
		/**
		 * 查询卡余额
		 */
		@Override
		public JSONObject queryBalance(HttpServletRequest req, HttpServletResponse res) {
			JSONObject jsonResult=new JSONObject();
			JSONObject response=new JSONObject();
			//String cardNo = json.getString("cardNo");
			HttpSession session = req.getSession();
			Patient patient=(Patient) session.getAttribute("patient");
			//测试数据
			//Patient patient=new Patient();
			//patient.setCardNo("X2020020800001");
			try {
	        	String request="<xml>" + 
	    				"<optioncode>8015</optioncode> " + //请求方法code
	    				"<TermNo>"+hisConfig.getAccount()+"</TermNo>" +  //终端号
						"<QuDao>"+hisConfig.getType()+"</QuDao>" + //渠道参数:默认空---询问医院
						"<Pwd>"+hisConfig.getPwd()+"</Pwd>" +//医院内部就诊卡磁卡3道数据(必须填写)银行卡、身份证直接作为一卡通时不需要,M1：6位随机码
	    				"  <parameters>" + 
	    				"    <CardNo>"+patient.getCardNo()+"</CardNo> " + //卡号
	    				"  </parameters>" + 
	    				"</xml>";
	        	//3.查询卡余额
	        	log.info("================>请求his入参：{},"+request);
	        	response=HisReqClientUtil.getHisResponseCXF(request,hisConfig.getHisUrl());
	        	log.info("================>请求his返参：{},",response);
	        	if(response.getString("resultCode").equals("1")) {
	        		log.info("================>查询诊疗卡余额成功,his返回：{},",response);
					jsonResult.put("code", 0);
					jsonResult.put("msg", response.getString("resultDesc"));
					jsonResult.put("data", response);
				}else {
					log.info("================>查询诊疗卡余额失败：{}",response.getString("resultDesc"));
					jsonResult.put("code", 1001);
					jsonResult.put("msg", "查询诊疗卡余额失败:"+response.getString("resultDesc"));
					jsonResult.put("data", new JSONArray());
				}
			} catch (Exception e) {
				e.printStackTrace();
				log.info("================>查询诊疗卡余额失败：{}",response);
				jsonResult.put("code", 1001);
				jsonResult.put("msg", "查询诊疗卡余额异常"+e);
			}
			return jsonResult;
		}

		@Override
		public JSONObject queryAllNews(HttpServletRequest request, HttpServletResponse response) {
			JSONObject jsonResult=new JSONObject();
			
			try {
				List<ContentDO>  contentList = commonDao.queryAllNews();
				JSONArray ja=new JSONArray();
				for(int i=0;i<contentList.size();i++) {
					JSONObject jo=new JSONObject();
					ContentDO content = contentList.get(i);
					jo.put("titel", content.getTitle());//文章标题
					jo.put("creatTime", content.getGtmCreate());//创建时间
					jo.put("author", content.getAuthor());//作者
					jo.put("cid", content.getCid());//文章的id
					jo.put("browse_num", content.getBrowseNum());//文章的id
					ja.add(jo);
				}
				
				jsonResult.put("data", ja);
				jsonResult.put("code", 0);
				jsonResult.put("msg", "获取新闻列表成功");
				log.info("================>获取新闻列表成功：{},",contentList);
			} catch (Exception e) {
				e.printStackTrace();
				jsonResult.put("data", new JSONArray());
				jsonResult.put("code", 1001);
				jsonResult.put("msg", "获取新闻列表失败");
				log.info("================>获取新闻列表失败========================");
			}
			return jsonResult;
		}
		
		
		/**
		 * 获取新闻的详情
		 */
		@Override
		public JSONObject queryNewsDetailById(JSONObject json,HttpServletRequest request, HttpServletResponse response) {
			JSONObject jsonResult=new JSONObject();
			SimpleDateFormat sdf =new SimpleDateFormat("yyyy-MM-dd");
			try {
				log.info("========获取新闻详情：{},",json);
				ContentDO bContentDO = commonDao.getOneNews(json.getString("cid"));
				commonDao.updateNewsbrowseNum(json.getString("cid"));
				jsonResult.put("code", 0);
				jsonResult.put("msg", "成功");
				JSONObject jsonData=new JSONObject(); 
				jsonData.put("title", bContentDO.getTitle());//标题
				jsonData.put("author", bContentDO.getAuthor());//作者
				jsonData.put("creatTime", bContentDO.getGtmCreate());//创建时间
				jsonData.put("browse_num", bContentDO.getBrowseNum());//浏览量
				jsonData.put("content", bContentDO.getContent());//内容
				jsonResult.put("data", jsonData);
				//log.info("========新闻详情：{},",bContentDO);
			} catch (Exception e) {
				e.printStackTrace();
				jsonResult.put("code", 1001);
				jsonResult.put("msg", "成功");
				jsonResult.put("data", "");
				log.info("================>获取新闻异常：{},",e);
			}
			return jsonResult;
		}
		
		
		/**
		 * 获取二维码信息
		 */
		@Override
		public JSONObject queryQrCodeInfo(JSONObject json, HttpServletRequest req, HttpServletResponse res) {
			JSONObject jsonResult=new JSONObject();
			JSONObject response=new JSONObject();
			//String cardNo = json.getString("cardNo");
			HttpSession session = req.getSession();
			Patient patient=(Patient) session.getAttribute("patient");
			//测试数据
			//Patient patient=new Patient();
			//patient.setCardNo("X2020020800001");
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
	        	log.info("================>获取二维码信息，请求his入参：{},"+request);
	        	response=HisReqClientUtil.getHisResponseCXF(request,hisConfig.getHisUrl());
	        	log.info("================>获取二维码，请求his返参：{},",response);
	        	if(response.getString("resultCode").equals("1")) {
	        		log.info("================>获取二维码，成功,his返回：{},",response);
					jsonResult.put("code", 0);
					jsonResult.put("msg", response.getString("resultDesc"));
					jsonResult.put("data", response.getString("CardEwm"));
				}else {
					log.info("================>获取二维码，失败：{}",response.getString("resultDesc"));
					jsonResult.put("code", 1001);
					jsonResult.put("msg", response.getString("resultDesc"));
					jsonResult.put("data", new JSONArray());
				}
			} catch (Exception e) {
				e.printStackTrace();
				log.info("================>获取二维码，失败：{}",response);
				jsonResult.put("code", 1001);
				jsonResult.put("msg", e);
			}
			return jsonResult;
		}
		
		/**
		   * 获取充值记录
		 */
		@Override
		public JSONObject queryRechargeResult(JSONObject json, HttpServletRequest req,HttpServletResponse res) {
			JSONObject jsonResult=new JSONObject();
			JSONObject response=new JSONObject();
			String orderCode = json.getString("orderCode");//订单号
			try {
	        	
				Recode recode=commonDao.queryRecode(orderCode);
				if(recode==null||(!recode.getStatus().equals("1"))) {
					jsonResult.put("code", 1001);
					jsonResult.put("msg", "查询充值记录失败");
					return jsonResult;
				}
				
				jsonResult.put("code", 0);
				jsonResult.put("msg", "查询充值记录成功");
			} catch (Exception e) {
				e.printStackTrace();
				log.info("================>查询充值记录异常：{}",response);
				jsonResult.put("code", 1001);
				jsonResult.put("msg", "查询充值记录失败");
			}
			
			return jsonResult;
		}
		public static void main(String[] args) {
			String idNo="43312719880810801X";
			Patient patient=new Patient();
			
			patient.setIdCardNo(idNo);//2.身份证
			patient.setBirthday(idNo==""?"":IdcardUtils.getBirthByIdCard(idNo));//生日
			patient.setSex(idNo==""?"":IdcardUtils.getGenderByIdCard(idNo));//5.性别
			System.out.println(patient.getSex()+"--"+patient.getBirthday());
			
			
			
			
			
			
		}
}
