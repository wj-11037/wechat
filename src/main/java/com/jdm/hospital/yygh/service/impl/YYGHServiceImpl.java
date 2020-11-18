package com.jdm.hospital.yygh.service.impl;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import com.jdm.hospital.gh.dao.GHDao;
import com.jdm.hospital.gh.domain.GHInfo;
import com.jdm.hospital.utils.HisReqClientUtil;
import com.jdm.hospital.utils.OrderCodeUtil;
import com.jdm.hospital.utils.WeChatTemplateMsgUtils;
import com.jdm.hospital.yygh.dao.YYGHDao;
import com.jdm.hospital.yygh.service.YYGHService;
import com.jdm.wechat.common.domain.WeChatUser;
import com.jdm.wechat.common.domain.WxConfig;


@Service
public class YYGHServiceImpl implements YYGHService {
	@Autowired
	YYGHDao yyghDao;
	@Autowired
	GHDao ghdao;
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	@Autowired
	HisConfig hisConfig;
	@Autowired
	CommonDao commonDao;
	@Autowired
	WeChatTemplateMsgUtils weChatTemplateMsgUtils;
	@Autowired
	WxConfig wxConfig;
	/**
	 * 预约挂号，获取科室列表
	 */
	@Override
	public JSONObject getDeptList(JSONObject json, HttpServletRequest req, HttpServletResponse res) {
			JSONObject jsonResult=new JSONObject();
			JSONObject response=new JSONObject();
			try {
	        	String request="<xml>" + 
	    				"<optioncode>8006</optioncode>" + //请求方法code
	    				"<TermNo>"+hisConfig.getAccount()+"</TermNo>" + //终端号
	    				"<QuDao>"+hisConfig.getType()+"</QuDao>" + //渠道参数:默认空---询问医院
	    				"<Pwd>"+hisConfig.getPwd()+"</Pwd>" + //医院内部就诊卡磁卡3道数据(必须填写)银行卡、身份证直接作为一卡通时不需要,M1：6位随机码
	    				"<parameters>" + 
		    				"<ZhuanYeCode/>" + //专业代码
	    				"</parameters>" + 
	    				"</xml>";
	        	log.info("==============>预约挂号，获取科室列表，his入参：{},"+request);
	        	response=HisReqClientUtil.getHisResponseCXF(request,hisConfig.getHisUrl());
	        	log.info("==============>预约挂号，获取科室列表，his返参：{},",response);
	        	if(response.getString("resultCode").equals("1")) {
	        		
	        		jsonResult.put("code", 0);
	        		jsonResult.put("msg", response.getString("resultDesc"));
	        		jsonResult.put("data", response.getJSONObject("resultObjects"));
	        	}else {
	        		jsonResult.put("code", response.getString("resultCode"));
	        		jsonResult.put("msg", response.getString("resultDesc"));
	        		jsonResult.put("data", "");
	        	}
	        } catch (Exception e) {
	            e.printStackTrace();
	            log.info("==============>预约挂号，获取科室列表异常：{},",e);
	            jsonResult.put("code", response.getString("resultCode"));
	    		jsonResult.put("msg", response.getString("resultDesc"));
	        }
				
			return jsonResult;
		}
	

	/**
	 * 获取医生列表
	 */
	@Override
	public JSONObject getdoctorList(JSONObject json, HttpServletRequest req, HttpServletResponse res) {
		JSONObject jsonResult=new JSONObject();
		JSONObject response=new JSONObject();
		
		String time = json.getString("time").substring(6, 8);//预约的时段
		
		try {
			
			if(Integer.parseInt(time)<=12) {
				time="上午";
			}else {
				time="下午";
			}
			
			
        	String request="<xml>" + 
    				"<optioncode>8007</optioncode>" + //请求方法code
    				"<TermNo>"+hisConfig.getAccount()+"</TermNo>" + //终端号
    				"<QuDao>"+hisConfig.getType()+"</QuDao>" + //渠道参数:默认空---询问医院
    				"<Pwd>"+hisConfig.getPwd()+"</Pwd>" + //医院内部就诊卡磁卡3道数据(必须填写)银行卡、身份证直接作为一卡通时不需要,M1：6位随机码
    				"<parameters>" + 
	    				"<Type>1</Type>" + //挂号类型	int	0：当日号  1：挂预约号
	    				"<Date>"+json.getString("condate")+"</Date>" + //预约挂号日期：yyyy-mm-dd	当日：不需要
	    				"<DepartmentsID>"+json.getString("deptId")+"</DepartmentsID>" + //
    				"</parameters>" + 
    				"</xml>";
        	
        	log.info("==============>预约挂号，请求his入参：{},"+request);
        	response=HisReqClientUtil.getHisResponseCXF(request,hisConfig.getHisUrl());
        	log.info("==============>预约挂号，请求his返参：{},",response);
        	if(response.getString("resultCode").equals("1")) {
        		jsonResult.put("code", 0);
        		jsonResult.put("msg", response.getString("resultDesc"));
        		//计算剩余号源
        		JSONObject ph = calcuLatePiaoHao(time,json.getString("deptId"));
        		jsonResult.put("sw", ph.getString("sw"));
        		jsonResult.put("xw", ph.getString("xw"));
        		//替换前的json
        		JSONObject jsonObject = response.getJSONObject("resultObjects");
        		//将持证的替换
        		JSONObject jsonReplace=new JSONObject();//替换后的json
        		Iterator<Entry<String, Object>> iter = jsonObject.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry<String, Object> entry = (Map.Entry<String, Object>) iter.next();
                    String key = entry.getKey().toString();
                    String value = entry.getValue().toString();
                    if(value.contains("持证")) {//过滤持证的
                    	continue;
                    }
                    if(json.getString("deptId").equals("0EC")) {//过滤核酸检测下午的号源
                    	if(value.contains("下午")) {
                        	continue;
                        }
                    }
                    if(!value.contains(time)) {
                    	continue;
                    }
                    jsonReplace.put(key, JSONObject.parseObject(value));
                }
        		jsonResult.put("data",jsonReplace);
        	}else {
        		jsonResult.put("code", response.getString("resultCode"));
        		jsonResult.put("msg", response.getString("resultDesc"));
        		jsonResult.put("data", new JSONArray());
        	}
        } catch (Exception e) {
            e.printStackTrace();
            log.info("==============>预约挂号，获取医生列表异常：{},",e);
            jsonResult.put("code", response.getString("resultCode"));
    		jsonResult.put("msg", "获取医生列表异常");
        }
		return jsonResult;
	}
	
	private JSONObject calcuLatePiaoHao(String time, String deptId) {
		JSONObject jr=new JSONObject();
		List<GHInfo>  list=null;
		try {
			SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Map<String,Object> map=new HashMap<String, Object>();
			map.put("actionType", "YYGH");//挂号类型
			
			if(time.equals("上午")) {
				map.put("startTime", sdf.format(new Date()).substring(0, 10)+" 00 :00:00");//开始时间
			}else {
				map.put("startTime", sdf.format(new Date())+" 12:00:00");//开始时间
			}
			
			map.put("endTime", sdf.format(new Date()));//结束时间
			map.put("deptId", deptId);//科室ID
			
			list=commonDao.getPiaoHao(map);
			log.info("查询时间:{},list:{},",map.get("startTime")+"--"+map.get("endTime"),list.size());
			if(time.equals("上午")) {//上午
				jr.put("sw", Integer.parseInt(hisConfig.getSumPiaoHao())-list.size());
				jr.put("xw", Integer.parseInt(hisConfig.getSumPiaoHao()));
			}else {
				jr.put("sw", Integer.parseInt(hisConfig.getSumPiaoHao()));
				jr.put("xw", Integer.parseInt(hisConfig.getSumPiaoHao())-list.size());
			}
		} catch (Exception e) {
			log.error("获取剩余号源异常:{},",e);
			jr.put("sw", Integer.parseInt(hisConfig.getSumPiaoHao()));
			jr.put("xw", Integer.parseInt(hisConfig.getSumPiaoHao()));
		}
		return jr;
	}
	
	/**
	 * 预约挂号，提交挂号
	 */
	@Override
	public JSONObject commitYYGH(JSONObject json, HttpServletRequest req, HttpServletResponse res) {
		JSONObject jsonResult=new JSONObject();
		JSONObject response=new JSONObject();
		String path ="";
		HttpSession session=null;
		GHInfo ghinfo =null;
		WeChatUser weChatUser=null;
		try {
			session= req.getSession();
			ghinfo= (GHInfo) session.getAttribute("ghinfo");//获取挂号信息
			path= (String) session.getAttribute("path");
			weChatUser=(WeChatUser) session.getAttribute("weChatUser");
			String orderCode = OrderCodeUtil.createOrderCode();//订单号
			ghinfo.setOrderCode(orderCode);
			
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
        	
        	log.info("==============>预约挂号，提交挂号，his入参：{},"+request);
        	response=HisReqClientUtil.getHisResponseCXF(request,hisConfig.getHisUrl());
        	log.info("==============>预约挂号，提交挂号his，返参：{},",response);
        	
        	if(response.getString("resultCode").equals("1")) {
        		//.预约挂号模板消息
        		weChatTemplateMsgUtils.sendYYGHTemp(json,ghinfo,weChatUser.getOpenid(),response);
        		jsonResult.put("code", 0);
        		jsonResult.put("msg", response.getString("resultDesc"));
        		jsonResult.put("url",wxConfig.getProjectUrl()+"/hospital/yygh/ghDetail?order_id="+orderCode+"&time="+json.getString("time")); 
        	}else {
        		jsonResult.put("code", response.getString("resultCode"));
        		jsonResult.put("msg", response.getString("resultDesc"));
        		jsonResult.put("url", wxConfig.getProjectUrl()+path);
        	}
        } catch (Exception e) {
            e.printStackTrace();
            log.info("==============>预约挂号，提交挂号异常：{},",e);
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
	 




/**
 * 保存挂号信息，发送模板消息
 * @param json
 * @param response
 * @param weChatUser
 * @param patient 
 */
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
		log.info("==============>操作记录：{},",rc);
		commonDao.saveRecode(rc);
		//2.保存预约挂号记录
		saveYYGHInfo(ghinfo,response);
	} catch (Exception e) {
		log.error("==============>保存预约挂号,发送消息模板异常============"+e);
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
 * 查询预约挂号信息
 */
@Override
public JSONObject queryYYGHInfoByOrderCode(JSONObject json, HttpServletRequest request, HttpServletResponse response) {
	JSONObject jsonResult=new JSONObject();
	try {
		GHInfo  ghinfo=ghdao.queryGHInfoByOrderCode(json.getString("order_id"));
		JSONObject jo=new JSONObject();
		jo.put("actionType", ghinfo.getActionType().equals("GH")?"挂号":"预约挂号");
		jo.put("amount", ghinfo.getAmount());
		jo.put("deptId", ghinfo.getDeptId());
		jo.put("ghId", ghinfo.getGhId());
		jo.put("ordercode", ghinfo.getOrderCode());
		jo.put("orderStatus", ghinfo.getOrderStatus().equals(0)?"未就诊":"已就诊");
		jo.put("deptName", ghinfo.getDeptName());
		jo.put("ghType", ghinfo.getGhType());
		jo.put("ghTypeId", ghinfo.getGhTypeId());
		jo.put("doctorId", ghinfo.getDoctorId());
		jo.put("doctorName", ghinfo.getDoctorName());
		jo.put("period", ghinfo.getPeriod());
		jo.put("userName", ghinfo.getUserName());
		jo.put("cardNo", ghinfo.getCardNo());
		jo.put("idCardNo", ghinfo.getIdCardNo());
		jo.put("condate", ghinfo.getCondate());
		jo.put("createTime", ghinfo.getCreateTime());
		jsonResult.put("code", 0);
		jsonResult.put("msg", "获取预约挂号信息成功");
		jsonResult.put("data", jo);
	} catch (Exception e) {
		e.printStackTrace();
		jsonResult.put("code", 1001);
		jsonResult.put("msg", "获取预约挂号信息失败");
		jsonResult.put("data", "");
		log.info("==============>获取预约挂号信息失败========"+e);
	}
	return jsonResult;
  }

	/**
	 * 获取预约挂号记录
	 */
	@Override
	public JSONObject getYYGHRrcorde(JSONObject json, HttpServletRequest req, HttpServletResponse res) {
		JSONObject jsonResult=new JSONObject();
		JSONObject response=new JSONObject();
		try {
        	String request="<xml>" + 
    				"<optioncode>8021</optioncode>" + //请求方法code
    				"<TermNo>"+hisConfig.getAccount()+"</TermNo>" + //终端号
    				"<QuDao>"+hisConfig.getType()+"</QuDao>" + //渠道参数:默认空---询问医院
    				"<Pwd>"+hisConfig.getPwd()+"</Pwd>" + //医院内部就诊卡磁卡3道数据(必须填写)银行卡、身份证直接作为一卡通时不需要,M1：6位随机码
    				"<parameters>" + 
	    				"<CardNo>"+json.getString("cardNo")+"</CardNo>" + //卡号
    				"</parameters>" + 
    				"</xml>";
        	log.info("==============>获取预约挂号信息，请求his入参：{},"+request);
        	response=HisReqClientUtil.getHisResponseCXF(request,hisConfig.getHisUrl());
        	log.info("==============>获取预约挂号信息，请求his返参：{},",response);
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
            log.info("==============>获取预约挂号详情：{},",e);
            jsonResult.put("code", 1001);
    		jsonResult.put("msg", response.getString("resultDesc"));
        }
		return jsonResult;
	}
	
	/**
	 * 获取当前预约挂号信息
	 */
	@Override
	public JSONObject getCurrentYYGH(JSONObject json, HttpServletRequest request, HttpServletResponse response) {
		JSONObject jsonResult=new JSONObject();
		try {
			GHInfo  ghinfo=ghdao.queryGHInfoByOrderCode(json.getString("order_id"));
			JSONObject jo=new JSONObject();
			jo.put("piaoHao", piaoHao(ghinfo.getPiaoHao(),ghinfo.getPeriod()));
			jo.put("actionType", ghinfo.getActionType().equals("GH")?"挂号":"预约挂号");
			jo.put("amount", ghinfo.getAmount());
			jo.put("deptId", ghinfo.getDeptId());
			jo.put("ghId", ghinfo.getGhId());
			jo.put("ordercode", ghinfo.getOrderCode());
			jo.put("orderStatus", ghinfo.getOrderStatus().equals(0)?"未就诊":"已就诊");
			jo.put("deptName", ghinfo.getDeptName());
			jo.put("ghType", ghinfo.getGhType());
			jo.put("ghTypeId", ghinfo.getGhTypeId());
			jo.put("doctorId", ghinfo.getDoctorId());
			jo.put("doctorName", ghinfo.getDoctorName());
			jo.put("period", ghinfo.getPeriod());
			jo.put("userName", ghinfo.getUserName());
			jo.put("cardNo", ghinfo.getCardNo());
			jo.put("idCardNo", ghinfo.getIdCardNo());
			jo.put("condate", ghinfo.getCondate());
			jo.put("createTime", ghinfo.getCreateTime());
			
			jsonResult.put("code", 0);
			jsonResult.put("msg", "获取当前预约挂号信息成功");
			jsonResult.put("data", jo);
		} catch (Exception e) {
			e.printStackTrace();
			jsonResult.put("code", 1001);
			jsonResult.put("msg", "获取当前预约挂号信息失败");
			jsonResult.put("data", "");
			log.info("==============>获取预约挂号信息失败========"+e);
		}
		return jsonResult;
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
	 * 预约挂号退号
	 */
	@Override
	public JSONObject refundYYGH(JSONObject json, HttpServletRequest req, HttpServletResponse res) {
		JSONObject jsonResult=new JSONObject();
		JSONObject response=new JSONObject();
		String orderCode = json.getString("order_id");//订单号
		//1.先查询该订单信息
		GHInfo  ghinfo=ghdao.queryGHInfoByOrderCode(orderCode);
		Patient patient = (Patient) req.getSession().getAttribute("patient");
		if(ghinfo==null||patient==null) {
			log.info("==============>预约挂号退号失败===============");
			jsonResult.put("code", 0);
    		jsonResult.put("msg", "预约挂号退号失败");
			return jsonResult;
		}
		try {
        	String request="<xml>" + 
    				"<optioncode>8022</optioncode>" + //请求方法code
    				"<TermNo>"+hisConfig.getAccount()+"</TermNo>" + //终端号
    				"<QuDao>"+hisConfig.getType()+"</QuDao>" + //渠道参数:默认空---询问医院
    				"<Pwd>"+hisConfig.getPwd()+"</Pwd>" + //医院内部就诊卡磁卡3道数据(必须填写)银行卡、身份证直接作为一卡通时不需要,M1：6位随机码
    				"<parameters>" + 
	    				"<Id>"+ghinfo.getGhId()+"</Id>" + //挂号单据号
	    				"<Num>"+ghinfo.getPiaoHao()+"</Num>" + //票号
	    				"<MyDate>"+ghinfo.getCondate()+"</MyDate>" + //就诊日期
	    				"<Card_Id>"+patient.getPatientId()+"</Card_Id>" + //一卡通内部号
	    				"<Card_Id4Card>"+ghinfo.getCardNo()+"</Card_Id4Card>" + //一卡通卡号
	    				"<Departments>"+ghinfo.getDeptName()+"</Departments>" + //科室
	    				"<DepartmentsId>"+ghinfo.getDeptId()+"</DepartmentsId>" + //科室ID
	    				"<GhTypeId>"+ghinfo.getGhTypeId()+"</GhTypeId>" + //挂号类型
	    				"<GhType>"+ghinfo.getGhType()+"</GhType>" + //挂号类型
	    				"<DoctorId>"+ghinfo.getDoctorId()+"</DoctorId>" + //医生Id
	    				"<Doctor>"+ghinfo.getDoctorName()+"</Doctor>" + //医生
	    				"<Period>"+ghinfo.getPeriod()+"</Period>" + //时段
	    				"<MyMoney>"+ghinfo.getAmount()+"</MyMoney>" + //金额
    				"</parameters>" + 
    				"</xml>";
        	log.info("==============>预约挂号退号，请求his入参：{},"+request);
        	response=HisReqClientUtil.getHisResponseCXF(request,hisConfig.getHisUrl());
        	log.info("==============>预约挂号退号，请求his返参：{},",response);
        	if(response.getString("resultCode").equals("1")) {
        		jsonResult.put("code", 0);
        		jsonResult.put("msg", "预约挂号退号成功");
        	}else {
        		jsonResult.put("code", 1001);
        		jsonResult.put("msg", response.getString("resultDesc"));
        	}
        } catch (Exception e) {
            e.printStackTrace();
            log.info("==============>预约挂号退号异常：{},",e);
            jsonResult.put("code", 1001);
    		jsonResult.put("msg", response.getString("resultDesc"));
        }
		return jsonResult;
	}
}








