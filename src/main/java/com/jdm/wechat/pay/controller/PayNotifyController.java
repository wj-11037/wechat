package com.jdm.wechat.pay.controller;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import com.alibaba.fastjson.JSONObject;
import com.github.wxpay.sdk.WXPayUtil;
import com.jdm.hospital.common.domain.WeChatOrder;
import com.jdm.hospital.yjjcz.service.YjjczService;
import com.jdm.hospital.zycz.service.ZyService;
import com.jdm.wechat.common.dao.WxUserDao;
import com.jdm.wechat.common.domain.WxConfig;
import com.jdm.wechat.pay.service.PayNotifyService;


/**
 * 支付回调
 *@author Allen
 *@date 2020年3月26日 上午9:14:45
 */
@Controller
@RequestMapping("/notify")
public class PayNotifyController {
	
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	@Autowired
	WxConfig wxConfig;
	@Autowired
	PayNotifyService payNotifyService;
	@Autowired
	YjjczService yjjczService;
	@Autowired
	ZyService zyService;
	@Autowired
	WxUserDao wxUserDao;
	
	/**
	 * 微信支付完成回调
	 *@author Allen
	 *@date 2019年8月30日 下午2:13:05 
	 *@param request
	 *@param response
	 *@throws Exception
	 */
	@RequestMapping(value = "/wechatNotify.do", method = RequestMethod.POST)
	@ResponseBody
	public void notifyYjjcz(HttpServletRequest request,HttpServletResponse response) throws Exception{
		
		
		log.info("================微信支付回调:{}",request);
        InputStream inStream = request.getInputStream();
        ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while ((len = inStream.read(buffer)) != -1) {
            outSteam.write(buffer, 0, len);
        }
        
        String resultxml = new String(outSteam.toByteArray(), "utf-8");
        log.info("================微信支付回调接收到的xml:{}",resultxml);
        
        //1.将接收到的Xml转为Map
        Map<String, String> notifyMap = WXPayUtil.xmlToMap(resultxml);
        //log.info("================微信支付，将接收到的Xml转为Map:{}",notifyMap);
        outSteam.close();
        inStream.close();
        
        //2.验证签名
        if (!(WXPayUtil.isSignatureValid(resultxml,wxConfig.getPaternerkey()))) {
        	log.info("================微信支付验证签名失败================");
        	PrintWriter out = response.getWriter();
            out.write("<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[签名不正确]]></return_msg></xml>"); 
            out.close();
            return;
        }
        
        
        log.info("================>微信支付验证签名成功================");
        //3.验证是否成功
        if(notifyMap!=null&&"FAIL".equals(notifyMap.get("result_code"))){
        	log.info("================微信支付回调状态码错误，result_code:{}",notifyMap.get("result_code"));
		    PrintWriter out = response.getWriter();
		    out.write("<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[回调状态码不正确]]></return_msg></xml>"); 
		    out.close();
		    return;
        }
        
        
	   	String orderCode = notifyMap.get("out_trade_no");//订单号 
	   	String flowNo=notifyMap.get("transaction_id");//支付流水号
	   	String totalFee = notifyMap.get("total_fee");//实际支付的订单金额:单位 分
	   	
	   	try {
			//1.根据订单号查询微信支付订单信息
			WeChatOrder wechatOrder=payNotifyService.queryWechatOrderByOrderNo(orderCode);
			
			log.info("================>支付回调,获取订单详情：{}",wechatOrder);
			//2.判断订单是否为空
			if(wechatOrder==null) {
				log.info("================>订单为空=====================");
				PrintWriter out = response.getWriter();
	   		    out.write("<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[SUCCESS]]></return_msg></xml>"); 
	   		    out.close();
	   		    return;
			}
			//3.判断该订单是否已支付
			if(wechatOrder.getOrderStatus().equals("1")) {
				log.info("================>订单已支付=====================");
			    PrintWriter out = response.getWriter();
			    out.write("<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[回调状态码不正确]]></return_msg></xml>"); 
			    out.close();
			    return;
			}
			//3.校验金额(上线记得要校验订单金额)
			double payAmount=Double.parseDouble(totalFee)/100;//将金额转为元
			if(wechatOrder.getAmount()!=payAmount) {
	   			log.info("================微信支付回调金额不对，订单金额(元):{},微信回调金额(元){}",wechatOrder.getAmount(),payAmount);
	   			PrintWriter out = response.getWriter();
	   		    out.write("<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[SUCCESS]]></return_msg></xml>"); 
	   		    out.close();
	   		    return;
	   		}
			
			//4.修改订单状态，更新支付流水
			Map<String,Object> map=new HashMap<String, Object>();
			map.put("orderCode", orderCode);
			map.put("flowNo", flowNo);
			if(payNotifyService.updateWechatOrderStatus(map)<=0) {
				log.info("================>修改订单状态，更新支付流水 失败=============================");
				PrintWriter out = response.getWriter();
	   		    out.write("<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[SUCCESS]]></return_msg></xml>"); 
	   		    out.close();
			}
			
			//5.判断订单类型,
			if(wechatOrder.getBtype().equals("YJJCZ")) {
				//调预交金充值接口充值
				JSONObject recharge = yjjczService.notifyToRecharge(wechatOrder);
				log.info("================>支付回调，预交金充值：{},",recharge);
				
			}
			if(wechatOrder.getBtype().equals("GH")) {
				//挂号
				JSONObject recharge = yjjczService.notifyToGH(wechatOrder);
				log.info("================>支付回调，挂号：{},",recharge);
				
			}
			if(wechatOrder.getBtype().equals("YYGH")) {
				//预约挂号
				JSONObject recharge = yjjczService.notifyToYYGH(wechatOrder);
				log.info("================>支付回调，预约挂号：{},",recharge);
				
			}
			if(wechatOrder.getBtype().equals("MZJF")) {
				//门诊缴费
				JSONObject recharge = yjjczService.notifyToMZJF(wechatOrder);
				log.info("================>支付回调，门诊缴费：{},",recharge);
				
			}
			if(wechatOrder.getBtype().equals("ZYCZ")) {
				JSONObject recharge = zyService.notifyToRechargeZyFee(wechatOrder);
				log.info("================>支付回调，住院充值：{},",recharge);
			}
			
			
			
			
			PrintWriter out = response.getWriter();
   		    out.write("<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[SUCCESS]]></return_msg></xml>"); 
   		    out.close();
		} catch (Exception e) {
			log.info("================>支付回调，异常：{},",e);
			PrintWriter out = response.getWriter();
   		    out.write("<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[SUCCESS]]></return_msg></xml>"); 
   		    out.close();
			e.printStackTrace();
		}
	}
	
	
}
