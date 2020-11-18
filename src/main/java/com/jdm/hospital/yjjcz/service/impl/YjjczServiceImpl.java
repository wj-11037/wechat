package com.jdm.hospital.yjjcz.service.impl;

import java.math.BigDecimal;
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
import com.jdm.hospital.common.dao.CommonDao;
import com.jdm.hospital.common.domain.HisConfig;
import com.jdm.hospital.common.domain.Patient;
import com.jdm.hospital.common.domain.Recode;
import com.jdm.hospital.common.domain.WeChatOrder;
import com.jdm.hospital.gh.dao.GHDao;
import com.jdm.hospital.gh.domain.GHInfo;
import com.jdm.hospital.utils.HisReqClientUtil;
import com.jdm.hospital.utils.OrderCodeUtil;
import com.jdm.hospital.utils.WeChatTemplateMsgUtils;
import com.jdm.hospital.yjjcz.dao.YjjczDao;
import com.jdm.hospital.yjjcz.service.YjjczService;
import com.jdm.wechat.common.dao.WxUserDao;
import com.jdm.wechat.common.domain.WeChatUser;
import com.jdm.wechat.common.domain.WxConfig;
import com.jdm.wechat.pay.service.PayNotifyService;


@Service
public class YjjczServiceImpl implements YjjczService {
	
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	@Autowired
	HisConfig hisConfig;
	@Autowired
	CommonDao commonDao;
	@Autowired
	WeChatTemplateMsgUtils weChatTemplateMsgUtils;
	@Autowired
	YjjczDao yjjczDao;
	@Autowired
	WxUserDao wxUserDao;
	@Autowired
	WxConfig wxConfig;
	@Autowired
	GHDao ghdao;
	@Autowired
	PayNotifyService payNotifyService;
	
	
	/**
	 * 预交金充值
	 */
	@Override
	public JSONObject toRecharge(HttpServletRequest req, HttpServletResponse res, JSONObject json) {
		JSONObject jsonResult=new JSONObject();	
		JSONObject response=new JSONObject();
		HttpSession session = req.getSession();
		Patient patient = (Patient) session.getAttribute("patient");
		WeChatUser weChatUser = (WeChatUser) session.getAttribute("weChatUser");
		String orderCode = json.getString("orderCode");//订单号
		//==============================测试数据==========================
		//Patient patient=new Patient();
		//patient.setCardNo("X2020020800001");
		//WeChatUser weChatUser =new WeChatUser();
		//weChatUser.setId(1);
		//weChatUser.setOpenid("oKob-vhp-Nlbl8kpdZGnAIegaXZc");
		
		log.info("==========预交金充值入参：{},微信用户信息:{},患者信息：{}，",json,weChatUser,patient);
		//1.先查询该订单号是否存在，存在则直接返回
		Recode recode=commonDao.queryRecodeByOrderCode(weChatUser.getId(),orderCode);
		if(recode!=null) {
			jsonResult.put("code", 0);
			jsonResult.put("msg",recode.getStatus().equals("1")?"成功":"失败");
			jsonResult.put("data", "");
			jsonResult.put("orderCode", orderCode);
			log.info("==========预交金充值，该订单已支付，直接返回：{},",recode);
			return jsonResult;
		}
		
		//测试数据
		//Patient patient =new Patient();
		//patient.setCardNo("X2020020800001");
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
			log.info("============预交金充值,请求his入参：{},"+request);
        	response=HisReqClientUtil.getHisResponseCXF(request,hisConfig.getHisUrl());
        	log.info("============预交金充值,请求his返参：{},",response);
			if(response.getString("resultCode").equals("1")) {
				log.info("==========预交金充值成功,his返回：{},",response);
				//1.发送模板消息
				response.put("amount", json.getString("amount"));
				weChatTemplateMsgUtils.sendYjjcz(patient, response, weChatUser.getOpenid());
				jsonResult.put("code", 0);
				jsonResult.put("msg", response.getString("resultDesc"));
				jsonResult.put("data", response);
				jsonResult.put("orderCode", orderCode);
			}else {
				log.info("==========预交金充值失败：{}",response.getString("resultDesc"));
				jsonResult.put("code", 1001);
				jsonResult.put("msg", "预交金充值失败:"+response.getString("resultDesc"));
				jsonResult.put("data", "");
				jsonResult.put("orderCode", orderCode);
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.info("==========预交金充值失败：{}",response);
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
			rc.setOrderCode(orderCode);//订单号
			rc.setStatus(response.getString("resultCode"));
			rc.setMsg(response.getString("resultDesc"));
			log.info("====>预交金充值：{},",rc);
			commonDao.saveRecode(rc);
		}
		return jsonResult;
	}
	
	
	
	@Override
	public void saveWeChatOrder(JSONObject json, Patient patient, WeChatUser weChatUser) {
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		WeChatOrder wo=new WeChatOrder();
		wo.setPid(patient.getId());
		wo.setWid(weChatUser.getId());
		wo.setAmount(Double.parseDouble(json.getString("amount")));
		wo.setBtype(json.getString("type"));
		wo.setCardNo(patient.getCardNo());
		wo.setUpdateTime(sdf.format(new Date()));
		wo.setIdCardNo(patient.getIdCardNo());
		wo.setName(patient.getUserName());
		wo.setOrderStatus("0");
		wo.setFlowNo("");
		wo.setOrderCode(json.getString("orderCode"));
		wo.setSqBaId(json.getString("sqBaId"));
		wo.setZyh(json.getString("zyh"));
		log.info("============>保存微信充值订单数据：{},",wo);
		//1.根据订单号更新数据
		WeChatOrder wechatOrder=payNotifyService.queryWechatOrderByOrderNo(json.getString("orderCode"));
		if(wechatOrder!=null) {
			yjjczDao.updateWechatOrderInfo(wo);
		}else {//2.订单号不存在的就保存数据
			wo.setCreateTime(sdf.format(new Date()));
			yjjczDao.saveWeChatOrder(wo);
		}
		
	}
	@Override
	public WeChatOrder queryWeChatOrderByOrderNoAndWid(String orderCode, Integer wid) {
		
		return yjjczDao.queryWeChatOrderByOrderNoAndWid(orderCode,wid);
	}
	
	
	@Override
	public int updateOrderStatusByOrderNoAndWid(String orderCode, Integer wid,String updateTime,String transaction_id) {
		return yjjczDao.updateOrderStatusByOrderNoAndWid(orderCode,wid,updateTime,transaction_id);
	}
	
	
	
	@Override
	public JSONObject notifyToRecharge(WeChatOrder wechatOrder) {
		JSONObject jsonResult=new JSONObject();	
		JSONObject response=new JSONObject();
		WeChatUser weChatUser = wxUserDao.queryWechatUserInfoById(wechatOrder.getWid());
		Patient patient = wxUserDao.getPatientByWid(wechatOrder.getWid());
		//log.info("===================微信支付回调预交金充值，微信用户信息：{},患者信息：{},",weChatUser,patient);
		
		//1.先查询该订单号是否存在，存在则直接返回
		Recode recode=commonDao.queryRecodeByOrderCode(weChatUser.getId(),wechatOrder.getOrderCode());
		if(recode!=null) {
			jsonResult.put("code", 0);
			jsonResult.put("msg",recode.getStatus().equals("1")?"成功":"失败");
			jsonResult.put("data", "");
			jsonResult.put("orderCode", wechatOrder.getOrderCode());
			log.info("================>预交金充值，该订单已支付，直接返回：{},",recode);
			return jsonResult;
		}
		
		//测试数据
		//Patient patient =new Patient();
		//patient.setCardNo("X2020020800001");
		try {
			String request="<xml>" + 
					"<optioncode>10004</optioncode> " + //请求方法code,11004-支付宝，10004-微信
					"<TermNo>"+hisConfig.getAccount()+"</TermNo>" +  //终端号
					"<QuDao>"+hisConfig.getType()+"</QuDao>" + //渠道参数:默认空---询问医院
					"<Pwd>"+hisConfig.getPwd()+"</Pwd>" +//医院内部就诊卡磁卡3道数据(必须填写)银行卡、身份证直接作为一卡通时不需要,M1：6位随机码
					"<parameters>" + 
						"<CardNo>"+patient.getCardNo()+"</CardNo> " + //卡号(身份证号码)
						"<IDType/>" + //卡类型
						"<Amount>"+wechatOrder.getAmount()+"</Amount> " + //金额（元）
						"<PayWay>2</PayWay> " + //付款方式 1：现金;2：非现金  
						"<BankCardNo/>" + //银行卡号
						"<SerialNumber>"+wechatOrder.getOrderCode()+"</SerialNumber> " + //流水号
						"<DingDanHao>"+wechatOrder.getOrderCode()+"</DingDanHao> " + //订单号
					"</parameters>" + 
					"</xml>";
			log.info("================>预交金充值,请求his入参：{},"+request);
        	response=HisReqClientUtil.getHisResponseCXF(request,hisConfig.getHisUrl());
        	log.info("================>预交金充值,请求his返参：{},",response);
			if(response.getString("resultCode").equals("1")) {
				log.info("================>预交金充值成功,his返回：{},",response);
				//1.发送模板消息
				response.put("amount", wechatOrder.getAmount());
				weChatTemplateMsgUtils.sendYjjcz(patient, response, weChatUser.getOpenid());
				jsonResult.put("code", 0);
				jsonResult.put("msg", response.getString("resultDesc"));
				jsonResult.put("data", response);
				jsonResult.put("orderCode", wechatOrder.getOrderCode());
			}else {
				log.info("================>预交金充值失败：{}",response.getString("resultDesc"));
				jsonResult.put("code", 1001);
				jsonResult.put("msg", "预交金充值失败:"+response.getString("resultDesc"));
				jsonResult.put("data", "");
				jsonResult.put("orderCode", wechatOrder.getOrderCode());
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
			rc.setAmount(new BigDecimal(wechatOrder.getAmount()));//充值金额
			rc.setCardNo(patient.getCardNo());//卡号
			rc.setBType("YJJCZ");//业务类型
			rc.setCreateTime(sdf.format(new Date()));//创建时间
			rc.setIdCardNo(patient.getIdCardNo());//身份证号码
			rc.setName(patient.getUserName());//姓名
			rc.setPid(patient.getId());//pid
			rc.setWid(patient.getWid());//wid
			rc.setOrderCode(wechatOrder.getOrderCode());//订单号
			rc.setStatus(response.getString("resultCode"));
			rc.setMsg(response.getString("resultDesc"));
			log.info("================>预交金充值：{},",rc);
			commonDao.saveRecode(rc);
		}
		return jsonResult;
	}


	/**
	 * 预交金充值后再挂号
	 */
	@Override
	public JSONObject notifyToGH(WeChatOrder wechatOrder) {
		JSONObject  result =new JSONObject();
		JSONObject rechageResult = notifyToRecharge(wechatOrder);
		if(rechageResult.getString("code").equals("0")) {
			//预交金充值成功后再挂号
			JSONObject parse = JSONObject.parseObject(wechatOrder.getParams());
			log.info("==========》支付回调，挂号参数:{},",parse);
			commitGH(parse);
		}else {
			result=rechageResult;
		}
		
		return result;
	}
	
	
	/**
	 * 预交金充值后预约挂号
	 */
	@Override
	public JSONObject notifyToYYGH(WeChatOrder wechatOrder) {
		JSONObject  result =new JSONObject();
		JSONObject rechageResult = notifyToRecharge(wechatOrder);
		if(rechageResult.getString("code").equals("0")) {
			//预交金充值成功后再预约挂号
			JSONObject parse = JSONObject.parseObject(wechatOrder.getParams());
			log.info("==========》支付回调，预约挂号参数:{},",parse);
			commitYYGH(parse);
		}else {
			result=rechageResult;
		}
		return result;
	}
	
	
	/**
	 * 预约挂号
	 * TODO
	 * @param json
	 * @return
	 */
	public JSONObject commitYYGH(JSONObject json) {
		JSONObject jsonResult=new JSONObject();
		JSONObject response=new JSONObject();
		String path ="";
		GHInfo ghinfo =null;
		WeChatUser weChatUser=null;
		try {
			ghinfo=JSONObject.parseObject(json.toJSONString() , GHInfo.class);
			
        	String request="<xml>" + 
    				"<optioncode>8008</optioncode>" + //请求方法code
    				"<TermNo>"+hisConfig.getAccount()+"</TermNo>" + //终端号
    				"<QuDao>"+hisConfig.getType()+"</QuDao>" + //渠道参数:默认空---询问医院
    				"<Pwd>"+hisConfig.getPwd()+"</Pwd>" + //医院内部就诊卡磁卡3道数据(必须填写)银行卡、身份证直接作为一卡通时不需要,M1：6位随机码
    				"<parameters>" + 
	    				"<Type>1</Type>" + //挂号类型	int	0：当日号  1：挂预约号
	    				"<Date>"+ghinfo.getCondate()+"</Date>"+ //预约挂号日期：yyyy-mm-dd	当日：不需要
	    				"<DepartmentsID>"+ghinfo.getDeptId()+"</DepartmentsID>" + //科室ID
	    				"<Departments>"+ghinfo.getDeptName()+"</Departments>" + //科室名称
	    				"<GhTypeId>"+ghinfo.getGhTypeId()+"</GhTypeId>" + //挂号类型Id
	    				"<GhType>"+ghinfo.getGhType()+"</GhType>" + //挂号类型Des
	    				"<DoctorId>"+ghinfo.getDoctorId()+"</DoctorId>" + //医生id
	    				"<Doctor>"+ghinfo.getDoctorName()+"</Doctor>" + //医生
	    				"<Period>"+ghinfo.getPeriod()+"</Period>" + //就诊时段 如：上午、下午
	    				"<RegisteredAmount>"+ghinfo.getAmount()+"</RegisteredAmount>" + //挂号费  单位：元
	    				"<CardNo>"+ghinfo.getCardNo()+"</CardNo>" + //卡号
	    				"<PatientName>"+ghinfo.getUserName()+"</PatientName>" + //患者姓名
    				"</parameters>" + 
    				"</xml>";
        	
        	log.info("==============>支付回调，预约挂号，提交挂号，his入参：{},"+request);
        	response=HisReqClientUtil.getHisResponseCXF(request,hisConfig.getHisUrl());
        	log.info("==============>支付回调，预约挂号，提交挂号his，返参：{},",response);
        	
        	if(response.getString("resultCode").equals("1")) {
        		//.预约挂号模板消息
        		weChatUser = wxUserDao.queryWechatUserInfoById(ghinfo.getWid());
        		weChatTemplateMsgUtils.sendYYGHTemp(json,ghinfo,weChatUser.getOpenid(),response);
        		jsonResult.put("code", 0);
        		jsonResult.put("msg", response.getString("resultDesc"));
        		jsonResult.put("url",wxConfig.getProjectUrl()+"/hospital/yygh/ghDetail?order_id="+ghinfo.getOrderCode()+"&time="+json.getString("time")); 
        	}else {
        		jsonResult.put("code", response.getString("resultCode"));
        		jsonResult.put("msg", response.getString("resultDesc"));
        		jsonResult.put("url", wxConfig.getProjectUrl()+path);
        	}
        } catch (Exception e) {
            log.info("==============>支付回调，预约挂号，提交挂号异常：{},",e);
            jsonResult.put("code", response.getString("resultCode"));
    		jsonResult.put("msg", response.getString("resultDesc"));
    		jsonResult.put("url", wxConfig.getProjectUrl()+path);
        }
		finally {
        	//1.保存挂号信息，发送模板消息
        	saveYYGHInfo(ghinfo,response,weChatUser);
		}
		return jsonResult;
	}
	
	
	private void saveYYGHInfo(GHInfo ghinfo, JSONObject response, WeChatUser weChatUser) {
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String time = sdf.format(new Date());
		try {
			//1.保存预约挂号金额，操作类型等信息，方便对账
			Recode rc=new Recode();
			rc.setAmount(new BigDecimal(ghinfo.getAmount()));//挂号金额
			rc.setCardNo(ghinfo.getCardNo());//卡号
			rc.setBType("YYGH");//业务类型
			rc.setIdCardNo(ghinfo.getIdCardNo());//身份证号码
			rc.setName(ghinfo.getUserName());//姓名
			rc.setCreateTime(time);//
			rc.setPid(ghinfo.getPid());//pid
			rc.setWid(ghinfo.getWid());//wid
			rc.setOrderCode(ghinfo.getOrderCode());//订单号
			rc.setStatus(response.getString("resultCode"));
			rc.setMsg(response.getString("resultDesc"));
			log.info("==============>支付回调，操作记录：{},",rc);
			commonDao.saveRecode(rc);
			//2.保存预约挂号记录
			saveYYGHInfo(ghinfo,response);
		} catch (Exception e) {
			log.error("==============>支付回调，保存预约挂号,发送消息模板异常============"+e);
			e.printStackTrace();
		}
	}


	/**
	 * 保存预约挂号信息
	 * @param json
	 * @param patient
	 * @param response
	 */
	private void saveYYGHInfo(GHInfo ghinfo, JSONObject response) {
		try {
			SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String time = sdf.format(new Date());
			//1.保存挂号信息，方便查询挂号记录等信息
			 ghinfo.setActionType("YYGH");
			 ghinfo.setOrderStatus(0);
			 if(response.getString("resultCode").equals("1")) {
				 ghinfo.setGhId(response.getString("GhId"));//挂号单据号
				 ghinfo.setPiaoHao(response.getString("PiaoHao"));//票号
			 }
			 ghinfo.setCreateTime(time);
			 ghinfo.setCondate(ghinfo.getCondate());
			 ghdao.saveGHInfo(ghinfo);
		} catch (Exception e) {
			e.printStackTrace();
			log.info("==============>保存预约挂号相关信息异常============="+e);
		}
		
	 }



	/**
	 * 提交挂号
	 * TODO
	 * @param json
	 * @param req
	 * @param res
	 * @return
	 */
	public JSONObject commitGH(JSONObject json) {
		JSONObject jsonResult=new JSONObject();
		JSONObject response=new JSONObject();
		GHInfo ghinfo = null;
		WeChatUser weChatUser =null;
		try {
			ghinfo=JSONObject.parseObject(json.toJSONString() , GHInfo.class);
			
        	String request="<xml>" + 
    				"<optioncode>8008</optioncode>" + //请求方法code
    				"<TermNo>"+hisConfig.getAccount()+"</TermNo>" + //终端号
    				"<QuDao>"+hisConfig.getType()+"</QuDao>" + //渠道参数:默认空---询问医院
    				"<Pwd>"+hisConfig.getPwd()+"</Pwd>" + //医院内部就诊卡磁卡3道数据(必须填写)银行卡、身份证直接作为一卡通时不需要,M1：6位随机码
    				"<parameters>" + 
	    				"<Type>0</Type>" + //挂号类型	int	0：当日号  1：挂预约号
	    				"<Date/>" + //预约挂号日期：yyyy-mm-dd	当日：不需要
	    				"<DepartmentsID>"+ghinfo.getDeptId()+"</DepartmentsID>" + //科室ID
	    				"<Departments>"+ghinfo.getDeptName()+"</Departments>" + //科室名称
	    				"<GhTypeId>"+ghinfo.getGhTypeId()+"</GhTypeId>" + //挂号类型Id
	    				"<GhType>"+ghinfo.getGhType()+"</GhType>" + //挂号类型Des
	    				"<DoctorId>"+ghinfo.getDoctorId()+"</DoctorId>" + //医生id
	    				"<Doctor>"+ghinfo.getDoctorName()+"</Doctor>" + //医生
	    				"<Period>"+ghinfo.getPeriod()+"</Period>" + //就诊时段 如：上午、下午
	    				"<RegisteredAmount>"+ghinfo.getAmount()+"</RegisteredAmount>" + //挂号费  单位：元
	    				"<CardNo>"+ghinfo.getCardNo()+"</CardNo>" + //卡号
	    				"<PatientName>"+ghinfo.getUserName()+"</PatientName>" + //患者姓名
    				"</parameters>" + 
    				"</xml>";
        	log.info("==============>支付回调，提交挂号，his入参：{},"+request);
        	response=HisReqClientUtil.getHisResponseCXF(request,hisConfig.getHisUrl());
        	log.info("==============>支付回调，提交挂号，his返参：{},",response);
        	if(response.getString("resultCode").equals("1")) {
        		//1.挂号成功，发送模板消息
        		weChatUser = wxUserDao.queryWechatUserInfoById(ghinfo.getWid());
    			weChatTemplateMsgUtils.sendGHTemp(ghinfo, weChatUser.getOpenid(),response);
        		jsonResult.put("code", 0);
        		jsonResult.put("msg", response.getString("resultDesc"));
        		jsonResult.put("url",wxConfig.getProjectUrl()+"/hospital/gh/ghDetail?order_id="+ghinfo.getOrderCode());        		
        	}else {
        		jsonResult.put("code", response.getString("resultCode"));
        		jsonResult.put("msg", response.getString("resultDesc"));
        	}
        } catch (Exception e) {
            e.printStackTrace();
            log.info("==============>支付回调，提交挂号异常：{},",e);
            jsonResult.put("code", response.getString("resultCode"));
    		jsonResult.put("msg", response.getString("resultDesc"));
        }finally {
        	//1.保存挂号信息，发送模板消息
        	saveGHInfo(ghinfo,response,weChatUser);
		}
		return jsonResult;
	}
	
	/**
	 * 保存挂号信息
	 * @param ghinfo
	 * @param response
	 * @param weChatUser
	 */
	private void saveGHInfo(GHInfo ghinfo, JSONObject response, WeChatUser weChatUser) {
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String time = sdf.format(new Date());
		try {
			//1.保存挂号金额，操作类型等信息，方便对账
			Recode rc=new Recode();
			rc.setAmount(new BigDecimal(ghinfo.getAmount()));//挂号金额
			rc.setCardNo(ghinfo.getCardNo());//卡号
			rc.setBType("GH");//业务类型
			rc.setIdCardNo(ghinfo.getIdCardNo());//身份证号码
			rc.setName(ghinfo.getUserName());//姓名
			rc.setCreateTime(time);//
			rc.setPid(ghinfo.getPid());//pid
			rc.setWid(ghinfo.getWid());//wid
			rc.setOrderCode(ghinfo.getOrderCode());//订单号
			rc.setStatus(response.getString("resultCode"));
			rc.setMsg(response.getString("resultDesc"));
			log.info("==============>支付回调，操作记录：{},",rc);
			commonDao.saveRecode(rc);
			//2.保存挂号记录
			saveGHInfo(ghinfo,response);
		} catch (Exception e) {
			log.error("==============>支付回调，保存挂号记录，异常============"+e);
			e.printStackTrace();
		}
	}
	
	//保存挂号信息记录
	private void saveGHInfo(GHInfo ghinfo, JSONObject response) {
		try {
			SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String time = sdf.format(new Date());
			//1.保存挂号信息，方便查询挂号记录等信息
			 ghinfo.setActionType("GH");
			 ghinfo.setOrderStatus(0);
			 if(response.getString("resultCode").equals("1")) {
				 ghinfo.setGhId(response.getString("GhId"));//挂号单据号
				 ghinfo.setPiaoHao(response.getString("PiaoHao"));//票号
			 }
			 ghinfo.setCreateTime(time);
			 ghinfo.setCondate(time.substring(0, 10));
			 ghdao.saveGHInfo(ghinfo);
		} catch (Exception e) {
			e.printStackTrace();
			log.info("==============>支付回调，保存挂号相关信息异常============="+e);
		}
	}


	/**
	 * 支付回调，门诊缴费
	 */
	@Override
	public JSONObject notifyToMZJF(WeChatOrder wechatOrder) {
		JSONObject jsonResult=new JSONObject();
		JSONObject response=new JSONObject();
		WeChatUser weChatUser=null;
		Patient patient=null;
		String item ="";
		JSONObject json =null;
		try {
			patient=commonDao.queryPatientByID(wechatOrder.getPid());
			item = wechatOrder.getParams();
			weChatUser = wxUserDao.queryWechatUserInfoById(wechatOrder.getWid());
			json = JSONObject.parseObject(wechatOrder.getJfjson());
			log.info("===========支付回调，门诊缴费,患者信息：{},缴费item:{},微信信息:{},",patient.toString(),item,weChatUser.toString());
			
			
        	String request="<xml>" + 
    				"<optioncode>8011</optioncode>" + //请求方法code
    				"<TermNo>"+hisConfig.getAccount()+"</TermNo>" + //终端号
    				"<QuDao>"+hisConfig.getType()+"</QuDao>" + //渠道参数:默认空---询问医院
    				"<Pwd>"+hisConfig.getPwd()+"</Pwd>" + //医院内部就诊卡磁卡3道数据(必须填写)银行卡、身份证直接作为一卡通时不需要,M1：6位随机码
    				"<parameters>" + 
	    				"<CardNo>"+patient.getCardNo()+"</CardNo>" + //卡号
	    				"<PatientName>"+patient.getUserName()+"</PatientName>" + //患者姓名
	    				 item+
    				"</parameters>" + 
    				"</xml>";
        	log.info("==============>支付回调，门诊缴费，请求his入参：{},"+request);
        	response=HisReqClientUtil.getHisResponseCXF(request,hisConfig.getHisUrl());
        	log.info("==============>支付回调，门诊缴费，请求his返参：{},",response);
        	if(response.getString("resultCode").equals("1")) {
        		response.put("prescriptionMoney", wechatOrder.getAmount());//缴费总金额（发送模板消息用到）
        		//1.门诊缴费成功发送模板消息
    			weChatTemplateMsgUtils.sendMZJFTemp(json,patient,response, weChatUser.getOpenid());
    			
    			//2.保存缴费信息
        		jsonResult.put("code", 0);
        		jsonResult.put("msg", response.getString("resultDesc"));
        		jsonResult.put("sjh", response.getString("SJH"));
        		jsonResult.put("url", wxConfig.getProjectUrl()+"/hospital/mzjf/jfList");
        	}else {
        		jsonResult.put("code", 1001);
        		jsonResult.put("msg", response.getString("resultDesc"));
        		jsonResult.put("sjh", "");
        		jsonResult.put("url", wxConfig.getProjectUrl()+"/hospital/mzjf/jfList");
        	}
        } catch (Exception e) {
            e.printStackTrace();
            log.info("==============>支付回调，门诊缴费异常：{},",e);
            jsonResult.put("code", 1001);
    		jsonResult.put("msg", "门诊缴费异常");
    		jsonResult.put("sjh", "");
    		jsonResult.put("url", wxConfig.getProjectUrl()+"/hospital/mzjf/jfList");
        }finally {
        	//1.保存门诊缴费记录
        	saveMZJFRecode(json,patient,wechatOrder.getMzjfAmount(),response,weChatUser);
			//2.清空缴费item和缴费总金额
        }
		
		return jsonResult;
	}


	/**
	 * 保存门诊缴费记录，发送模板消息
	 * @param json 
	 * @param patient
	 * @param response
	 * @param weChatUser
	 */
	private void saveMZJFRecode(JSONObject json, Patient patient, String prescriptionMoney,JSONObject response, WeChatUser weChatUser) {
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String time = sdf.format(new Date());
		try {
			//1.保存门诊缴费金额，操作类型等信息，方便对账
			Recode rc=new Recode();
			rc.setAmount(new BigDecimal(prescriptionMoney));//缴费金额
			rc.setCardNo(patient.getCardNo());//卡号
			rc.setBType("MZJF");//业务类型
			rc.setIdCardNo(patient.getIdCardNo());//身份证号码
			rc.setName(patient.getUserName());//姓名
			rc.setCreateTime(time);//
			rc.setOrderCode(OrderCodeUtil.createOrderCode());//生成一个订单号
			rc.setPid(patient.getId());//pid
			rc.setWid(patient.getWid());//wid
			rc.setStatus(response.getString("resultCode"));
			rc.setMsg(response.getString("resultDesc"));
			log.info("==============>支付回调，操作记录：{},",rc);
			commonDao.saveRecode(rc);
			//2.保存缴费记录
			//saveMZJFInfo(patient,response);
		} catch (Exception e) {
			log.error("==============>支付回调，门诊缴费,发送消息模板异常============"+e);
			e.printStackTrace();
		}
	}



	
	
	
 }



	
