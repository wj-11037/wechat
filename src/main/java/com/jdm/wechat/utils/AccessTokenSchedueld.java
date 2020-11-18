package com.jdm.wechat.utils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.jdm.wechat.common.dao.WxUserDao;
import com.jdm.wechat.common.domain.WxConfig;
import net.sf.json.JSONObject;

@Component
public class AccessTokenSchedueld {
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	@Autowired 
	WxUserDao wxUserDao;
	@Autowired 
	WxConfig wxconfig;
	
	//@Scheduled(cron="0 0 */2 * * ?")
	//@Scheduled(cron="0 */1 * * * ?") 
	/**
	 * 每2小时获取一次accesstoken
	 *cron：seconds minutes hours day month week year
	 */
	@Scheduled(cron="0 0 */1 * * ?")    
	public void getAccessToken(){ 
		log.error("=========定时任务获取accesstoken===============");
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String updateTime=sdf.format(new Date());
		
		JSONObject wxtoken=null;
				
		String url="https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=APPID&secret=APPSECRET";		
		try {
			//1.获取微信的AccessToken
			String sendGet = HttpRequest.sendGet(url.replace("APPID", wxconfig.getAppid()).replace("APPSECRET", wxconfig.getAppsecert()));
			wxtoken=JSONObject.fromObject(sendGet);
			log.info("=========>定时任务获取accesstoken成功：{}，",wxtoken.getString("access_token"));
			if(wxUserDao.updateAccessToken(wxtoken.getString("access_token"), updateTime)>0) {
				log.info("=========定时任务，更新accesstoken成功================");
			}else {
				log.info("=========定时任务，更新accesstoken失败===============");
			}
		} catch (IOException e) {
			log.error("=========定时任务，更新accesstoken异常==============="+e);
			e.printStackTrace();
		}
		
		
	}
	
}






