 package com.jdm.wechat.common.controller;


import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import com.jdm.wechat.utils.DealMessageUtil;
import com.jdm.wechat.utils.InputStreamReaderUtil;
import com.jdm.wechat.utils.SignUtil;
import com.jdm.wechat.utils.WeChatUtil;

 

/**
   *    微信认证+ 接收，发送消息
 * @author Allen
 */
@Controller
@RequestMapping("/jdm")
public class WechatAuthController {
	
	
	
	private static Logger log = LoggerFactory.getLogger(WechatAuthController.class);
 
	@RequestMapping(value="/wechat",method = { RequestMethod.GET })
	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		log.info("===============微信接入验证==============");
		// 微信加密签名
		String signature = request.getParameter("signature");
		// 时间戳
		String timestamp = request.getParameter("timestamp");
		// 随机数
		String nonce = request.getParameter("nonce");
		// 随机字符串
		String echostr = request.getParameter("echostr");
 
		// 通过检验signature对请求进行校验，若校验成功则原样返回echostr，表示接入成功，否则接入失败
		PrintWriter out = null;
		try {
			out = response.getWriter();
			if (SignUtil.checkSignature(signature, timestamp, nonce)) {
				log.info("接入成功================》");
				out.print(echostr);
				out.flush();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (out != null)
				out.close();
		}
	}
	
	@RequestMapping(value="/wechat",method = { RequestMethod.POST })
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out=response.getWriter();
        
       
       //1.获取服务器发送过来的信息，因为不是参数，得用输入流读取
       String msg = InputStreamReaderUtil.getInputStr(request);
       log.info("微信接收到的消息：=======》"+msg);//打印用户发送的消息
       try
         {
    	   //2.将接收到的用户消息转为Map
        	Map<String, String> reqMsg = WeChatUtil.xmlToMap(msg.toString());
            //3.对接收到的消息进行处理
            Map<String, String> resMap=dealRequestMsg(reqMsg);
        	
        	
            //4.将map转为xml
            String xml = WeChatUtil.mapToXml(resMap);
            //log.info("=======》返回用户的消息："+xml);//打印用户发送的消息
            out.println(xml);//回复
            out.flush();
           
        }catch(Exception e){
        	log.info("=======》微信处理消息异常："+e.toString());//打印用户发送的消息
        }finally {
        	out.close();
		}
    }

	/**
	 * 处理消息
	 *@author Allen
	 *@date 2019年12月28日 下午12:41:34 
	 *@param reqMsg
	 *@return
	 */
	private Map<String, String> dealRequestMsg(Map<String, String> reqMsg) {
		String msgType = reqMsg.get("MsgType");
		
		Map<String, String> msg=new HashMap<String, String>();
		switch (msgType) {
		case "text":
			msg=DealMessageUtil.dealTextMsg(reqMsg);
			break;
		case "image":
			msg=DealMessageUtil.dealImageMsg(reqMsg);
			break;
		case "voice":
			
			break;
		case "shortvideo":
			
			break;
		case "location":
					
			break;
		case "link":
			
			break;
		case "event":
			msg=DealMessageUtil.dealEventMsg(reqMsg);
			break;
		default:
			break;
		}
		
		return msg;
	}

	
	
	
	
	
	
}
