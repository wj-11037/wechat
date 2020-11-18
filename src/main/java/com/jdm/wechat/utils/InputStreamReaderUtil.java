package com.jdm.wechat.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.servlet.http.HttpServletRequest;

public class InputStreamReaderUtil {
	
	public static String getInputStr(HttpServletRequest request) {
		 BufferedReader reader = null;
	        StringBuilder msg = new StringBuilder();
	        try{
	            reader = new BufferedReader(new InputStreamReader(request.getInputStream(), "utf-8"));
	            String line = null;
	            while ((line = reader.readLine()) != null){
	            	msg.append(line);
	            }
	        } catch (IOException e){
	            e.printStackTrace();
	        } finally {
	            try{
	                if (null != reader){ reader.close();}
	            } catch (IOException e){
	                e.printStackTrace();
	            }
	        }
			return msg.toString();
	}
}
