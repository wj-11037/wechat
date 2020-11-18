package com.jdm.hospital.utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

/*
 * 将soapxml转为 Map,将xml的Body部分取出，然后转为Map
 */
public class SoapXmlUtil {
	
	
	public static Map<String, String> map = new HashMap<String, String>();
	 
	public  static Map<String, String> ParseSoapXml(String SoapXml) {
		try {
			Document document = DocumentHelper.parseText(SoapXml);
			Element elements = document.getRootElement();
			Iterator iterators = elements.elementIterator();
			// 获取根节点中的信息
			while (iterators.hasNext()) {
				Element element = (Element) iterators.next();
				String name = element.getName();
				// 一般Header节点中是空的
				if (name.equals("Body")) {
					Iterator iterator = element.elementIterator();
					ParseDom(iterator);
				}
			}
		} catch (DocumentException e) {
			e.printStackTrace();
		}
		
		return map;
	}
	// 获取具体key-value
	private static void ParseDom(Iterator iterators) {
		while (iterators.hasNext()) {
			Element element = (Element) iterators.next();
			Iterator iterator = element.elementIterator();
			if (iterator.hasNext()) {
				ParseDom(iterator);
			} else {
				String key = element.getName();
				String value = element.getStringValue();
				map.put(key, value);
			}
		}
	}
}
