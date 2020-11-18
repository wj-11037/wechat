package com.jdm.hospital.yjjcz.controller;


import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
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
import com.jdm.hospital.common.domain.WeChatOrder;
import com.jdm.hospital.utils.JudeUtil;
import com.jdm.hospital.utils.OrderCodeUtil;
import com.jdm.hospital.yjjcz.service.YjjczService;
import com.jdm.hospital.zycz.service.ZyService;
import com.jdm.wechat.common.domain.WeChatUser;
import com.jdm.wechat.common.domain.WxConfig;
import com.jdm.wechat.pay.controller.PayController;
import com.jdm.wechat.utils.HttpRequest;
import com.jdm.wechat.utils.MoneyUtil;

/**
 * @author Allen
 */
@Controller
@RequestMapping("hospital/yjjcz")
public class YjjczController {
	
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	@Autowired
	private JudeUtil judeUtil;
	@Autowired
	HisConfig hisConfig;
	@Autowired
	WxConfig wxconfig;
	@Autowired
	YjjczService yjjczService;
	@Autowired
	PayController payController;
	@Autowired
	ZyService zyService;
	
	
	
	//跳转到充值页面
	@RequestMapping(value = "recharge")
	public String goDoctorDetail(HttpServletRequest request,HttpServletResponse response) {
		
		return "yjjcz/recharge";
	}
	//跳转中转页面
	@RequestMapping(value = "transition")
	public String transition(HttpServletRequest request,HttpServletResponse response) {
		
		return "yjjcz/transition";
	}
	
	//跳转中转页面
	@RequestMapping(value = "wxpay")
	public String wxpay(HttpServletRequest request,HttpServletResponse response) {
		
		return "yjjcz/wxpay";
	}
	
	
	//公共请求入口
	@RequestMapping(value = "/service.do", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
	@ResponseBody
	public JSONObject playEntrance(@RequestBody String params,
			HttpServletRequest request,
			HttpServletResponse response){
		JSONObject jsonResult=new JSONObject();
		//1.将入参转为json
		JSONObject json =null;
		try {
			json =JSONObject.parseObject(params.trim());
		} catch (Exception e) {
			log.error("===============公共请求参数错误=====================");	
			jsonResult.put("msg", "请求参数错误");
			jsonResult.put("code", 9001);
			return jsonResult;
		}
		//2.验证参数合法性
		jsonResult =  judeUtil.judgeSign(json);
	    if(!jsonResult.getString("code").equals("0")) {
	    	log.info("===============请求验证失败==================");
	    	return jsonResult;
	     }
		//3.主方法
		jsonResult=mainMethod(request,response,json);
		log.info("===============公共请求返回前端：{},",jsonResult);
		return jsonResult;
	}
	
	
	/**
	 * 主方法
	 * @param request
	 * @param response
	 * @param json
	 * @return
	 */
	private JSONObject mainMethod(HttpServletRequest request,HttpServletResponse response, JSONObject json) {
		JSONObject jsonResult=new JSONObject();
		String method = json.getString("method");//方法 
		
		if(method.equals("1")) {
			//log.info("=============微信充值==========>请求参数：{},",json);	
			jsonResult=weChatRecharge(request,response,json);
		}else if(method.equals("2")) {
			//log.info("=============获取微信充值结果====>请求参数：{},"+json);
			jsonResult=getResultForRecharge(json,request,response);
		}else if(method.equals("3")) {
			//log.info("===============his预交金充值=======>请求参数：{},",json);	
			jsonResult=toYjjRecharge(request,response,json);
		}else {
			log.info("===============请求方法错误=========>请求参数：{}",json);
			jsonResult.put("code", 9001);
			jsonResult.put("msg", "请求方法错误");
			jsonResult.put("data", "");
		}
		return jsonResult;
	}
	
	/**
	 * 调用接口，实现微信充值
	 * @param request
	 * @param response
	 * @param json
	 * @return
	 */
	private JSONObject weChatRecharge(HttpServletRequest request, HttpServletResponse response, JSONObject json) {
		JSONObject jsonResult=new JSONObject();
		try {
			//调微信接口支付
			JSONObject wxPay = toWxPay(request,json);
			log.info("==========>wxPay:"+wxPay);
			if(wxPay.getString("code").equals("0")) {
				jsonResult.put("code", 0);
				jsonResult.put("msg", "成功");
				jsonResult.put("orderCode",wxPay.getString("orderCode"));//订单号
				jsonResult.put("amount",wxPay.getString("amount"));//订单金额
				jsonResult.put("jsonPay",wxPay.getString("jsonPay"));
				log.info("=======微信预支付成功：{},",jsonResult);
			}else {
				jsonResult.put("code", 1001);
				jsonResult.put("msg", wxPay.getString("msg"));
				log.info("=======微信预支付失败：{},",jsonResult);
			}
		} catch (Exception e) {
			e.printStackTrace();
			jsonResult.put("code", 1001);
			jsonResult.put("msg", "微信预支付接口异常"+e);
			log.info("=======微信预支付微信支付接口异常：{},",e);
		}
		return jsonResult;
	}
	
	
	
	/**
	 * 发起微信支付
	 * @param orderCode
	 * @param amount
	 * @param weChatUser 
	 */
	private JSONObject toWxPay(HttpServletRequest request, JSONObject json) {
		JSONObject jsonResult=new JSONObject();
		String orderCode=StringUtils.EMPTY;
		String amount=StringUtils.EMPTY;
		String notifyUrl=StringUtils.EMPTY;
		
		int payMoney=0;
		HttpSession session = request.getSession();
		try {
			String type = json.getString("type");
			if(StringUtils.isEmpty(type)) {
				jsonResult.put("code", 1001);
				jsonResult.put("msg", "请重新操作");
				log.info("=======订单类型为空：{},",json);
				return jsonResult;
			}
			if(type=="") {
				jsonResult.put("code", 1001);
				jsonResult.put("msg", "请重新操作");
				log.info("=======订单类型为空：{},",json);
				return jsonResult;
			}
			
			orderCode=json.getString("orderCode");//console.log("orderCode:"+orderCode);"zxgh"+OrderCodeUtil.createOrderCode();//1.生成微信支付订单号
			if(StringUtils.isEmpty(orderCode)) {
				orderCode = "zxgh"+OrderCodeUtil.createOrderCode();
				json.put("orderCode", orderCode);
				log.info("=======订单号为空，生成订单号：{},",orderCode);
			}
			log.info("=======发起微信支付json：{},",json);
			
			amount = json.getString("amount");//2.金额
			notifyUrl=wxconfig.getNotifUrl();//3.设置回到地址
			payMoney=MoneyUtil.getIntegerFromDoubleString(amount); //总金额转为分
			//=====支付金额，测试数据========？？？？？？？？？
			//payMoney=1;
			
			WeChatUser weChatUser=(WeChatUser) session.getAttribute("weChatUser");//
			Patient patient=(Patient) session.getAttribute("patient");//
			if(weChatUser==null||patient==null) {
				jsonResult.put("code", 1001);
				jsonResult.put("msg", "openid为空，支付失败");
				log.info("=======openid为空，支付失败：{},",jsonResult);
				return jsonResult;
			}
			
			//发送Post请求
			String url=wxconfig.getIoapPay()+"?payMoney="+payMoney+"&payOrderNo="+orderCode+"&openId="+weChatUser.getOpenid()+"&callBackUrl="+notifyUrl;
			String httpResult = HttpRequest.httpPostWithForm(url, new HashMap<String, String>());
			log.info("=======预支付请求结果：{},",httpResult);
			JSONObject parseObject = JSONObject.parseObject(httpResult);
			if(parseObject.getString("result").equals("true")) {
				//1.保存微信支付数据
				yjjczService.saveWeChatOrder(json,patient,weChatUser);
				String jsonPay = parseObject.getString("obj");//前端支付的json
				jsonResult.put("code", 0);
				jsonResult.put("msg", "预支付成功");
				jsonResult.put("orderCode", orderCode);
				jsonResult.put("amount", amount);
				jsonResult.put("jsonPay", jsonPay);
			}else {
				jsonResult.put("code", 1001);
				jsonResult.put("msg", "预支付失败");
			}
		} catch (Exception e) {
			e.printStackTrace();
			jsonResult.put("code", 1001);
			jsonResult.put("msg", "调用微信支付接口支付异常");
			log.info("=======调用微信支付接口支付异常：{},",e);
		}
		
		return jsonResult;
	}
	
	
	
	
	
	
	
	
	
	
	/**
	 * 获取充值结果
	 * @param json
	 * @param request
	 * @param response
	 * @return
	 */
	private JSONObject getResultForRecharge(JSONObject json, HttpServletRequest request, HttpServletResponse response) {
		JSONObject jsonResult=new JSONObject();	
		String url=StringUtils.EMPTY;
		try {
			String orderCode = json.getString("orderCode");//订单号
			String type = json.getString("type");//充值的类型
			String amountreq= json.getString("amount");
			WeChatUser weChatUser=(WeChatUser) request.getSession().getAttribute("weChatUser");
			
			
			//测试数据
			//WeChatUser weChatUser=new WeChatUser();
			//weChatUser.setId(1);
			if(weChatUser==null||orderCode=="") {
				jsonResult.put("code", 1001);
				jsonResult.put("msg", "wid或订单号为空，获取充值结果失败");
				return jsonResult;
			}
			//1.查询数据库订单号
			WeChatOrder  weChatOrder=yjjczService.queryWeChatOrderByOrderNoAndWid(orderCode,weChatUser.getId());
			if(weChatOrder==null) {
				jsonResult.put("code", 1001);
				jsonResult.put("msg", "订单号错误");
				log.info("===========获取充值结果失败,订单号错误,订单号：{},wid:{},",orderCode,weChatUser.getId());
				return jsonResult;
			}
			//2.调接口获取支付结果
			url=wxconfig.getIoapQueryPay()+"?payOrderNo="+orderCode;
			String sendGet = HttpRequest.sendGet(url);
			log.info("===========获取充值结果:{},",sendGet);
			JSONObject parseObject = JSONObject.parseObject(sendGet);
			//3.判断是否已支付成功
			if(parseObject.getString("result").equals("true")&&
			   parseObject.getJSONObject("data").getString("trade_state").equals("SUCCESS")) {			
				JSONObject jsonData = parseObject.getJSONObject("data");
				//double amount = (Double.parseDouble(jsonData.getString("total_fee")))/100;//总费用分转元				
				//测试数据金额-----？？？？正式环境要改为微信实际支付金额
				double amount=Double.parseDouble(json.getString("amount"));
				
				//1.验证签名
//				Map<String, String> params = JSONObject.parseObject(jsonData.toJSONString(), new TypeReference<Map<String, String>>(){});				
//				String generateSignature = Fd.generateSignature(params, "bdlxyywechatguahaozhifu");				
//				if(!(params.get("sign").equals(generateSignature))) {
//					jsonResult.put("code", 1001);
//					jsonResult.put("msg", "查询支付结果，签名错误");
//					log.info("===========查询支付结果，签名错误，订单号：{},",orderCode);
//					return jsonResult;
//				}
				//2.校验金额？？？？？？？？？？？？正式环境开启金额校验
//				if(!(weChatOrder.getAmount()==amount)) {
//					jsonResult.put("code", 1001);
//					jsonResult.put("msg", "支付金额与顶大金额不等");
//					log.info("===========支付金额与顶大金额不等，订单金额:{},查询金额:{}",weChatOrder.getAmount(),amount);
//					return jsonResult;
//				}
				
				//3.修改订单的订单状态，更新支付流水
				SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				yjjczService.updateOrderStatusByOrderNoAndWid(orderCode,weChatUser.getId(),sdf.format(new Date()),jsonData.getString("transaction_id"));				
				//jsonResult.put("code", 0);
				//jsonResult.put("msg", "查询支付结果成功");
				//jsonResult.put("orderCode", weChatOrder.getOrderCode());
				//jsonResult.put("amount", weChatOrder.getAmount());
				//4.判断类型调接口进行充值
				if(type.equals("yjjcz")) {
					JSONObject jsonRecharge=new JSONObject();
					jsonRecharge.put("orderCode", orderCode);
					jsonRecharge.put("amount", amount);
					log.info("===========获取微信充值结果成功，调预交金充值,订单号：{},订单金额：{},微信ID:{},",orderCode,amount,weChatOrder.getWid());
					//调预交金接口充值
					JSONObject recharge = yjjczService.toRecharge(request,response,jsonRecharge);					
					jsonResult.put("code", recharge.getString("code"));
					jsonResult.put("msg", recharge.getString("msg"));
					jsonResult.put("orderCode",orderCode);
					jsonResult.put("amount",amount);
				}else if (type.equals("zycz")){
					JSONObject jsonRecharge=new JSONObject();
					jsonRecharge.put("orderCode", orderCode);
					jsonRecharge.put("amount", amount);
					jsonRecharge.put("registerNoId", json.getString("registerNoId"));
					jsonRecharge.put("zyh", json.getString("zyh"));
					log.info("===========获取微信充值结果成功，调住院充值,订单号：{},订单金额：{},微信ID:{},",orderCode,amount,weChatOrder.getWid());
					JSONObject rechageZyyjByZYNo = zyService.rechageZyyjByZYNo(jsonRecharge, request, response);
					jsonResult.put("code", rechageZyyjByZYNo.getString("code"));
					jsonResult.put("msg", rechageZyyjByZYNo.getString("msg"));
					jsonResult.put("orderCode",orderCode);
					jsonResult.put("amount",amount);
				}
			}else {
				jsonResult.put("code", 1001);
				jsonResult.put("msg", parseObject.getJSONObject("data").getString("trade_state_desc"));
			}
		} catch (Exception e) {
			e.printStackTrace();
			jsonResult.put("code", 1001);
			jsonResult.put("msg", "获取微信充值结果异常");
			log.info("==========获取微信充值结果异常:{},",e);
		}
		return jsonResult;
	}


	
	
	
	//预交金充值
	private JSONObject toYjjRecharge(HttpServletRequest request, HttpServletResponse response, JSONObject json) {		
		return yjjczService.toRecharge(request,response,json);
	}


	public static void main(String[] args) {
		String orderCode=StringUtils.EMPTY;
		boolean empty = StringUtils.isEmpty(orderCode);
		System.out.println(empty);
	}
	
}
