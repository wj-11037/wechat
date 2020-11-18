package com.jdm.hospital.gh.controller;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.collections.map.HashedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import com.alibaba.fastjson.JSONObject;
import com.jdm.hospital.common.domain.HisConfig;
import com.jdm.hospital.common.domain.Patient;
import com.jdm.hospital.common.service.ComService;
import com.jdm.hospital.gh.domain.GHInfo;
import com.jdm.hospital.gh.service.GHService;
import com.jdm.hospital.utils.DecimalMoney;
import com.jdm.hospital.utils.JudeUtil;
import com.jdm.hospital.utils.OrderCodeUtil;
import com.jdm.wechat.common.domain.WxConfig;

@Controller
@RequestMapping("hospital/gh")
public class GHController{
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	@Autowired
	public GHService ghService;
	@Autowired
	JudeUtil judeUtil;
	@Autowired
	ComService comService;
	@Autowired
	HisConfig hisConfig;
	@Autowired
	WxConfig wxConfig;
	
	//跳到科室列表页面
	@RequestMapping(value = "deptList")
	public String goToindex(HttpServletRequest request,HttpServletResponse response) {
		return "gh/department";
	}
	
	//跳转到医生的列表
	@RequestMapping(value = "doctorList")
	public String goDoctorList(HttpServletRequest request,HttpServletResponse response) {
		return "gh/doctorList";
	}
	
	//跳转到医生详情页面
	@RequestMapping(value = "doctorDetail")
	public String goDoctorDetail(HttpServletRequest request,HttpServletResponse response) {
		return "gh/doctor";
	}
	
	//跳转支付页面
	@RequestMapping(value = "gotopay")
	public String gotoPayPage(HttpServletRequest request,HttpServletResponse response) { 
		return "gh/pay";
	}
	
	
	//跳转到挂号详情页面
	@RequestMapping(value = "ghDetail")
	public String getGHDetail(HttpServletRequest request,HttpServletResponse response) {
		return "gh/detail";
	}
	
	
	//跳转到挂号详情页面
	@RequestMapping(value = "order")
	public String goOrder(HttpServletRequest request,HttpServletResponse response) {
		return "gh/order";
	}
	
	//跳转到挂号详情页面
	@RequestMapping(value = "recharge")
	public String gotoRecharge(HttpServletRequest request,HttpServletResponse response) {
		
		return "gh/recharge";
	}
	
	//跳转挂号中转页面
	@RequestMapping(value = "transition")
	public String gototransition(HttpServletRequest request,HttpServletResponse response) {
		
		
		return "gh/transition";
	}
	
	
	//选择挂号缴费页面
	@RequestMapping("chooseGhType")
	public String chooseGhType() {
		
		return "chooseGhType";
	}
	
	
	
	
	
	
	
	/**
	 * 挂号请求入口
	 * @param params
	 * @return
	 */
	@RequestMapping(value = "service.do",method = RequestMethod.POST,produces="application/json;charset=UTF-8")
	@ResponseBody
	public JSONObject ghService(@RequestBody String params,HttpServletRequest request,HttpServletResponse response) {
		 
		JSONObject jsonResult=new JSONObject();
		JSONObject json =null;
		try {
			json =JSONObject.parseObject(params.trim());
		} catch (Exception e) {
			jsonResult.put("code","1");
			jsonResult.put("msg", "请求参数异常");
			log.error("请求参数异常{}",jsonResult);	
			return jsonResult;
		}
		 
		//2.验证参数合法性
		jsonResult =  judeUtil.judgeSign(json);
	    if(!jsonResult.getString("code").equals("0")) {
	    	log.info("===============公众号请求验证失败==================");
	    	return jsonResult;
	    }
		//3.主方法
	    jsonResult=mainMethod(json,request,response);
	    log.info("===============返回前端：{},",jsonResult);
		return jsonResult;
	}

	
	/**
	 * 挂号--主方法
	 * @param json
	 * @param response 
	 * @param request 
	 * @return
	 */
	private JSONObject mainMethod(JSONObject json, HttpServletRequest request,
			HttpServletResponse response) {
		JSONObject jsonResult=new JSONObject();
		String method = json.getString("method");//方法 
		
			if(method.equals("1")){
				//log.info("=====获取科室列表=====>请求参数：{},"+json);
				jsonResult=deptList(json,request,response);
			}else if(method.equals("2")){
				//log.info("=====获取医生列表=====>请求参数：{},"+json);
				jsonResult=getdoctorList(json,request,response);
			}else if(method.equals("3")){
				//log.info("=====获取医生详情=====>请求参数：{},"+json);
				jsonResult=getdoctorDetail(json,request,response);
			}else if(method.equals("4")){
				//log.info("=====提交挂号=====>请求参数：{},"+json);
				jsonResult=commitGH(json,request,response);
			}else if(method.equals("5")){
				//log.info("=====获取挂号信息====>请求参数：{},"+json);
				jsonResult=getGHInfo(json,request,response);
			}else if(method.equals("6")){
				//log.info("=====获取挂号历史记录====>请求参数：{},"+json);
				jsonResult=getGHRrcorde(json,request,response);
			}else if(method.equals("7")){
				//log.info("=====获取当前挂号信息====>请求参数：{},"+json);
				jsonResult=getCurrentGH(json,request,response);
			}else {
				log.info("===============请求方法错误================》请求参数：{}",json);
				jsonResult.put("code", 9001);
				jsonResult.put("msg", "请求方法错误");
			}
		
		return jsonResult;
	}
	

	/**
	 * 获取当前挂号信息
	 * @param json
	 * @param request
	 * @param response
	 * @return
	 */
	private JSONObject getCurrentGH(JSONObject json, HttpServletRequest request, HttpServletResponse response) {
		JSONObject jsonResult=new JSONObject();
		jsonResult=ghService.getCurrentGH(json,request,response);
		return jsonResult;
	}

	//获取挂号信息
	private JSONObject getGHInfo(JSONObject json, HttpServletRequest request, HttpServletResponse response) {
		JSONObject jsonResult=new JSONObject();
		try {
			HttpSession session = request.getSession();
			GHInfo ghinfo = (GHInfo) session.getAttribute("ghinfo");
			JSONObject jo=new JSONObject();
			jo.put("amount", ghinfo.getAmount());//总金额
			jo.put("deptId", ghinfo.getDeptId());//科室ID
			jo.put("deptName", ghinfo.getDeptName());//科室名称
			jo.put("ghType", ghinfo.getGhType());//挂号类型
			jo.put("ghTypeId",ghinfo.getGhTypeId());//挂号类型编码
			jo.put("doctorId", ghinfo.getDoctorId());//医生ID
			jo.put("doctorName",ghinfo.getDoctorName());//医生姓名
			jo.put("period", ghinfo.getPeriod());//时段
			jo.put("userName", ghinfo.getUserName());//患者姓名
			jo.put("cardNo",ghinfo.getCardNo());//患者卡号
			//jo.put("condate",ghinfo.getCondate());//预约日期
			jsonResult.put("code", 0);//总金额
			jsonResult.put("msg", "获取挂号信息成功");//总金额
			jsonResult.put("data", jo);//
		} catch (Exception e) {
			e.printStackTrace();
			jsonResult.put("code", 1001);//总金额
			jsonResult.put("msg", "获取挂号信息失败");//总金额
			jsonResult.put("data", "");//
			log.info("============从session中获取挂号信息异常============");
		}
		return jsonResult;
	}

	/**
	 * 获取医生详情
	 * @param json
	 * @param request
	 * @param response
	 * @return
	 */
	private JSONObject getdoctorDetail(JSONObject json, HttpServletRequest request, HttpServletResponse response) {
		
		return null;
	}

	/**
	 * 获取挂号记录
	 *@author Allen
	 *@date 2020年1月7日 下午6:43:05 
	 *@param json
	 *@param request
	 *@param response
	 *@return
	 */
	private JSONObject getGHRrcorde(JSONObject json, HttpServletRequest request, HttpServletResponse response) {
		JSONObject jsonResult=new JSONObject();
		jsonResult=ghService.getGHRrcorde(json,request,response);
		
		return jsonResult;
	}

	/**
	 * 提交挂号
	 * @param json
	 * @param request
	 * @param response
	 * @return
	 */
	private JSONObject commitGH(JSONObject json, HttpServletRequest request, HttpServletResponse response) {
		JSONObject jsonResult=new JSONObject();
		try {
			HttpSession session = request.getSession();
			//1.先查询卡余额
			JSONObject qr= comService.queryBalance(request, response);
			if(!qr.getString("code").equals("0")) {
				log.info("======提交挂号，查询余额失败:{},",qr);
				jsonResult.put("code", qr.getString("code"));
				jsonResult.put("msg", qr.getString("msg"));
				session.setAttribute("ghinfo", "");//清空挂号信息
				jsonResult.put("url", wxConfig.getProjectUrl()+"/hospital/gh/deptList");
				return jsonResult;
			}
			//2.保存挂号信息session---ghinfo
			saveGHInfoToSession(json,request);
			//3.判断余额
			double balance = Double.parseDouble(qr.getJSONObject("data").getString("RemainMoney"));//余额
			double amount = Double.parseDouble(json.getString("amount"));//挂号总金额
			if(balance<amount) {
				 //余额不足，跳到充值页面
				double rechargeMoney = DecimalMoney.decimalFormat(amount-balance);
				log.info("=======================>>>余额不足,rechargeMoney:{},",rechargeMoney);
				GHInfo ghinfo = (GHInfo) session.getAttribute("ghinfo");
				jsonResult.put("code", 0);
				jsonResult.put("msg", "余额不足");
				jsonResult.put("url", wxConfig.getProjectUrl()+"/hospital/yjjcz/recharge?rechargeMoney="+
								rechargeMoney+"&path=gh&type=GH"+"&orderCode="+ghinfo.getOrderCode());
				return jsonResult;
			}else {
				jsonResult=ghService.commitGH(json,request,response);
			}
			
			
		} catch (NumberFormatException e) {
			e.printStackTrace();
			log.info("=======提交挂号异常========");
		}
		
		return jsonResult;
	}
	
	//将用户提交的挂号订单信息保存到session中
	private void saveGHInfoToSession(JSONObject json, HttpServletRequest request) {
		try {
			SimpleDateFormat  sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			HttpSession session = request.getSession();
			Patient patient=(Patient) session.getAttribute("patient");
			GHInfo ghinfo=new GHInfo();
			ghinfo.setAmount(json.getString("amount"));
			ghinfo.setDeptId(json.getString("deptId"));
			ghinfo.setDeptName(json.getString("deptName"));
			ghinfo.setDoctorId(json.getString("doctorId"));
			ghinfo.setDoctorName(json.getString("doctorName"));
			ghinfo.setGhType(json.getString("ghType"));
			ghinfo.setGhTypeId(json.getString("ghTypeId"));
			ghinfo.setPeriod(json.getString("period"));
			ghinfo.setCardNo(patient.getCardNo());//卡号
			ghinfo.setUserName(patient.getUserName());//姓名
			ghinfo.setIdCardNo(patient.getIdCardNo());//身份证号码
			ghinfo.setWid(patient.getWid());
			ghinfo.setPid(patient.getId());
			String orderCode = "zxgh"+OrderCodeUtil.createOrderCode();//订单号
			ghinfo.setOrderCode(orderCode);
			//1.将挂号信息存入session
			session.setAttribute("ghinfo", ghinfo);
			Map<String,Object> map=new HashMap<String, Object>();
			map.put("btype", "GH");
			map.put("params", JSONObject.toJSONString(ghinfo));
			map.put("orderCode", orderCode);
			map.put("createTime", sdf.format(new Date()));
			//2.订单号信息保存起来
			ghService.saveParams(map);
			
			log.info("========挂号信息存session成功:{},",ghinfo);
		} catch (Exception e) {
			e.printStackTrace();
			log.error("========挂号信息存session异常=======");
		}
	}

	/**
	 * 获取医生列表
	 * @param json
	 * @param request
	 * @param response
	 * @return
	 */
	private JSONObject getdoctorList(JSONObject json, HttpServletRequest request, HttpServletResponse response) {
		JSONObject jsonResult=new JSONObject();
		jsonResult=ghService.getdoctorList(json,request,response);
		return jsonResult;
	}
	
	
	/**
	 * 获取科室列表
	 * @param json
	 * @param request
	 * @param response
	 * @return
	 */
	private JSONObject deptList(JSONObject json,HttpServletRequest request, HttpServletResponse response) {
		JSONObject jsonResult=new JSONObject();
		jsonResult=ghService.getDeptList(json,request,response);
		return jsonResult;
	}

	
	
	public static void main(String[] args) {
		GHInfo ghinfo=new GHInfo();
		ghinfo.setAmount("1");
		ghinfo.setDeptId("deptId");
		ghinfo.setDeptName("deptName");
		ghinfo.setDoctorId("doctorId");
		ghinfo.setDoctorName("doctorName");
		ghinfo.setGhType("ghType");
		ghinfo.setGhTypeId("ghTypeId");
		ghinfo.setPeriod("period");
		ghinfo.setCardNo("卡号");//卡号
		ghinfo.setUserName("姓名");//姓名
		ghinfo.setIdCardNo("身份证号");//身份证号码
		ghinfo.setWid(1);
		ghinfo.setPid(1);
		String orderCode = OrderCodeUtil.createOrderCode();//订单号
		ghinfo.setOrderCode(orderCode);
		
		System.out.println(JSONObject.toJSONString(ghinfo));
		String jsonStr="{\"amount\":\"1\",\"cardNo\":\"卡号\",\"deptId\":\"deptId\",\"deptName\":\"deptName\",\"doctorId\":\"doctorId\",\"doctorName\":\"doctorName\",\"ghType\":\"ghType\",\"ghTypeId\":\"ghTypeId\",\"idCardNo\":\"身份证号\",\"orderCode\":\"202008201522122772\",\"period\":\"period\",\"pid\":1,\"userName\":\"姓名\",\"wid\":1}\r\n" + 
				"";
		GHInfo parse =  JSONObject.parseObject(jsonStr , GHInfo.class);
		System.out.println(parse.getCardNo());
		
	}
	
	
}
