package com.jdm.hospital.utils;


import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson.JSONObject;
import com.jdm.client.Service1Soap;
import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;

/**
 * 将HIS客服端导入本地的方式
 * 请求工具类
 * @author Allen
 *
 */

@Component
public class HisReqClientUtil {
	
	
	private static final Logger log = LoggerFactory.getLogger(HisReqClientUtil.class);
	
	/**
	 * wsdl导入webservice客服端，通过CXF方式调用
	 * @param request
	 * @param url
	 * @return
	 */
	public static  JSONObject getHisResponseCXF(String request,String url) {
		JSONObject response=new JSONObject();
		try {
			//1.创建WebService客户端代理工厂
			JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
			//2.设置 webservice地址
			factory.setAddress(url);
			//4.获得接口的实现类
			//TranslateHisWebserviceSoap create = factory.create(TranslateHisWebserviceSoap.class);
			
			Service1Soap create = factory.create(Service1Soap.class);
			
			String result = create.myCzzxWeb(request);
			response = JSONObject.parseObject(XmlJsonUtils.xmlToJson(result));
		} catch (Exception e) {
			e.printStackTrace();
			log.error("=============cxf调用his接口异常:{},",e);
			response.put("resultCode", 1001);
			response.put("resultDesc", "his接口异常");
		}
		return response;
	}
	
	
	public static  String getXmlHisResponseCXF(String request,String url) {
		String response="";
		try {
			//1.创建WebService客户端代理工厂
			JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
			//2.设置 webservice地址
			factory.setAddress(url);
			//4.获得接口的实现类
			//TranslateHisWebserviceSoap create = factory.create(TranslateHisWebserviceSoap.class);
			Service1Soap create = factory.create(Service1Soap.class);
			
			response = create.myCzzxWeb(request);
		} catch (Exception e) {
			e.printStackTrace();
			log.error("=============cxf调用his接口异常:{},",e);
			response="<xml>" + 
						"<resultCode>1001</resultCode>" + 
						"<resultDesc>失败</resultDesc>"+
					 "<xml>" ;
		}
		return response;
	}
	
	
	
	/**
	 * wsimport方式导入his客服端
	   * 用到的包
	 *  import java.net.MalformedURLException;
	 *  import java.net.URL;
	 *  import javax.xml.namespace.QName;
	 *  import javax.xml.ws.Service;
	 *  import org.tempuri.TranslateHisWebservice;
	 * @param request
	 * @param url
	 * @return
	 */
	public static  JSONObject getHisResponseImport(String request,String url) {
		JSONObject response=new JSONObject();
		URL urlstr=null;
		try {
			urlstr = new URL(url);
			log.error("=============wsimport调用his接口地址:{},",urlstr);
			QName qName=new QName("http://tempuri.org/", "TranslateHisWebservice");
			Service service=Service.create(urlstr, qName);
			Service1Soap his = service.getPort(new QName("http://tempuri.org/", "Service1Soap"),Service1Soap.class);			
			String result = his.myCzzxWeb(request);
			response = JSONObject.parseObject(XmlJsonUtils.xmlToJson(result));
		} catch (MalformedURLException e) {
			e.printStackTrace();
			log.error("=============wsimport调用his接口异常:{},",e);
			response.put("resultCode", 1001);
			response.put("resultDesc", "his接口异常");
		}
		
		return response;
	}
	
	public static void main(String[] args) {
		String request="<xml>" + 
				"  <optioncode>8005</optioncode> " + //请求方法code
				"  <TermNo>马静</TermNo> " + //终端号
				"  <QuDao>掌上</QuDao> " + //渠道参数:默认空---询问医院
				"  <Pwd>wjt20151124</Pwd> " + //医院内部就诊卡磁卡3道数据(必须填写)银行卡、身份证直接作为一卡通时不需要,M1：6位随机码
				"  <parameters>" + 
				"    <CardNo>X2020020800001</CardNo> " + //卡号(身份证号码)
				"    <CardNoType>2</CardNoType>" + //卡类型
				"  </parameters>" + 
				"</xml>";
    	//3.先根据用户的信息查询his有误记录，http://www.bdlxyy.cn:8050/TranslateHisWebservice.asmx?wsdl
    	JSONObject hisResponseImport = HisReqClientUtil.getHisResponseCXF(request,"http://192.168.0.18:8012/Service1.asmx?wsdl");
    	//JSONObject hisResponseImport = HisReqClientUtil.getHisResponseImport(request, "http://www.bdlxyy.cn:8050/TranslateHisWebservice.asmx?wsdl");
    	System.out.println("hisResponseImport=="+hisResponseImport);
	}
}




