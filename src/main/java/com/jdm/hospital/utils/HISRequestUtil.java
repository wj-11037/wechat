package com.jdm.hospital.utils;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Iterator;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 向HIS发起请求
 *@author Allen
 *@date 2019年10月31日 上午8:37:38
 */
public class HISRequestUtil {
	static int socketTimeout = 30000;// 请求超时时间
    static int connectTimeout = 30000;// 传输超时时间
    private final static Logger log = LoggerFactory.getLogger(new HISRequestUtil().getClass());
	
	
    /*
     * HttpURLConnection
     */
	public static String sendPostByHttpUrlConnection(String urlstr, String soapXml,String soapaction) {
			String retStr = "";
			try {
				URL url= new URL(urlstr);
				HttpURLConnection conn = (HttpURLConnection)url.openConnection();
				//设置请求格式
				conn.setRequestProperty("Content-Type", "text/xml;charset=UTF-8");
				//conn.setRequest
				//设置从服务器端读取，输出数据
				conn.setDoInput(true);
				conn.setDoOutput(true);
				OutputStream outputStream = conn.getOutputStream();
				outputStream.write(soapXml.getBytes());
				outputStream.close();
				if(conn.getResponseCode()==200) {
					InputStream inputStream = conn.getInputStream();
					BufferedReader br=new BufferedReader(new InputStreamReader(inputStream));
					String line =null;
					StringBuffer sb=new StringBuffer();
					while ((line=br.readLine())!=null) {
						sb.append(line);
					}
					System.out.println("his返回："+sb.toString());
					br.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			
		  return retStr;
	 }
	
	
	/**
	 * soap 发送请求()
	 *@author Allen
	 *@date 2019年10月31日 下午2:10:10 
	 *@param url
	 *@param content
	 *@return
	 *@throws ClientProtocolException
	 *@throws IOException
	 */
	public static final String soapActionRequest(String url, String content,String code)
			throws ClientProtocolException, IOException {
		String retStr = StringUtils.EMPTY;
		// 创建HttpClientBuilder
		HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
		// HttpClient
		CloseableHttpClient closeableHttpClient = httpClientBuilder.build();
		HttpPost httpPost = new HttpPost(url);
		// 设置请求和传输超时时间
		RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(socketTimeout).setConnectTimeout(connectTimeout)
				.build();
		httpPost.setConfig(requestConfig);
		try {
			httpPost.setHeader("Content-Type", "text/xml;charset=UTF-8");
			//httpPost.setHeader("SOAPAction", parseSoapAction(content));
			httpPost.setHeader("SOAPAction", "http://tempuri.org/"+code);
			StringEntity data = new StringEntity(content, Charset.forName("UTF-8"));
			httpPost.setEntity(data);
			
			CloseableHttpResponse response = closeableHttpClient.execute(httpPost);
			HttpEntity httpEntity = response.getEntity();
			if (httpEntity != null) {
				retStr = EntityUtils.toString(httpEntity, "UTF-8");
			}
		} finally {
			closeableHttpClient.close();
		}
		return retStr;
	}
	
	
	/**
	 * 原始请求封装
	 *@author Allen
	 *@date 2019年12月28日 下午5:24:46 
	 *@param url
	 *@param content
	 *@return
	 *@throws ClientProtocolException
	 *@throws IOException
	 */
	public static final String soapActionRequest2(String url, String content)
			throws ClientProtocolException, IOException {
		String retStr = StringUtils.EMPTY;
		// 创建HttpClientBuilder
		HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
		// HttpClient
		CloseableHttpClient closeableHttpClient = httpClientBuilder.build();
		HttpPost httpPost = new HttpPost(url);
		// 设置请求和传输超时时间
		RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(socketTimeout).setConnectTimeout(connectTimeout)
				.build();
		httpPost.setConfig(requestConfig);
		try {
			httpPost.setHeader("Content-Type", "text/xml;charset=UTF-8");
			httpPost.setHeader("SOAPAction", parseSoapAction(content));
			StringEntity data = new StringEntity(content, Charset.forName("UTF-8"));
			httpPost.setEntity(data);
			CloseableHttpResponse response = closeableHttpClient.execute(httpPost);
			HttpEntity httpEntity = response.getEntity();
			if (httpEntity != null) {
				retStr = EntityUtils.toString(httpEntity, "UTF-8");
			}
		} finally {
			closeableHttpClient.close();
		}
		return retStr;
	}

	
	private static String parseSoapAction(String content) {
		String soapAction = StringUtils.EMPTY;
		try {
			Document document = DocumentHelper.parseText(content);
			Element root = document.getRootElement();
			soapAction = parseRootNode(root);
		} catch (DocumentException e) {
			log.error("HttpUtils parseSoapAction Exception, content is {}.", content);
		}
		return soapAction;
	}
	
	private static String parseRootNode(Element root) {
		String soapAction = StringUtils.EMPTY;
		Iterator<Element> rootChildrens = root.elementIterator();
		while (rootChildrens.hasNext()) {
			Element rootChildren = rootChildrens.next();
			soapAction = parseBodyNode(rootChildren);
		}
		return soapAction;
	}
	
	private static String parseBodyNode(Element rootChildren) {
		String soapAction = StringUtils.EMPTY;
		if (rootChildren.getName().equalsIgnoreCase("Body")) {
			Iterator<Element> bodyChildrens = rootChildren.elementIterator();
			if (bodyChildrens.hasNext()) {
				Element bodyChildren = bodyChildrens.next();
				Attribute attribute = bodyChildren.attribute("soapAction");
				if (attribute != null) {
					soapAction = attribute.getValue();
				}
			}
		}
		return soapAction;
	}
	
//	public static void main(String[] args) {
//        try {
//        	String request="<xml>" + 
//    				"  <optioncode>8005</optioncode> " + 
//    				"  <TermNo>马静</TermNo> " + 
//    				"  <QuDao>掌上</QuDao> " + 
//    				"  <Pwd>wjt20151124</Pwd> " + 
//    				"  <parameters>" + 
//    				"    <CardNo>342623199408013816</CardNo> " + 
//    				"    <CardNoType>2</CardNoType>" + 
//    				"  </parameters>" + 
//    				"</xml>";
//            // 接口地址
//            String address = "http://www.bdlxyy.cn:8050/TranslateHisWebservice.asmx?wsdl";
//            // 代理工厂
//            JaxWsProxyFactoryBean jaxWsProxyFactoryBean = new JaxWsProxyFactoryBean();
//            // 设置代理地址
//            jaxWsProxyFactoryBean.setAddress(address);
//            // 设置接口类型
//            jaxWsProxyFactoryBean.setServiceClass(TranslateHisWebservice.class);
//            // 创建一个代理接口实现
//            jaxWsProxyFactoryBean.create();
//            // 数据准备
//           
//            // 调用代理接口的方法调用并返回结果
//            String result = us.getHello(request);
//            System.out.println("返回结果:" + result);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
	
	public static void main(String[] args) {
		String url="http://www.bdlxyy.cn:8050/TranslateHisWebservice.asmx";
		String request="<?xml version=\"1.0\" encoding=\"utf-8\"?>" + 
				"<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
				+ "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" "
				+ "xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">" + 
				"<soap:Body>" + 
				"<MyCzzxWeb xmlns=\"http://tempuri.org/\">" + 
					"<Request>"+ 
							"<xml>" + 
								"<optioncode>8005</optioncode>" + 
								"<TermNo>马静</TermNo>" + 
								"<QuDao>掌上</QuDao>" + 
								"<Pwd>wjt20151124</Pwd>" + 
								"<parameters>" + 
								"<CardNo>43312719880810801X</CardNo>" + 
								"<CardNoType>2</CardNoType>" + 
								"</parameters>" + 
							"</xml>"+
					"</Request>" + 
				"</MyCzzxWeb>" + 
				"</soap:Body>" + 
				"</soap:Envelope>";
		String sendPostByHttpUrlConnection = sendPostByHttpUrlConnection(url, request, "");
		System.out.println("sendPostByHttpUrlConnection====>"+sendPostByHttpUrlConnection);
	}
}
