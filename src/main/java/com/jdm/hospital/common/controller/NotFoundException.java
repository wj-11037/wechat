package com.jdm.hospital.common.controller;

import javax.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;


/**
 * 404页面全局处理
 *@author Allen
 *@date 2020年1月8日 上午10:20:33
 */
@Controller
public class NotFoundException implements ErrorController{
	
	@Override
    public String getErrorPath() {
        return "/error";
    }
 
    @RequestMapping(value = {"/error"})
    public String error(HttpServletRequest request) {
        return "error/404";
    }
}
