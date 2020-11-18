package com.jdm.hospital.mzjf.service.impl;


import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
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
import com.jdm.hospital.gh.service.GHService;
import com.jdm.hospital.mzjf.dao.MzjfDao;
import com.jdm.hospital.mzjf.service.MzjfService;
import com.jdm.hospital.utils.DecimalMoney;
import com.jdm.hospital.utils.HisReqClientUtil;
import com.jdm.hospital.utils.OrderCodeUtil;
import com.jdm.hospital.utils.WeChatTemplateMsgUtils;
import com.jdm.wechat.common.domain.WeChatUser;
import com.jdm.wechat.common.domain.WxConfig;

@Service
public class MzjfServiceImpl implements MzjfService {
	@Autowired
	MzjfDao mzjfDao;
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	@Autowired
	HisConfig hisConfig;
	@Autowired
	ComService comService;
	@Autowired
	CommonDao commonDao;
	@Autowired
	WeChatTemplateMsgUtils weChatTemplateMsgUtils;
	@Autowired
	WxConfig wxConfig;
	@Autowired
	GHService ghService;
	
	/**
	 * 获取缴费列表
	 */
	@Override
	public JSONObject queryJFList(JSONObject json, HttpServletRequest req, HttpServletResponse res) {
		JSONObject jsonResult=new JSONObject();
		JSONObject response=new JSONObject();
		Patient patient=(Patient) req.getSession().getAttribute("patient");
		//测试数据
		//Patient patient=new Patient();
		//patient.setCardNo("X2020020800001");
		try {
        	String request="<xml>" + 
    				"<optioncode>8009</optioncode>" + //请求方法code
    				"<TermNo>"+hisConfig.getAccount()+"</TermNo>" + //终端号
    				"<QuDao>"+hisConfig.getType()+"</QuDao>" + //渠道参数:默认空---询问医院
    				"<Pwd>"+hisConfig.getPwd()+"</Pwd>" + //医院内部就诊卡磁卡3道数据(必须填写)银行卡、身份证直接作为一卡通时不需要,M1：6位随机码
    				"<parameters>" + 
	    				"<CardNo>"+patient.getCardNo()+"</CardNo>" + //卡号patient.getCardNo()
	    				"<MyType>2</MyType>" + //1：只查乡医 2：全部  否则：只查本院医生(默认)
    				"</parameters>" + 
    				"</xml>";
        	log.info("==============>获取缴费列表，请求his入参：{},"+request);
        	response=HisReqClientUtil.getHisResponseCXF(request,hisConfig.getHisUrl());
        	
        	log.info("==============>获取缴费列表，请求his返参：{},",response);
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
            log.info("==============>获取缴费列表异常：{},",e);
            jsonResult.put("code", 1001);
    		jsonResult.put("msg", "获取缴费列表异常");
        }
		return jsonResult;
	}
	
	 
	/**
	 * 查询处方信息
	 */
	@Override
	public JSONObject queryPrescriptionDetailInfo(JSONObject json, HttpServletRequest req, HttpServletResponse res) {
		JSONObject jsonResult=new JSONObject();
		JSONObject response=new JSONObject();
		String itemStr="";
		try {
			JSONArray jsonArray = json.getJSONArray("data");
			for(int i=0;i<jsonArray.size();i++) {
				JSONObject object = (JSONObject) jsonArray.get(i);
				itemStr+="<Item>"+
							"<SqMzDocPatientId>"+object.getString("sqMzDocPatientId")+"</SqMzDocPatientId>"+
							"<SqMzDocPatientItemNum>"+object.getString("sqMzDocPatientItemNum")+"</SqMzDocPatientItemNum>" + //处方序号
							"<MyType>"+object.getString("myType")+"</MyType>" + //处方类型  0：处方， 1：化验，2：检查
						"</Item>";
			}
        	String request="<xml>" + 
    				"<optioncode>8010</optioncode>" + //请求方法code
    				"<TermNo>"+hisConfig.getAccount()+"</TermNo>" + //终端号
    				"<QuDao>"+hisConfig.getType()+"</QuDao>" + //渠道参数:默认空---询问医院
    				"<Pwd>"+hisConfig.getPwd()+"</Pwd>" + //医院内部就诊卡磁卡3道数据(必须填写)银行卡、身份证直接作为一卡通时不需要,M1：6位随机码
    				"<parameters>" +
    					"<SqFenyuan_Id>0</SqFenyuan_Id>" + //分院标识 0本院,空：按本院处理
    					 itemStr+//处方集合Item
	    			"</parameters>" + 
    				"</xml>";
        	log.info("==============>查询处方信息，请求his入参：{},"+request);
        	response=HisReqClientUtil.getHisResponseCXF(request,hisConfig.getHisUrl());
        	log.info("==============>查询处方信息，请求his返参：{},",response);
        	if(response.getString("resultCode").equals("1")) {
        		//1.session缓存Item节点，缴费
        		saveXml(request,req);
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
            log.info("==============>查询处方信息异常：{},",e);
            jsonResult.put("code", 1001);
    		jsonResult.put("msg", "查询处方信息异常");
        }
		return jsonResult;
	}

	@SuppressWarnings("rawtypes")
	private void saveXml(String request,HttpServletRequest req) {
		HttpSession session = req.getSession();
		//获取原生his返回的xml
		String item="";
		String xml = HisReqClientUtil.getXmlHisResponseCXF(request, hisConfig.getHisUrl());
		try {
			String prescriptionMoney ="";
			Document doc = DocumentHelper.parseText(xml);
			Element rootElt = doc.getRootElement(); // 获取根节点
			Iterator iterss = rootElt.elementIterator("resultObjects"); ///获取根节点下的子节点resultObjects            
            while (iterss.hasNext()) {
                Element recordEless = (Element) iterss.next();
                prescriptionMoney= recordEless.elementTextTrim("PrescriptionMoney");               
                Iterator itersElIterator = recordEless.elementIterator("Item"); // 获取子节点body下的子节点Item
                //遍历resultObjects节点下的Item节点
                while (itersElIterator.hasNext()) {
                    Element itemEle = (Element) itersElIterator.next();
                    String asXML = itemEle.asXML();
                    item+=asXML;
                }
            }
            session.setAttribute("item", item);//处方的Item
            session.setAttribute("prescriptionMoney", prescriptionMoney);//处方总金额
            log.info("============>session缓存要缴费Item字符串:{},处方总金额:{},",item,prescriptionMoney);
		} catch (DocumentException e) {
			e.printStackTrace();
		}
		
	}

	/**
	 * 提交处方，缴费
	 */
	@Override
	public JSONObject commitMZJF(JSONObject json, HttpServletRequest req, HttpServletResponse res) {
		JSONObject jsonResult=new JSONObject();
		JSONObject response=new JSONObject();
		HttpSession session = req.getSession();
		String item = (String)session.getAttribute("item");//处方Item
		String prescriptionMoney = (String) req.getSession().getAttribute("prescriptionMoney");//处方总金额		
		WeChatUser weChatUser=(WeChatUser) session.getAttribute("weChatUser");//微信用户
		Patient patient=(Patient) session.getAttribute("patient");//患者信息
		String path = (String) session.getAttribute("path");//请求路径
		
		log.info("==============>缴费item:{},处方总金额:,{}",item,prescriptionMoney);
		JSONObject object = (JSONObject) json.getJSONArray("data").get(0);
		String sqMzDocPatientId = object.getString("sqMzDocPatientId");
		String sqMzDocPatientItemNum =object.getString("sqMzDocPatientItemNum");
		String myType =object.getString("myType");
		
		if(item.isEmpty()) {
			jsonResult.put("code", 1001);
    		jsonResult.put("msg", "session无缴费item，缴费失败");
    		jsonResult.put("sjh", "");
    		jsonResult.put("url",wxConfig.getProjectUrl()+path);
			return jsonResult;
		}
		
		//======================1.保存缴费参数================================
		String orderCode = "zxgh"+OrderCodeUtil.createOrderCode();//订单号
		SimpleDateFormat  sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Map<String,Object> map=new HashMap<String, Object>();
		map.put("btype", "MZJF");
		map.put("params", item);
		map.put("orderCode", orderCode);
		map.put("createTime", sdf.format(new Date()));
		map.put("mzjfAmount", prescriptionMoney);
		map.put("jfjson", json);
		//2.订单号信息保存起来
		ghService.saveMzJfParams(map);
		
		JSONObject qr= comService.queryBalance(req, res);
		double balance = Double.parseDouble(qr.getJSONObject("data").getString("RemainMoney"));//余额
		double pm = Double.parseDouble(prescriptionMoney);//处方总金额
		if(balance<pm) {
			jsonResult.put("code", 0);
			jsonResult.put("msg", "余额不足");
			//余额不足，跳到充值页面
			jsonResult.put("sjh", "");
			jsonResult.put("url", wxConfig.getProjectUrl()+"/hospital/yjjcz/recharge?rechargeMoney="+DecimalMoney.decimalFormat(pm-balance)
									+"&path=mzjf"+"&type=MZJF"+"&orderCode="+orderCode
									+"&sqMzDocPatientId="+sqMzDocPatientId
									+"&sqMzDocPatientItemNum="+sqMzDocPatientItemNum
									+"&myType="+myType);
			return jsonResult;
		}
		
		
		
		//---------------测试数据--------------????????????
		//String cardNo = "X2020020800001";
		//String patientName = "鲁智深";
		try {
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
        	log.info("==============>门诊缴费，请求his入参：{},"+request);
        	response=HisReqClientUtil.getHisResponseCXF(request,hisConfig.getHisUrl());
        	log.info("==============>门诊缴费，请求his返参：{},",response);
        	if(response.getString("resultCode").equals("1")) {
        		response.put("prescriptionMoney", prescriptionMoney);//缴费总金额（发送模板消息用到）
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
            log.info("==============>门诊缴费异常：{},",e);
            jsonResult.put("code", 1001);
    		jsonResult.put("msg", "门诊缴费异常");
    		jsonResult.put("sjh", "");
    		jsonResult.put("url", wxConfig.getProjectUrl()+"/hospital/mzjf/jfList");
        }finally {
        	//1.保存门诊缴费记录
        	saveMZJFRecode(json,patient,prescriptionMoney,response,weChatUser);
			//2.清空缴费item和缴费总金额
			session.setAttribute("item", ""); 
			session.setAttribute("prescriptionMoney", ""); 
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
			log.info("==============>操作记录：{},",rc);
			commonDao.saveRecode(rc);
			//2.保存缴费记录
			//saveMZJFInfo(patient,response);
		} catch (Exception e) {
			log.error("==============>门诊缴费,发送消息模板异常============"+e);
			e.printStackTrace();
		}
	}

	
}



