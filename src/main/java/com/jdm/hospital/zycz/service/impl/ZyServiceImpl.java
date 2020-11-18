package com.jdm.hospital.zycz.service.impl;

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

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jdm.hospital.common.dao.CommonDao;
import com.jdm.hospital.common.domain.HisConfig;
import com.jdm.hospital.common.domain.Patient;
import com.jdm.hospital.common.domain.Recode;
import com.jdm.hospital.common.domain.WeChatOrder;
import com.jdm.hospital.utils.HisReqClientUtil;
import com.jdm.hospital.utils.WeChatTemplateMsgUtils;
import com.jdm.hospital.utils.XmlJsonUtils;
import com.jdm.hospital.zycz.dao.ZyDao;
import com.jdm.hospital.zycz.service.ZyService;
import com.jdm.wechat.common.dao.WxUserDao;
import com.jdm.wechat.common.domain.WeChatUser;


@Service
public class ZyServiceImpl implements ZyService{
	
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	@Autowired
	ZyDao zyDao;
	@Autowired
	HisConfig hisConfig;
	@Autowired
	CommonDao commonDao;
	@Autowired
	WeChatTemplateMsgUtils weChatTemplateMsgUtils;
	@Autowired
	WxUserDao wxUserDao;
	
	/**
	 * 按住院号查询在院病人基本信息(有一卡通的)
	 */
	@Override
	public JSONObject queryPatInfoByZyh(JSONObject json, HttpServletRequest req, HttpServletResponse res) {
		JSONObject jsonResult=new JSONObject();
		JSONObject response=new JSONObject();
		//String idNo = json.getString("idNo");
		try {
        	String request="<xml>" + 
    				"<optioncode>8061</optioncode>" + //请求方法code
    				"<TermNo>"+hisConfig.getAccount()+"</TermNo>" + //终端号
    				"<QuDao>"+hisConfig.getType()+"</QuDao>" + //渠道参数:默认空---询问医院
    				"<Pwd>"+hisConfig.getPwd()+"</Pwd>" + //医院内部就诊卡磁卡3道数据(必须填写)银行卡、身份证直接作为一卡通时不需要,M1：6位随机码
    				"<parameters>" + 
	    				"<Zyh>"+json.getString("zyh")+"</Zyh>" + //卡号
    				"</parameters>" + 
    				"</xml>";
        	log.info("==============>按住院号查询在院病人基本信息(有一卡通的)，请求his入参：{},"+request);
        	response=HisReqClientUtil.getHisResponseCXF(request,hisConfig.getHisUrl());       	
        	log.info("==============>按住院号查询在院病人基本信息(有一卡通的)，请求his返参：{},",response);
        	if(response.getString("resultCode").equals("1")) {
        		jsonResult.put("code", 0);
        		jsonResult.put("msg", response.getString("resultDesc"));
        		jsonResult.put("data", response);
        	}else {
        		jsonResult.put("code", 1001);
        		jsonResult.put("msg", response.getString("resultDesc"));
        		jsonResult.put("data", "");
        	}
        } catch (Exception e) {
            e.printStackTrace();
            log.info("==============>按住院号查询在院病人基本信息(有一卡通的)：{},",e);
            jsonResult.put("code", 1001);
    		jsonResult.put("msg", "接口异常");
    		jsonResult.put("data", "");
        }
		return jsonResult;
	}

	/**
	 * 按住院号查询在院病人基本信息(-)
	 */
	@Override
	public JSONObject queryPatInfoByZyhNo(JSONObject json, HttpServletRequest req, HttpServletResponse res) {
		JSONObject jsonResult=new JSONObject();
		JSONObject response=new JSONObject();
		try {
        	String request="<xml>" + 
    				"<optioncode>8062</optioncode>" + //请求方法code
    				"<TermNo>"+hisConfig.getAccount()+"</TermNo>" + //终端号
    				"<QuDao>"+hisConfig.getType()+"</QuDao>" + //渠道参数:默认空---询问医院
    				"<Pwd>"+hisConfig.getPwd()+"</Pwd>" + //医院内部就诊卡磁卡3道数据(必须填写)银行卡、身份证直接作为一卡通时不需要,M1：6位随机码
    				"<parameters>" + 
	    				"<Zyh>"+json.getString("zyh")+"</Zyh>" + //卡号
	    				"<MyStatus></MyStatus>" + //状态 空：只查在院病人 1：查询所有状态的病人(默认空)
	    				"<IsXuNi>1</IsXuNi>" + //是否查询关联的虚拟一卡通信息  1：查询  否则：不查询
    				"</parameters>" + 
    				"</xml>";
        	log.info("==============>按住院号查询在院病人基本信息(-)，请求his入参：{},"+request);
        	response=HisReqClientUtil.getHisResponseCXF(request,hisConfig.getHisUrl());
        	log.info("==============>按住院号查询在院病人基本信息(-)，请求his返参：{},",response);
        	if(response.getString("resultCode").equals("1")) {
        		jsonResult.put("code", 0);
        		jsonResult.put("msg", response.getString("resultDesc"));
        		jsonResult.put("data", response);
        	}else {
        		jsonResult.put("code", 0);
        		jsonResult.put("msg", response.getString("resultDesc"));
        		jsonResult.put("data", new JSONArray());
        	}
        } catch (Exception e) {
            e.printStackTrace();
            log.info("==============>按住院号查询在院病人基本信息(-)：{},",e);
            jsonResult.put("code", 1001);
    		jsonResult.put("msg", "接口异常");
    		jsonResult.put("data", "");
        }
		return jsonResult;
	}
	
	
	/**
	 * 住院内部号虚拟为一卡通进行充值，并交住院押金
	 */
	@Override
	public JSONObject rechageZyyjByZYNo(JSONObject json, HttpServletRequest req, HttpServletResponse res) {
		JSONObject jsonResult=new JSONObject();
		JSONObject response=new JSONObject();
		String orderCode = json.getString("orderCode");//订单号
		
		HttpSession session = req.getSession();
		WeChatUser weChatUser=(WeChatUser)session.getAttribute("weChatUser");//微信用户信息
		Patient patient = (Patient)session.getAttribute("patient");//患者信息
		String registerNoId=(String) session.getAttribute("registerNoId");//住院内部号
		
		
		
		//1.先查询该订单号是否存在，存在则直接返回
		Recode recode=commonDao.queryRecodeByOrderCode(weChatUser.getId(),orderCode);
		if(recode!=null) {
			jsonResult.put("code", 0);
			jsonResult.put("msg", recode.getStatus().equals("1")?"成功":"失败");
			jsonResult.put("data", "");
			jsonResult.put("orderCode", orderCode);
			log.info("==============>住院充值，该订单已支付，直接返回：{},",recode);
			return jsonResult;
		}
		
		
		try {
        	String request="<xml>" + 
    				"<optioncode>8401</optioncode>" + //请求方法code
    				"<TermNo>"+hisConfig.getAccount()+"</TermNo>" + //终端号
    				"<QuDao>"+hisConfig.getType()+"</QuDao>" + //渠道参数:默认空---询问医院
    				"<Pwd>"+hisConfig.getPwd()+"</Pwd>" + //医院内部就诊卡磁卡3道数据(必须填写)银行卡、身份证直接作为一卡通时不需要,M1：6位随机码
    				"<parameters>" + 
	    				"<RegisterNoId>"+registerNoId+"</RegisterNoId>" + //住院内部号
	    				"<Amount>"+json.getString("amount")+"</Amount>" + //本次交易的总金额 单位：元
	    				"<PayWay>4</PayWay>" +//付款方式 1：现金;2：建行 3：农行 4：微信 5：支付宝 6：掌上医院
	    				"<BankCardNo></BankCardNo>" + //银行卡卡号微信号等 PayWay <>1 时有效
	    				"<SerialNumber></SerialNumber>" + //
	    				"<DingDanHao>"+orderCode+"</DingDanHao>" + //生成一个订单号
    				"</parameters>" + 
    				"</xml>";
        	log.info("==============>住院内部号虚拟为一卡通进行充值，并交住院押金，请求his入参：{},"+request);
        	response=HisReqClientUtil.getHisResponseCXF(request,hisConfig.getHisUrl());
        	log.info("==============>住院内部号虚拟为一卡通进行充值，并交住院押金，请求his返参：{},",response);
        	if(response.getString("resultCode").equals("1")) {
        		//1.发送模板消息
    			//weChatTemplateMsgUtils.sendZYCZTemp(patient,json,response,weChatUser);
        		jsonResult.put("code", 0);
        		jsonResult.put("msg", response.getString("resultDesc"));
        		jsonResult.put("data", response);
        	}else {
        		jsonResult.put("code", 1001);
        		jsonResult.put("msg", response.getString("resultDesc"));
        		jsonResult.put("data", "");
        	}
        } catch (Exception e) {
            e.printStackTrace();
            log.info("==============>住院内部号虚拟为一卡通进行充值，并交住院押金：{},",e);
            jsonResult.put("code", 1001);
    		jsonResult.put("msg", "接口异常");
    		jsonResult.put("data", "");
        }finally {
        	//1.保存住院充值信息，发送模板消息
        	//清空住院内部号
        	session.setAttribute("registerNoId", "");
        	//saveZyRecode(patient,json,response,weChatUser);
		}
		return jsonResult;
	}
	
	/**
	 * @param wechatOrder
	 * @param response
	 * @param weChatUser
	 */
	private void saveZyRecode(Patient patient,WeChatOrder wechatOrder, JSONObject response, WeChatUser weChatUser) {
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String time = sdf.format(new Date());
		try {
			//1.保存挂号金额，操作类型等信息，方便对账
			Recode rc=new Recode();
			rc.setAmount(new BigDecimal(wechatOrder.getAmount()));//挂号金额json.getString("registerNoId")
			rc.setCardNo(patient.getCardNo());//卡号
			rc.setBType("ZYCZ");//业务类型
			rc.setIdCardNo(patient.getIdCardNo());//身份证号码
			rc.setName(patient.getUserName());//姓名
			rc.setCreateTime(time);//创建时间
			rc.setPid(patient.getId());//pid
			rc.setWid(patient.getWid());//wid
			rc.setOrderCode(wechatOrder.getOrderCode());//订单号
			rc.setStatus(response.getString("resultCode"));
			rc.setMsg(response.getString("resultDesc"));
			log.info("==============>操作记录：{},",rc);
			commonDao.saveRecode(rc);
			
		} catch (Exception e) {
			log.error("==============>保存住院充值,发送消息模板异常============"+e);
			e.printStackTrace();
		}
	}

	/**
	 * 就诊卡转住院押金
	 */
	@Override
	public JSONObject cardMoneychangeToZyyj(JSONObject json, HttpServletRequest req, HttpServletResponse res) {
		JSONObject jsonResult=new JSONObject();
		JSONObject response=new JSONObject();
		HttpSession session = req.getSession();
		Patient patient=(Patient) session.getAttribute("patient");
		//测试数据
		//Patient patient=new Patient();
		//patient.setCardNo("X2020020800001");
		try {
        	String request="<xml>" + 
    				"<optioncode>8053</optioncode>" + //请求方法code
    				"<TermNo>"+hisConfig.getAccount()+"</TermNo>" + //终端号
    				"<QuDao>"+hisConfig.getType()+"</QuDao>" + //渠道参数:默认空---询问医院
    				"<Pwd>"+hisConfig.getPwd()+"</Pwd>" + //医院内部就诊卡磁卡3道数据(必须填写)银行卡、身份证直接作为一卡通时不需要,M1：6位随机码
    				"<parameters>" + 
    					"<CardNo>"+patient.getCardNo()+"</CardNo>" + //卡号
    					"<Amount>"+json.getString("amount")+"</Amount>" + //金额
	    				//"<RegisterNo>"+json.getString("registerNo")+"</RegisterNo>" + //住院号
	    				"<RegisterNoId>"+json.getString("registerNoId")+"</RegisterNoId>" + //住院内部号
    				"</parameters>" + 
    				"</xml>";
        	log.info("==============>就诊卡转住院押金，请求his入参：{},"+request);
        	response=HisReqClientUtil.getHisResponseCXF(request,hisConfig.getHisUrl());
        	log.info("==============>就诊卡转住院押金，请求his返参：{},",response);
        	if(response.getString("resultCode").equals("1")) {
        		jsonResult.put("code", 0);
        		jsonResult.put("msg", response.getString("resultDesc"));
        		jsonResult.put("data", response);
        	}else {
        		jsonResult.put("code", 1001);
        		jsonResult.put("msg", response.getString("resultDesc"));
        		jsonResult.put("data", "");
        	}
        } catch (Exception e) {
            e.printStackTrace();
            log.info("==========就诊卡转住院押金：{},",e);
            jsonResult.put("code", 1001);
    		jsonResult.put("msg", "接口异常");
    		jsonResult.put("data", "");
        }
		return jsonResult;
	}
	
	/**
	 * 根据住院号获取当前在院病人费用流水
	 */
	@Override
	public JSONObject queryZyFeeByZyNo(JSONObject json, HttpServletRequest req, HttpServletResponse res) {
		JSONObject jsonResult=new JSONObject();
		JSONObject response=new JSONObject();
		try {
        	String request="<xml>" + 
    				"<optioncode>8402</optioncode>" + //请求方法code
    				"<TermNo>"+hisConfig.getAccount()+"</TermNo>" + //终端号
    				"<QuDao>"+hisConfig.getType()+"</QuDao>" + //渠道参数:默认空---询问医院
    				"<Pwd>"+hisConfig.getPwd()+"</Pwd>" + //医院内部就诊卡磁卡3道数据(必须填写)银行卡、身份证直接作为一卡通时不需要,M1：6位随机码
    				"<parameters>" + 
	    				"<Zyh>"+json.getString("zyh")+"</Zyh>" + //住院号
	    				"<MyDateS>"+json.getString("start")+"</MyDateS>" + //开始日期(包括)空：不限制
	    				"<MyDateE>"+json.getString("end")+"</MyDateE>" + //结束日期(不包括)	空：不限制
	    				"<NumDays4MyDateE></NumDays4MyDateE>" + //结束日期天数增量
	    				"<IsItemType></IsItemType>" + //1:返回结果Item模式 否则：Item1\Item2模式
	    				"<IsGz></IsGz>" + //是否高值 1：高值 0：非高值 空：全部
    				"</parameters>" + 
    				"</xml>";
        	log.info("==============>根据住院号获取当前在院病人费用流水，请求his入参：{},"+request);
        	response=HisReqClientUtil.getHisResponseCXF(request,hisConfig.getHisUrl());
        	log.info("==============>根据住院号获取当前在院病人费用流水，请求his返参：{},",response);
        	if(response.getString("resultCode").equals("1")) {
        		jsonResult.put("code", 0);
        		jsonResult.put("msg", response.getString("resultDesc"));
        		jsonResult.put("data", response);
        	}else {
        		jsonResult.put("code", 1001);
        		jsonResult.put("msg", response.getString("resultDesc"));
        		jsonResult.put("data", "");
        	}
        } catch (Exception e) {
            e.printStackTrace();
            log.info("==============>根据住院号获取当前在院病人费用流水：{},",e);
            jsonResult.put("code", 1001);
    		jsonResult.put("msg", "接口异常");
    		jsonResult.put("data", "");
        }
		return jsonResult;
	}

	
	/**
	 * 按住院号查询病人医嘱信息
	 */
	@Override
	public JSONObject queryDocInfoforPatByZyNo(JSONObject json, HttpServletRequest req,
			HttpServletResponse res) {
		JSONObject jsonResult=new JSONObject();
		JSONObject response=new JSONObject();
		try {
        	String request="<xml>" + 
    				"<optioncode>8700</optioncode>" + //请求方法code
    				"<TermNo>"+hisConfig.getAccount()+"</TermNo>" + //终端号
    				"<QuDao>"+hisConfig.getType()+"</QuDao>" + //渠道参数:默认空---询问医院
    				"<Pwd>"+hisConfig.getPwd()+"</Pwd>" + //医院内部就诊卡磁卡3道数据(必须填写)银行卡、身份证直接作为一卡通时不需要,M1：6位随机码
    				"<parameters>" + 
	    				"<Zyh>"+json.getString("zyh")+"</Zyh>" + //住院号
	    				"<SqJianchaCati_Id>"+json.getString("sqJianchaCati_Id")+"</SqJianchaCati_Id>" + //检查项目分类编码,多个分类可以用英文逗号分隔
	    				"<SqLISCati_Id>"+json.getString("sqLISCati_Id")+"</SqLISCati_Id>" + //化验项目分类编码,多个分类可以用英文逗号分隔
    				"</parameters>" + 
    				"</xml>";
        	log.info("==============>按住院号查询病人医嘱信息，请求his入参：{},"+request);
        	response=HisReqClientUtil.getHisResponseCXF(request,hisConfig.getHisUrl());
        	String result="";
        	response=JSONObject.parseObject(XmlJsonUtils.xmlToJson(result));
        	
        	log.info("==============>按住院号查询病人医嘱信息，请求his返参：{},",response);
        	if(response.getString("resultCode").equals("1")) {
        		jsonResult.put("code", 0);
        		jsonResult.put("msg", response.getString("resultDesc"));
        		jsonResult.put("data", response);
        	}else {
        		jsonResult.put("code", 1001);
        		jsonResult.put("msg", response.getString("resultDesc"));
        		jsonResult.put("data", "");
        	}
        } catch (Exception e) {
            e.printStackTrace();
            log.info("==============>根据住院号获取当前在院病人费用流水：{},",e);
            jsonResult.put("code", 1001);
    		jsonResult.put("msg", "接口异常");
    		jsonResult.put("data", "");
        }
		return jsonResult;
	}
	
	
	/**
	 * 根据住院号获取住院病人费用分类汇总
	 */
	@Override
	public JSONObject queryZyFenLeiByZyNo(JSONObject json, HttpServletRequest req, HttpServletResponse res) {
		JSONObject jsonResult=new JSONObject();
		JSONObject response=new JSONObject();
		try {
        	String request="<xml>" + 
    				"<optioncode>8704</optioncode>" + //请求方法code
    				"<TermNo>"+hisConfig.getAccount()+"</TermNo>" + //终端号
    				"<QuDao>"+hisConfig.getType()+"</QuDao>" + //渠道参数:默认空---询问医院
    				"<Pwd>"+hisConfig.getPwd()+"</Pwd>" + //医院内部就诊卡磁卡3道数据(必须填写)银行卡、身份证直接作为一卡通时不需要,M1：6位随机码
    				"<parameters>" + 
	    				"<Zyh>"+json.getString("zyh")+"</Zyh>" + //住院号
	    				"<MyType>"+json.getString("myType")+"</MyType>" + //0:发票分类 1:财务分类 2:项目分类
    				"</parameters>" + 
    				"</xml>";
        	log.info("==============>根据住院号获取住院病人费用分类汇总，请求his入参：{},"+request);
        	response=HisReqClientUtil.getHisResponseCXF(request,hisConfig.getHisUrl());
        	log.info("==============>根据住院号获取住院病人费用分类汇总，请求his返参：{},",response);
        	if(response.getString("resultCode").equals("1")) {
        		jsonResult.put("code", 0);
        		jsonResult.put("msg", response.getString("resultDesc"));
        		jsonResult.put("data", response);
        	}else {
        		jsonResult.put("code", 1001);
        		jsonResult.put("msg", response.getString("resultDesc"));
        		jsonResult.put("data", "");
        	}
        } catch (Exception e) {
            e.printStackTrace();
            log.info("==============>根据住院号获取住院病人费用分类汇总：{},",e);
            jsonResult.put("code", 1001);
    		jsonResult.put("msg", "接口异常");
    		jsonResult.put("data", "");
        }
		return jsonResult;
	}

	@Override
	public JSONObject notifyToRechargeZyFee(WeChatOrder wechatOrder) {
		JSONObject jsonResult=new JSONObject();
		JSONObject response=new JSONObject();
		
		
		WeChatUser weChatUser = wxUserDao.queryWechatUserInfoById(wechatOrder.getWid());
		Patient patient = wxUserDao.getPatientByWid(wechatOrder.getWid());
		log.info("==============>微信支付回调预交金充值，微信用户信息：{},患者信息：{},",weChatUser,patient);
		
		String registerNoId=wechatOrder.getSqBaId();//住院内部号
		
		//1.先查询该订单号是否存在，存在则直接返回
		Recode recode=commonDao.queryRecodeByOrderCode(weChatUser.getId(),wechatOrder.getOrderCode());
		if(recode!=null) {
			jsonResult.put("code", 0);
			jsonResult.put("msg", recode.getStatus().equals("1")?"成功":"失败");
			jsonResult.put("data", "");
			jsonResult.put("orderCode", wechatOrder.getOrderCode());
			log.info("==============>住院充值，该订单已支付，直接返回：{},",recode);
			return jsonResult;
		}
		
		
		try {
        	String request="<xml>" + 
    				"<optioncode>8401</optioncode>" + //请求方法code
    				"<TermNo>"+hisConfig.getAccount()+"</TermNo>" + //终端号
    				"<QuDao>"+hisConfig.getType()+"</QuDao>" + //渠道参数:默认空---询问医院
    				"<Pwd>"+hisConfig.getPwd()+"</Pwd>" + //医院内部就诊卡磁卡3道数据(必须填写)银行卡、身份证直接作为一卡通时不需要,M1：6位随机码
    				"<parameters>" + 
	    				"<RegisterNoId>"+registerNoId+"</RegisterNoId>" + //住院内部号
	    				"<Amount>"+wechatOrder.getAmount()+"</Amount>" + //本次交易的总金额 单位：元
	    				"<PayWay>4</PayWay>" +//付款方式 1：现金;2：建行 3：农行 4：微信 5：支付宝 6：掌上医院
	    				"<BankCardNo></BankCardNo>" + //银行卡卡号微信号等 PayWay <>1 时有效
	    				"<SerialNumber></SerialNumber>" + //
	    				"<DingDanHao>"+wechatOrder.getOrderCode()+"</DingDanHao>" + //生成一个订单号
    				"</parameters>" + 
    				"</xml>";
        	log.info("==============>支付回调，住院内部号虚拟为一卡通进行充值，并交住院押金，请求his入参：{},"+request);
        	response=HisReqClientUtil.getHisResponseCXF(request,hisConfig.getHisUrl());
        	log.info("==============>支付回调，住院内部号虚拟为一卡通进行充值，并交住院押金，请求his返参：{},",response);
        	if(response.getString("resultCode").equals("1")) {
        		//1.发送模板消息
    			weChatTemplateMsgUtils.sendZYCZTemp(patient,wechatOrder,response,weChatUser);
        		jsonResult.put("code", 0);
        		jsonResult.put("msg", response.getString("resultDesc"));
        		jsonResult.put("data", response);
        	}else {
        		jsonResult.put("code", 1001);
        		jsonResult.put("msg", response.getString("resultDesc"));
        		jsonResult.put("data", "");
        	}
        } catch (Exception e) {
            e.printStackTrace();
            log.info("==============>住院内部号虚拟为一卡通进行充值，并交住院押金：{},",e);
            jsonResult.put("code", 1001);
    		jsonResult.put("msg", "接口异常");
    		jsonResult.put("data", "");
        }finally {
        	//1.保存住院充值信息
        	saveZyRecode(patient,wechatOrder,response,weChatUser);
		}
		return jsonResult;
	}
	
	
	public static void main(String[] args) {
		String str="{\r\n" + 
				"	\"MyDate4Out\": \"3000/1/1 0:00:00\",\r\n" + 
				"	\"SqHuliYizhuWh_Id\": [],\r\n" + 
				"	\"resultCode\": \"1\",\r\n" + 
				"	\"SqbasOper_Id4DocIn\": \"456\",\r\n" + 
				"	\"MyMoney4Danbao\": \"0.00\",\r\n" + 
				"	\"RemainMoney\": \"0\",\r\n" + 
				"	\"ContactTele\": \"123456789\",\r\n" + 
				"	\"BingquGroupNum\": \"0\",\r\n" + 
				"	\"Id_Des4JieheCati\": [],\r\n" + 
				"	\"resultObjects\": [],\r\n" + 
				"	\"Id_PayTypeDes\": \"自费医疗\",\r\n" + 
				"	\"MyDate4In\": \"2020/4/21 0:00:00\",\r\n" + 
				"	\"SqkSite_Id4Dep4Out\": \"0BQ\",\r\n" + 
				"	\"CurMoney\": \"0.0000\",\r\n" + 
				"	\"MyStatus\": \"0\",\r\n" + 
				"	\"SqBaId\": \"202004210220001\",\r\n" + 
				"	\"Id_PayTypeId\": \"3\",\r\n" + 
				"	\"SqbasOper_Des4Doc4Bed\": \"苏亚青\",\r\n" + 
				"	\"SqCardId4Xn\": [],\r\n" + 
				"	\"SexFlag\": \"男\",\r\n" + 
				"	\"BirthDay\": \"1988/8/1 0:00:00\",\r\n" + 
				"	\"PatientName\": \"鲁智深\",\r\n" + 
				"	\"SqCardId\": [],\r\n" + 
				"	\"resultDesc\": \"成功\",\r\n" + 
				"	\"BedDes\": [],\r\n" + 
				"	\"RemainMoney4Xn\": \"0\",\r\n" + 
				"	\"CurPrepayLeft\": \"1000.0000\",\r\n" + 
				"	\"CardNo\": [],\r\n" + 
				"	\"SqbasOper_Des4DocIn\": \"苏亚青\",\r\n" + 
				"	\"CurPrepay\": \"1000.0000\",\r\n" + 
				"	\"SqkSite_Id4Out\": \"0BQ\",\r\n" + 
				"	\"SqkSite_Des4Out\": \"康复医学科\",\r\n" + 
				"	\"CardNo4Xn\": [],\r\n" + 
				"	\"ContactName\": [],\r\n" + 
				"	\"SqbasOper_Id4Doc4Bed\": \"456\",\r\n" + 
				"	\"SqkSite_Des4Dep4Out\": \"康复医学科\",\r\n" + 
				"	\"Id_Id4JieheCati\": [],\r\n" + 
				"	\"ContactAddress_Des\": []\r\n" + 
				"}";
		JSONObject response = JSONObject.parseObject(str);
		if(response.getString("resultCode").equals("1")) {
			
			String SqBaId = response.getString("SqBaId");//SqBaId
			String resultDesc = response.getString("resultDesc");
			System.out.println(SqBaId);
			System.out.println(resultDesc);
		}
	}
}


