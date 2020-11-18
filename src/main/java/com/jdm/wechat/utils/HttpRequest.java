package com.jdm.wechat.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

public class HttpRequest {
	
	
	
	/**
	 * 以form表单形式提交数据，发送post请求
	 * @explain 
	 *   1.请求头：httppost.setHeader("Content-Type","application/x-www-form-urlencoded")
	 *   2.提交的数据格式：key1=value1&key2=value2...
	 * @param url 请求地址
	 * @param paramsMap 具体数据
	 * @return 服务器返回数据
	 */
	public static String httpPostWithForm(String url,Map<String, String> paramsMap){
	    // 用于接收返回的结果
	    String resultData ="";
	     try {
	            HttpPost post = new HttpPost(url);
	            List<BasicNameValuePair> pairList = new ArrayList<BasicNameValuePair>();
	            // 迭代Map-->取出key,value放到BasicNameValuePair对象中-->添加到list中
	            for (String key : paramsMap.keySet()) {
	                pairList.add(new BasicNameValuePair(key, paramsMap.get(key)));
	            }
	            UrlEncodedFormEntity uefe = new UrlEncodedFormEntity(pairList, "utf-8");
	            post.setEntity(uefe); 
	            // 创建一个http客户端
	            CloseableHttpClient httpClient = HttpClientBuilder.create().build();
	            // 发送post请求
	            HttpResponse response = httpClient.execute(post);
	            
	            // 状态码为：200
	            if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
	                // 返回数据：
	                resultData = EntityUtils.toString(response.getEntity(),"UTF-8");
	            }else{
	                throw new RuntimeException("接口连接失败！");
	            }
	        } catch (Exception e) {
	            throw new RuntimeException("接口连接失败！");
	        }
	     return resultData;
	}
	
	
	
	
	/**
	 * 流Post
	 *@author Allen
	 *@date 2019年9月1日 上午11:24:28 
	 *@param url
	 *@param param
	 *@return
	 */
	   public static String sendPost(String url, Object param) {
	        PrintWriter out = null;
	        BufferedReader in = null;
	        String result = "";
	        try {
	            URL realUrl = new URL(url);
	            // 打开和URL之间的连接
	            URLConnection conn = realUrl.openConnection();
	            // 设置通用的请求属性 注意Authorization生成
	            // conn.setRequestProperty("Content-Type",
	            // "application/x-www-form-urlencoded");
	            // 发送POST请求必须设置如下两行
	            conn.setDoOutput(true);
	            conn.setDoInput(true);
	            // 获取URLConnection对象对应的输出流
	            out = new PrintWriter(new OutputStreamWriter(conn.getOutputStream(),"utf-8"));
	            // 发送请求参数
	            out.print(param);
	            // flush输出流的缓冲
	            out.flush();
	            // 定义BufferedReader输入流来读取URL的响应
	            in = new BufferedReader(
	                    new InputStreamReader(conn.getInputStream(),"utf-8"));
	            String line;
	            while ((line = in.readLine()) != null) {
	                result += line;
	            }
	            System.out.println(result);
	        } catch (Exception e) {
	            System.out.println("发送 POST 请求出现异常！" + e);
	            e.printStackTrace();
	        }
	        // 使用finally块来关闭输出流、输入流
	        finally {
	            try {
	                if (out != null) {
	                    out.close();
	                }
	                if (in != null) {
	                    in.close();
	                }
	            } catch (IOException ex) {
	                ex.printStackTrace();
	            }
	        }
	        
	        return result;
		}
	   
	/**
	 * 流Get
	 *@author Allen
	 *@date 2019年9月1日 上午11:24:37 
	 *@param urlStr
	 *@return
	 *@throws IOException
	 */
	public static String sendGet(String urlStr) throws IOException {
        URL url = new URL(urlStr);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // 返回结果-字节输入流转换成字符输入流，控制台输出字符
        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(),"UTF-8"));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
		return sb.toString();
    }
	
	public static String sendPostJson(String strURL, String params) {
      BufferedReader reader = null;
      try {
          URL url = new URL(strURL);// 创建连接
          HttpURLConnection connection = (HttpURLConnection) url.openConnection();
          connection.setDoOutput(true);
          connection.setDoInput(true);
          connection.setUseCaches(false);
          connection.setInstanceFollowRedirects(true);
          connection.setRequestMethod("POST"); // 设置请求方式
          //connection.setRequestProperty("Accept", "application/json"); // 设置接收数据的格式
          connection.setRequestProperty("Content-Type", "application/json"); // 设置发送数据的格式
          connection.connect();
          //一定要用BufferedReader 来接收响应， 使用字节来接收响应的方法是接收不到内容的
          OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream(), "UTF-8"); // utf-8编码
          out.append(params);
          out.flush();
          out.close();
          // 读取响应
          reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
          String line;
          String res = "";
          while ((line = reader.readLine()) != null) {
              res += line;
          }
          reader.close();
          return res;
      } catch (Exception e) {
          e.printStackTrace();
      }
      return "error"; // 自定义错误信息
  }
}

