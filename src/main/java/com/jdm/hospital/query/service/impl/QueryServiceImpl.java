package com.jdm.hospital.query.service.impl;

import java.time.LocalDate;

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
import com.jdm.hospital.query.service.QueryService;
import com.jdm.hospital.utils.HisReqClientUtil;

@Service
public class QueryServiceImpl implements QueryService {
	
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	@Autowired
	HisConfig hisConfig;
	@Autowired
	CommonDao commonDao;
	
	/**
	 * 根据住院号获取当前在院病人费用流水
	 */
	@Override
	public JSONObject zyFlowDetailList(JSONObject json, HttpServletRequest req, HttpServletResponse res) {
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
	    				"<MyDateS>"+json.getString("startDate")+"</MyDateS>" + //开始日期(包括)空：不限制
	    				"<MyDateE>"+json.getString("endDate")+"</MyDateE>" + //结束日期(不包括)	空：不限制
	    				//"<NumDays4MyDateE>0</NumDays4MyDateE>" + //结束日期天数增量
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
        		jsonResult.put("data", response.getJSONObject("resultObjects"));
        	}else {
        		jsonResult.put("code", 0);
        		jsonResult.put("msg", response.getString("resultDesc"));
        		jsonResult.put("data", new JSONArray());
        	}
        } catch (Exception e) {
            e.printStackTrace();
            log.info("==============>根据住院号获取当前在院病人费用流水异常：{},",e);
            jsonResult.put("code", 1001);
    		jsonResult.put("msg", "接口异常");
    		jsonResult.put("data", "");
        }
		return jsonResult;
	}
	
	
	
	/**
	 * 门诊费用列表
	 */
	@Override
	public JSONObject mzFyList(JSONObject json, HttpServletRequest req, HttpServletResponse res) {
		
		JSONObject jsonResult=new JSONObject();
		JSONObject response=new JSONObject();
		HttpSession session = req.getSession();
		Patient patient=(Patient) session.getAttribute("patient");
		//Patient patient=new Patient();
		//patient.setCardNo("X2020020800001");
		try {
			String type = json.getString("type");
			String startDate = json.getString("startDate");
			String endDate = json.getString("endDate");
			if(startDate.isEmpty()||endDate.isEmpty()) {
				LocalDate localDate = LocalDate.now();
				startDate=localDate.minusDays(30)+"";
				endDate=localDate+"";
			}
        	String request="<xml>" + 
    				"<optioncode>8108</optioncode>" + //请求方法code
    				"<TermNo>"+hisConfig.getAccount()+"</TermNo>" + //终端号
    				"<QuDao>"+hisConfig.getType()+"</QuDao>" + //渠道参数:默认空---询问医院
    				"<Pwd>"+hisConfig.getPwd()+"</Pwd>" + //医院内部就诊卡磁卡3道数据(必须填写)银行卡、身份证直接作为一卡通时不需要,M1：6位随机码
	    				"<parameters>" + 
		    				"<Id4Card>"+patient.getCardNo()+"</Id4Card>" + //住院号
		    				"<MyDateS>"+startDate+"</MyDateS>" + //开始日期(包括)空：不限制
		    				"<MyDateE>"+endDate+"</MyDateE>" + //结束日期(不包括)	空：不限制
		    				//"<NumDays4MyDateE></NumDays4MyDateE>" + //结束日期天数增量
		    				//事务编码:00	卡内部业务01转帐门诊03转账挂号,07一卡通押金,10一卡通押金N
		    				"<CardAffair_Ids>"+type+"</CardAffair_Ids>" + 
		    				"<GhOperator_DesLike></GhOperator_DesLike>" + //操作员名称Like匹配,请传：CCB%
		    				"<MyMoneyDy0></MyMoneyDy0>" + //1:查询条件明细金额>0
		    				"<IsBankOri></IsBankOri>" + //1:查询条件排除退款和冲正业务
		    				"<IsItem></IsItem>" + //1:返回Item模式，否则：Item1\Item2模式
	    				"</parameters>" + 
    				"</xml>";
        	log.info("==============>查询门诊费用列表，请求his入参：{},"+request);
        	response=HisReqClientUtil.getHisResponseCXF(request,hisConfig.getHisUrl());
        	log.info("==============>查询门诊费用列表，请求his返参：{},",response);
        	if(response.getString("resultCode").equals("1")) {
        		jsonResult.put("code", 0);
        		jsonResult.put("msg", response.getString("resultDesc"));
        		jsonResult.put("data", response.getJSONObject("resultObjects"));
        	}else {
        		jsonResult.put("code", 0);
        		jsonResult.put("msg", response.getString("resultDesc"));
        		jsonResult.put("data", "");
        	}
        } catch (Exception e) {
            e.printStackTrace();
            log.info("==============>查询门诊费用列表异常：{},",e);
            jsonResult.put("code", 1001);
    		jsonResult.put("msg", "查询门诊费用列表异常");
    		jsonResult.put("data", "");
        }
		return jsonResult;
	}


	/**
	 * 一卡通—根据卡号获取当前在院病人押金条明细
	 */
	@Override
	public JSONObject depositList(JSONObject json, HttpServletRequest req, HttpServletResponse res) {
		JSONObject jsonResult=new JSONObject();
		JSONObject response=new JSONObject();
		try {
        	String request="<xml>" + 
    				"<optioncode>8109</optioncode>" + //请求方法code
    				"<TermNo>"+hisConfig.getAccount()+"</TermNo>" + //终端号
    				"<QuDao>"+hisConfig.getType()+"</QuDao>" + //渠道参数:默认空---询问医院
    				"<Pwd>"+hisConfig.getPwd()+"</Pwd>" + //医院内部就诊卡磁卡3道数据(必须填写)银行卡、身份证直接作为一卡通时不需要,M1：6位随机码
    				"<parameters>" + 
	    				"<Id4Card>"+json.getString("cardNo")+"</Id4Card>" + //卡号
    				"</parameters>" + 
    				"</xml>";
        	log.info("==============>一卡通—根据卡号获取当前在院病人押金条明细，请求his入参：{},"+request);
        	response=HisReqClientUtil.getHisResponseCXF(request,hisConfig.getHisUrl());
        	log.info("==============>一卡通—根据卡号获取当前在院病人押金条明细，请求his返参：{},",response);
        	if(response.getString("resultCode").equals("1")) {
        		jsonResult.put("code", 0);
        		jsonResult.put("msg", response.getString("resultDesc"));
        		jsonResult.put("data", response.getJSONObject("resultObjects"));
        	}else {
        		jsonResult.put("code", 1001);
        		jsonResult.put("msg", response.getString("resultDesc"));
        		jsonResult.put("data", "");
        	}
        } catch (Exception e) {
            e.printStackTrace();
            log.info("==============>一卡通—根据卡号获取当前在院病人押金条明细异常：{},",e);
            jsonResult.put("code", 1001);
    		jsonResult.put("msg", "接口异常");
    		jsonResult.put("data", "");
        }
		return jsonResult;
	}
		
	
	
}
