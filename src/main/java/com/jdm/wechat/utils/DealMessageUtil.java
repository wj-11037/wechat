package com.jdm.wechat.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class DealMessageUtil {

	
	//文本消息
	public static Map<String, String> dealTextMsg(Map<String, String> reqMsg) {
		Map<String, String> resMap=new HashMap<String, String>();
		resMap.put("ToUserName", reqMsg.get("FromUserName"));
		resMap.put("FromUserName", reqMsg.get("ToUserName"));
		resMap.put("CreateTime", System.currentTimeMillis()+"");
		resMap.put("MsgType", "text");
		String content = reqMsg.get("Content");
		if(content.contains("你妈")||content.contains("尼玛")||content.contains("fuck")||content.contains("傻逼")) {
			resMap.put("Content", "请使用文明用语！");
		}else {
			resMap.put("Content", "感谢关注蠡县医院公众号,我们将竭诚为您服务！");
		}
		
		resMap.put("MsgId", randomMsg());
		return resMap;
	}
	
	
	private static String randomMsg() {
		StringBuilder sb=new StringBuilder();
		for(int i=0;i<16;i++) {
			sb.append(new Random().nextInt(10)+1);
		}
		
		return sb+"";
	}

	//图片消息
	public static Map<String, String> dealImageMsg(Map<String, String> reqMsg) {
		Map<String, String> resMap=new HashMap<String, String>();
		resMap.put("ToUserName", reqMsg.get("FromUserName"));
		resMap.put("FromUserName", reqMsg.get("ToUserName"));
		resMap.put("CreateTime", System.currentTimeMillis()+"");
		resMap.put("MsgType", "image");
		resMap.put("PicUrl", "");
		resMap.put("MediaId", reqMsg.get("MediaId"));
		resMap.put("MsgId", randomMsg());
		return resMap;
	}

	//处理关注与取消关注
	public static Map<String, String> dealEventMsg(Map<String, String> reqMsg) {
		Map<String, String> resMap=new HashMap<String, String>();
		resMap.put("ToUserName", reqMsg.get("FromUserName"));
		resMap.put("FromUserName", reqMsg.get("ToUserName"));
		resMap.put("CreateTime", System.currentTimeMillis()+"");
		String event = reqMsg.get("Event");
		resMap.put("MsgType", "text");
		if(event.equals("subscribe")) {//关注
			resMap.put("Content", "欢迎关注蠡县医院公众号，我们将竭诚为您服务！");
		}
		resMap.put("MsgId", randomMsg());
		return resMap;
	}
}	
