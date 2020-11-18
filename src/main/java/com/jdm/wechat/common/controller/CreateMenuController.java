package com.jdm.wechat.common.controller;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.jdm.hospital.common.domain.HisConfig;
import com.jdm.wechat.common.domain.WxConfig;
import com.jdm.wechat.utils.HttpRequest;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 创建菜单
 *@author Allen
 *@date 2019年10月28日 上午9:19:46
 */
@Controller
@RequestMapping("/createMenu")
public class CreateMenuController {
	private static Logger log = LoggerFactory.getLogger(CreateMenuController.class);
	@Autowired
	WxConfig wxConfig;
	@Autowired
	HisConfig hisconfig;
	
	@RequestMapping("/yes")
	@ResponseBody
	public  String createMenu() {
				JSONObject json=new JSONObject();
				JSONArray jsa=new JSONArray();//装一级菜单的数组
				//================菜单一==============
				JSONObject firstMenu=new JSONObject();//菜单一
				JSONArray firstArr=new JSONArray();//一级菜单链接
				firstMenu.put("name", "医院信息");
				//子菜单1
				JSONObject firstArr_one=new JSONObject();
				firstArr_one.put("type", "view");
				firstArr_one.put("name", "医院简介");
				firstArr_one.put("url", wxConfig.getProjectUrl()+"/page/hos_desc");
				//子菜单2
				JSONObject firstArr_two=new JSONObject();
				firstArr_two.put("type", "view");
				firstArr_two.put("name", "科室简介");
				firstArr_two.put("url", wxConfig.getProjectUrl()+"/page/dept_desc");
				//子菜单3
				JSONObject firstArr_three=new JSONObject();
				firstArr_three.put("type", "view");
				firstArr_three.put("name", "新闻动态");
				firstArr_three.put("url", wxConfig.getProjectUrl()+"/page/news");
				//1.将子菜单链接添加到数组
				firstArr.add(firstArr_three);
				firstArr.add(firstArr_one);
				firstArr.add(firstArr_two);
				//2.添加一级菜单下的子菜单数组
				firstMenu.put("sub_button", firstArr);
				
				//================菜单二==============
				JSONObject secendMenu=new JSONObject();//菜单一
				
				secendMenu.put("name", "自助服务");
				secendMenu.put("type", "view");
				secendMenu.put("name", "自助服务");
				secendMenu.put("url", wxConfig.getProjectUrl()+"/common/index");
				
				secendMenu.put("sub_button", secendMenu);
				
				//最后添加主菜单的json
				//将菜单一，二，三加入json
				jsa.add(firstMenu);
				jsa.add(secendMenu);
				
				
				json.put("button", jsa);
				log.info("json="+json);
				
				JSONObject wxtoken=null;
				String url="https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=APPID&secret=APPSECRET";
				String urlToken="https://api.weixin.qq.com/cgi-bin/menu/create?access_token=ACCESS_TOKEN";//创建菜单
				try {
					//1.获取微信的AccessToken
					String sendGet = HttpRequest.sendGet(url.replace("APPID", wxConfig.getAppid()).replace("APPSECRET", wxConfig.getAppsecert()));
					wxtoken=JSONObject.fromObject(sendGet);
					System.out.println("access_token="+wxtoken.getString("access_token"));
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				urlToken=urlToken.replace("ACCESS_TOKEN", wxtoken.getString("access_token"));
				String doPostStr=null;
				try {
					//发送创建菜单
					doPostStr = HttpRequest.sendPost(urlToken,json.toString());
				} catch (Exception e) {
					e.printStackTrace();
				}
				log.info("创建菜单:{},",doPostStr);
				
				return doPostStr;
			}
		
	
	
	
}


