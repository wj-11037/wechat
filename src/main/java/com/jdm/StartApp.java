package com.jdm;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;


@EnableScheduling
@ServletComponentScan
@SpringBootApplication
@MapperScan("com.jdm.**.dao")
@PropertySource(value = {"classpath:/config/wx.properties","classpath:/config/his.properties"},encoding = "utf-8")
public class StartApp extends SpringBootServletInitializer{
	public static void main(String[] args) {
		SpringApplication.run(StartApp.class, args);
		 System.out.println("=============微信公众号 START SUCCESS =============");
	}
	
	@Override
   	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
   		return builder.sources(StartApp.class);
   	}   
}
