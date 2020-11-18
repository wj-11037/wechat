package com.jdm.hospital.utils;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.jdm.wechat.common.domain.WxConfig;
import com.jdm.wechat.utils.WeChatUtil;

public class SignJsonUtil {
	
	
	/**
	 * 生成前端微信支付的json
	 * @param prepay_id
	 * @param wxconfig
	 */
	public static JSONObject generateSignature(String prepay_id, WxConfig wxconfig) {
		JSONObject jsonResult=new JSONObject();
		Map<String,String>  paySignMap =new HashMap<String,String>();
		String paySign = "";
		try {
				paySignMap.put("appId", wxconfig.getAppid());//1.APPID
				paySignMap.put("timeStamp", WeChatUtil.getCurrentTimestamp()+"");//2.时间戳
				paySignMap.put("nonceStr", WeChatUtil.generateNonceStr());//3.随机字符串
				paySignMap.put("package", "prepay_id="+prepay_id);//4.prepay_id预支付订单号
				paySignMap.put("signType", "MD5");//5.加密类型-MD5
				paySign=WeChatUtil.generateSignature(paySignMap, wxconfig.getPaternerkey());//生成签名
				paySignMap.put("paySign", paySign);//6.签名
				//拼装返回json
				JSONObject data=new JSONObject();
				data.put("appId", wxconfig.getAppid());//appid
				data.put("timeStamp", paySignMap.get("timeStamp"));//时间戳
				data.put("nonceStr", paySignMap.get("nonceStr"));//随机数
				data.put("package", paySignMap.get("package"));//"prepay_id="+prepay_id(接口返回的预支付订单)
				data.put("signType", paySignMap.get("signType"));//MD5
				data.put("paySign", paySignMap.get("paySign"));//签名
				jsonResult.put("code", 0);
				jsonResult.put("msg", "获取json成功");
				jsonResult.put("data", data);
		} catch (Exception e) {
			e.printStackTrace();
			jsonResult.put("code", 1001);//appid
			jsonResult.put("msg", e);//appid
			jsonResult.put("data", new JSONObject());
		}
		
		return jsonResult;	
	}
	
}
