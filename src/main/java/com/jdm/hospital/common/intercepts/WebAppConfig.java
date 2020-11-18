package com.jdm.hospital.common.intercepts;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
/**
 * 拦截器
 *@author Allen
 *@date 2019年11月6日 上午8:31:40
 */
@Configuration
public class WebAppConfig implements WebMvcConfigurer  {
	
	//实现拦截器 要拦截的路径以及不拦截的路径
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //注册自定义拦截器，添加拦截路径和排除拦截路径
    	registry.addInterceptor(interceptorConfig()).addPathPatterns("/hospital/**")
    	.excludePathPatterns("/page/**","detail/**","/qrCode/**","/common/**","/createMenu/**","/binding/**","/notify/**","/updateAccessToken/**");
    }
    
    //初始化拦截器的时候,通过构造器 注入对象
    @Bean
	 public InterceptorConfig interceptorConfig() {
		 return new InterceptorConfig();
	 }
    
    
    
}