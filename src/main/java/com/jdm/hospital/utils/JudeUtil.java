package com.jdm.hospital.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson.JSONObject;
import com.jdm.hospital.common.dao.CommonDao;
import com.jdm.hospital.common.domain.AccountClient;


@Component
public class JudeUtil {
	protected static final Logger log = LoggerFactory.getLogger(JudeUtil.class);
	@Autowired
	public  CommonDao commonDao;
	
	public  JSONObject judgeSign(JSONObject json) {
		
			JSONObject jsonResult=new JSONObject();
			String accountid ="";//账号
			String requestSign="";//请求的签名字符串
			String timeStamp="";//请求的时间戳
			long timeStamPNow=0l;//系统当前时间戳
			boolean token=false;
			AccountClient account=null;
			accountid =json.getString("accountid");
			
			//1.判断账号id
			if(accountid.isEmpty()) {
				jsonResult.put("code", "1");
				jsonResult.put("msg", "账号不能为空");
				return jsonResult;
			 }
			
			//2.判断账号是否存在
			account=commonDao.queryAccountClientINFO(accountid);
			if(account==null) {
				jsonResult.put("code", "1");
				jsonResult.put("msg", "账号不存在或被禁用");
				return jsonResult;
			}
			
			requestSign = json.getString("sign");
			timeStamp = json.getString("timeStamp");
			timeStamPNow = System.currentTimeMillis()/1000;
			Long time = (Long.valueOf(timeStamPNow) - Long.valueOf(timeStamp)) / (60);
			//3.请求的时间戳时间大于30分钟，则为非法请求
			if(time>30) {
				jsonResult.put("msg", "请求超时");
				jsonResult.put("code", 9001);
				return jsonResult; 
			}
			
			String md5Parse = MD5Utils.Md5ToString(account.getAccount()+account.getPassword()+account.getKey()+timeStamp);					
			token = requestSign.equals(md5Parse);
			//4.验证签名
			if(token) {
				jsonResult.put("code", "0");
				jsonResult.put("msg", "签名正确");
			}else {
				jsonResult.put("code", "1");
				jsonResult.put("msg", "签名错误");
			}
			
			return jsonResult;
		  }
}

