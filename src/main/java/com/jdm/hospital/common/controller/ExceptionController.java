package com.jdm.hospital.common.controller;

import java.util.HashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;


/**
 * 异常处理器
 */
@ControllerAdvice
public class ExceptionController {
	@ExceptionHandler(value = {Exception.class})
    @ResponseBody
    public Object error(Exception ex){
		
        Map<String,String> map = new HashMap<>();
        map.put("error", ex.getMessage());
        map.put("code", "500");
        return map;
    }
   
}
