package com.jdm.hospital.common.intercepts;


import java.net.URLEncoder;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import com.jdm.hospital.common.domain.HisConfig;
import com.jdm.hospital.common.domain.Patient;
import com.jdm.wechat.common.domain.WxConfig;

/**
 * 拦截器配置类
 *@author Allen
 *@date 2019年11月6日 上午8:31:24
 */


public class InterceptorConfig implements HandlerInterceptor {
	
	@Autowired
	HisConfig hisConfig;
	@Resource
	private WxConfig wxConfig;
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object o) throws Exception {
        //log.info("==============开始进入请求地址拦截 =================");
        HttpSession session = request.getSession();
        String path = request.getRequestURI();
        //log.info("==============>拦截请求路径：path：{}",path);
        session.setAttribute("path", path);//保存请求的路由
        Patient patient = (Patient) session.getAttribute("patient");//患者信息
        //log.info("==============>患者信息：{}",patient);
        
        if (patient == null) {//1.先从session缓存里拿用户信息
        		String url="https://open.weixin.qq.com/connect/oauth2/authorize?appid="+
        					wxConfig.getAppid()+"&redirect_uri="+
        					URLEncoder.encode(wxConfig.getWeChatUser(), "UTF-8")
        					+"&response_type=code&scope=snsapi_userinfo&state=STATE#wechat_redirect";        		
	        	response.sendRedirect(url);
	        	return false; 	
		}
        
        return true; //用户存在放行
    }
    
	@Override
    public void postHandle(HttpServletRequest httpServletRequest, 
				    		HttpServletResponse httpServletResponse, 
				    		Object o, ModelAndView modelAndView) throws Exception {
		//log.info("==========放行postHandle===========");
    }
 
    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest,
					    		HttpServletResponse httpServletResponse, 
					    		Object o, Exception e) throws Exception {
    	//log.info("==========放行afterCompletion===========");
    }
    
    
}
