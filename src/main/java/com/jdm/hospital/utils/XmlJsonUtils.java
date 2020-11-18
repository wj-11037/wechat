package com.jdm.hospital.utils;


import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import net.sf.json.xml.XMLSerializer;
/**
 * xml转json
 * @author Allen
 *
 */
public class XmlJsonUtils {
	
	
    /**
     * xml 转 json
     * @param xmlString xml字符串
     * @return
     */
    public static String xmlToJson(String xml) {
    	XMLSerializer serializer = new XMLSerializer();
    	return serializer.read(xml).toString();
    }
    
    
    public static String json2xml(String jsonString) {
        XMLSerializer xmlSerializer = new XMLSerializer();
        xmlSerializer.setTypeHintsEnabled(false); // 去除 节点中type类型
        String xml = xmlSerializer.write(JSONSerializer.toJSON(jsonString));
        xml = xml.replace("<o>", "").replace("</o>", "");
        xml = xml.replaceAll("\r\n", "").concat("\r\n");
        return xml.replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", "");
    }
    
    
    public static void main(String[] args) {
    	
    	String xmlStr="<xml>\r\n" + 
    			"<resultCode>1</resultCode>\r\n" + 
    			"<resultDesc>成功</resultDesc>\r\n" + 
    			"<resultObjects>\r\n" + 
    			"	<rowcount>3</rowcount>\r\n" + 
    			"	<Item>\r\n" + 
    			"		<Departments>儿科</Departments>\r\n" + 
    			"		<Billing>系统管理员</Billing>\r\n" + 
    			"		<MyDate>2016-07-14 16:28:30</MyDate>\r\n" + 
    			"		<MyType>0</MyType>\r\n" + 
    			"		<SqMzDocPatientId>2016071400001</SqMzDocPatientId>\r\n" + 
    			"		<SqMzDocPatientItemNum>3</SqMzDocPatientItemNum>\r\n" + 
    			"		<rowcounttwo>2</rowcounttwo>\r\n" + 
    			"		<Itemtwo>\r\n" + 
    			"			<PrescriptionBodyId>01000001</PrescriptionBodyId>\r\n" + 
    			"			<PrescriptionBodyName>注射用苯唑西林钠</PrescriptionBodyName>\r\n" + 
    			"			<Specification>.5g*10*1</Specification>\r\n" + 
    			"			<Unit>支</Unit>\r\n" + 
    			"			<Quantity>20.0000</Quantity>\r\n" + 
    			"			<Fushuo>1</Fushuo>\r\n" + 
    			"		</Itemtwo>\r\n" + 
    			"		<Itemtwo>\r\n" + 
    			"			<PrescriptionBodyId>00G33060400600</PrescriptionBodyId>\r\n" + 
    			"			<PrescriptionBodyName>阻生牙拔除术</PrescriptionBodyName>\r\n" + 
    			"			<Specification>每牙</Specification>\r\n" + 
    			"			<Unit>每牙</Unit>\r\n" + 
    			"			<Quantity>1.0000</Quantity>\r\n" + 
    			"			<Fushuo>1</Fushuo>\r\n" + 
    			"		</Itemtwo>\r\n" + 
    			"	</Item>\r\n" + 
    			"</resultObjects>\r\n" + 
    			"</xml>";
    	
		System.out.println("----------->"+xmlToJson(xmlStr));
		
		
	}
}
