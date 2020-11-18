package com.jdm.wechat.pay.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import com.alibaba.fastjson.JSONObject;
import com.jdm.hospital.common.domain.HisConfig;
import com.jdm.wechat.common.domain.WxConfig;
import com.jdm.wechat.utils.HttpRequest;
import com.jdm.wechat.utils.IPAddUtil;
import com.jdm.wechat.utils.MoneyUtil;
import com.jdm.wechat.utils.WeChatUtil;

@Controller
@RequestMapping(value = "/wxPay", method = {RequestMethod.GET,RequestMethod.POST})
public class PayController {
	
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	@Autowired
	WxConfig wxConfig; 
	@Autowired
	HisConfig hisconfig; 
	
	
	/**
	 * 公众号支付
	 * Title: JSAPI
	 * Description: 
	 * @param request
	 * @param requestMap
	 * @return
	 */
	@RequestMapping(value = "/jspApi.com", method = RequestMethod.GET)
    public void JSAPIPay(HttpServletRequest request,HttpServletResponse response) {
		String xmlStr=StringUtils.EMPTY;
		Map<String, String>  mapResult=new HashMap<String, String>();
		String orderCode=request.getParameter("orderCode");
		String openId=request.getParameter("open_id");
		String redirect_url=request.getParameter("redirect_url");
		String go_url=request.getParameter("go_url");
		String total_fee=request.getParameter("total_fee");
		
		try {
			total_fee=MoneyUtil.getIntegerFromDoubleString(total_fee)+"";
			//拼接下单地址参数
			Map<String, String> paraMap = new HashMap<String, String>();
			//获取请求ip地址
			String ip = IPAddUtil.getIP(request);
			paraMap.put("appid", wxConfig.getAppid()); //1.APPID 
			paraMap.put("mch_id", wxConfig.getMchid());//2.商户ID
			paraMap.put("nonce_str", WeChatUtil.generateNonceStr());//3.随机字符串  
			paraMap.put("body", "订单支付"); //4.商品描述
			paraMap.put("out_trade_no", orderCode);//5.商品的订单号每次要唯一
			paraMap.put("total_fee", total_fee);//总价
			paraMap.put("spbill_create_ip", ip);  
			//微信支付完成通知  
			paraMap.put("notify_url", wxConfig.getProjectUrl()+"/notify/complate.do");
			paraMap.put("trade_type", "JSAPI");//公众号支付
			paraMap.put("openid", openId);
			String sign = WeChatUtil.generateSignature(paraMap, wxConfig.getPaternerkey());
			paraMap.put("sign", sign);
			//1.统一下单，发送支付请求
			log.info("==========>微信支付的参数map:{},",paraMap);
			String xml= WeChatUtil.mapToXml(paraMap);
			log.info("==========>微信统一下单后，将返回的参数(map)转为xml格式:{},",xml);
			//2.发送post请求"统一下单接口"
			xmlStr= HttpRequest.sendPost("https://api.mch.weixin.qq.com/pay/unifiedorder", xml);			
			//3.将下单后的xml转为map===============
			Map<String, String> map = WeChatUtil.xmlToMap(xmlStr);
			delResult(map,response,mapResult,redirect_url,go_url);
		} catch (Exception e) {  
			log.info("================微信公众号支付异常==========="+e);
			mapResult.put("code","9001");
			mapResult.put("msg","支付异常");
	}
}
	
	
	

	/**
	   * 处理统一下单后的结果
	 * @param map
	 * @param response
	 * @param mapResult
	 * @param redirect_url
	 * @param go_url
	 */
    private void delResult(Map<String, String> map, HttpServletResponse response, Map<String, String> mapResult, String redirect_url, String go_url) {
    	try {
			if(map.get("result_code").equals("FAIL")) {
				log.info("================公众号支付失败===========");
				response.setCharacterEncoding("utf-8");       
				response.setContentType("text/html; charset=utf-8");
				PrintWriter out=response.getWriter(); 
				if(map.get("err_code").equals("ORDERPAID")) {
					String str= "<script type=\"text/javascript\" charset=\"utf-8\">" + 
								"	alert('"+map.get("err_code_des")+"');"+
								"	location.href='"+go_url+"';" + 
								"$(function () {"+
					                    "var isPageHide = false;"+
					                    "window.addEventListener('pageshow', function () {"+
					                       " if (isPageHide) {"+
					                           " window.location.reload();"+
					                        "}"+
					                   " });"+
					                    "window.addEventListener('pagehide', function () {"+
					                        "isPageHide = true;"+
					                    "});"+
					               " });"+
							    "</script>";
					out.println(str); 
					out.flush(); 
					out.close();
				}
			 }else {
				log.info("===============公众号支付成功执行js代码===========");
				mapResult= scriptEngineManager(map);
				response.setContentType("text/html;charset=utf-8");
				response.setCharacterEncoding("UTF-8");
				PrintWriter out=response.getWriter(); 
				JSONObject jsJson=new JSONObject();
				jsJson.put("appId", mapResult.get("appId"));//appid
				jsJson.put("timeStamp", mapResult.get("timeStamp"));//时间戳
				jsJson.put("nonceStr", mapResult.get("nonceStr"));//随机数
				jsJson.put("package", mapResult.get("package"));//"prepay_id="+prepay_id(接口返回的预支付订单)
				jsJson.put("signType", mapResult.get("signType"));//MD5
				jsJson.put("paySign", mapResult.get("paySign"));//签名
				log.info("=============调用微信JS api 支付:{},",jsJson);
				//WeixinJSBridge是微信浏览器对象，只能在微信中打开的网页才能使用
				String htmlStr="<script type=\"text/javascript\">" + 
						"//调用微信JS api 支付" + 
						"function jsApiCall()" + 
						"{" + 
						"   WeixinJSBridge.invoke(" + 
						"      'getBrandWCPayRequest',"+jsJson.toJSONString()+"," + 
						"      function(res){" + 
						"         //WeixinJSBridge.log(res.err_msg);" + 
						"          if(res.err_msg == \"get_brand_wcpay_request:ok\") {" + 
						"             location.href='"+go_url+"';" + 
						"          }else{" + 
						"            alert(res.err_code+res.err_desc+res.err_msg);" + 
						"             location.href='"+redirect_url+"';" + 
						"          }" + 
						"      }" + 
						"   );" + 
						"}" + 
						"" + 
						"function callpay()" +  
						"{" + 
						"   if (typeof WeixinJSBridge == \"undefined\"){" + 
						"       if( document.addEventListener ){" + 
						"           document.addEventListener('WeixinJSBridgeReady', jsApiCall, false);" + 
						"       }else if (document.attachEvent){" + 
						"           document.attachEvent('WeixinJSBridgeReady', jsApiCall);" + 
						"           document.attachEvent('onWeixinJSBridgeReady', jsApiCall);" + 
						"       }" + 
						"   }else{" + 
						"       jsApiCall();" + 
						"   }" + 
						"}" + 
						"callpay();" + 
						"window.onpageshow = function(event) {"+
						    "if (event.persisted) {"+
						      "window.location.reload()"+
						    "}"+
						  "};"+
						"</script>";
				out.println(htmlStr); 
				out.flush(); 
				out.close();
			 }
		} catch (IOException e) {
			e.printStackTrace();
		}
	}




    /**
            * 返回前端json
     * @param map
     * @return
     */
	private Map<String, String> scriptEngineManager(Map<String, String> map) {
		Map<String,String>  paySignMap =new HashMap<String,String>();
		String paySign = "";
		try {
				paySignMap.put("appId", (String) map.get("appid"));//1.APPID
				paySignMap.put("timeStamp", WeChatUtil.getCurrentTimestamp()+"");//2.时间戳
				paySignMap.put("nonceStr", WeChatUtil.generateNonceStr());//3.随机字符串
				paySignMap.put("package", "prepay_id="+(String)map.get("prepay_id"));//4.prepay_id预支付订单号
				paySignMap.put("signType", "MD5");//5.加密类型-MD5
				paySign=WeChatUtil.generateSignature(paySignMap, wxConfig.getPaternerkey());//生成签名
		} catch (Exception e) {
			e.printStackTrace();
			log.info("=============微信公众号执行js代码异常:{},",e);
		}
		paySignMap.put("paySign", paySign);//6.签名
		log.info("==============>微信公众号执行签名参数：{},签名:{},",paySignMap.toString(),paySign);
        return paySignMap;
	}
	
	
    
}




