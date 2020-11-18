package com.jdm.Lis.service.impl;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jdm.Lis.dao.LisDao;
import com.jdm.Lis.domain.Report;
import com.jdm.Lis.service.LisService;
import com.jdm.hospital.common.domain.HisConfig;
import com.jdm.hospital.common.domain.Patient;
import com.jdm.hospital.utils.HisReqClientUtil;
import com.jdm.hospital.utils.ParseAge;



@Service
public class LisServiceImpl implements LisService {
	@Autowired
	LisDao lisDao;
	@Autowired
	HisConfig hisConfig;
	
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	
	/**
	 * 查询检验报告数量(及详情)
	 */
	@Override
	public JSONObject queryReportQuntity(JSONObject json, HttpServletRequest req, HttpServletResponse res) {
		JSONObject jsonResult=new JSONObject();
		List<Report> reportList=null;
		try {
			String type = json.getString("type");//查询的类型
			String date = json.getString("date");//查询的日期
			
			HttpSession session = req.getSession();
			Patient patient = (Patient) session.getAttribute("patient");//2.患者信息
			try {
				String request="<xml>" + 
	    				"<optioncode>8500</optioncode> " + //请求方法code
	    				"<TermNo>"+hisConfig.getAccount()+"</TermNo>" + 
	        			"<QuDao>"+hisConfig.getType()+"</QuDao>" + 
	        			"<Pwd>"+hisConfig.getPwd()+"</Pwd>" + 
	    				"  <parameters>" + 
	    				"    <MyDate4S>"+date+"</MyDate4S> " + //开始时间
	    				"    <Card_Id>"+patient.getPatientId()+"</Card_Id>" + //患者Id
	    				"  </parameters>" + 
	    				"</xml>";
	        	log.info("================>lis检验报告查询，请求his入参：{},"+request);
	        	JSONObject response=HisReqClientUtil.getHisResponseCXF(request,hisConfig.getHisUrl());
	        	log.info("================>lis检验报告查询，请求his返参：{},",response);
			} catch (Exception e) {
				log.info("================>lis检验报告查询异常：{},",e);
			}
			
			Map<String,String> map=new HashMap<String, String>();
			map.put("dateS", LocalDate.now().minusDays(90)+" 00:00:00");//开始时间(默认查3个月)
			map.put("dateE", LocalDate.now()+" 23:59:59");//结束时间
			
			if(type.equals("mz")) {
				//1.查询门诊的检验报告数量
				//map.put("patientId", patient.getPatientId());
				map.put("patientId", "");
				map.put("sPName", patient.getUserName());
				String nSAge=patient.getBirthday()==""?"":ParseAge.getAge(patient.getBirthday())+"";
				if(nSAge=="") {
					map.put("nSAgeS","");
					map.put("nSAgeE", "");
				}else {
					map.put("nSAgeS",(Integer.parseInt(nSAge)-1)+"");
					map.put("nSAgeE", (Integer.parseInt(nSAge)+1)+"");
				}
			}else if(type.equals("zy")) {
				//2.住院的输入住院号
				String zyh = json.getString("zyh");
				map.put("sPName", "");
				map.put("patientId", zyh);
				map.put("nSAgeS","");
				map.put("nSAgeE", "");
			}
			reportList=lisDao.queryReportQuntity(map);
			JSONArray dataArr=new JSONArray();//1.总报告数组
			for(int i=0;i<reportList.size();i++) {
				JSONObject repotSingle=new JSONObject();//2.单个报告信息
				Map<String,String> mapDetail=null;
				List<Report> reportDtailList=null;
				Report report =null;
				JSONObject jo=new JSONObject();//1.用户数据
				JSONArray repotDetailArr=new JSONArray();//2.单个报告的明细数组
				
				report= reportList.get(i);
				jo.put("nIID", report.getNIID());//1.仪器序号
				jo.put("nSID", report.getNSID());//2.样本号
				jo.put("sPID", report.getSPID());//3.病员号
				jo.put("sBedno", report.getSBedno());//4.病床号
				jo.put("dSTime", report.getDSTime());//5.送检时间
				jo.put("dPTime", report.getDPTime());//6.出检时间
				jo.put("sPName", report.getSPName());//7.姓名
				jo.put("sSex", report.getSSex());//8.性别
				jo.put("nSAge", report.getNSAge());//9.年龄
				jo.put("dDate", report.getDDate().subSequence(0, 10));//10.查询日期
				jo.put("nSDID", report.getNSDID());//11.送检医生ID
				jo.put("nTDID", report.getNTDID());//12.检验医生
				jo.put("nDID", report.getNDID());//13.科别ID
				jo.put("sANo", report.getSANo());//14.申请单号
				jo.put("num", i+1);//16.第几条
				jo.put("sdoctor", report.getSDoctor());//17.送检医生名称
				jo.put("tdoctor", report.getTDoctor());//18.检验医生名称
				jo.put("dept", report.getDept());//19.执行科室
				
				
				//============2.明细==========
				mapDetail=new HashMap<String, String>();
				mapDetail.put("nIID",report.getNIID());//1.仪器序号
				mapDetail.put("nSID",report.getNSID());//2.样本号
				mapDetail.put("dDate",report.getDDate());//3.查询日期
				mapDetail.put("nSDID",report.getNSDID());//4.送检医生ID
				mapDetail.put("nTDID",report.getNTDID());//5.检验医生
				mapDetail.put("nDID",report.getNDID());//6.科别ID
				mapDetail.put("sPID",map.get("patientId"));//7.住院号（patientId）
				//============================================================================
				reportDtailList=lisDao.queryReportDetailList(mapDetail);//报告明细
				for(int j=0;j<reportDtailList.size();j++) {
					JSONObject repotDtailJson=new JSONObject();
					Report reportDtail = reportDtailList.get(j);
					repotDtailJson.put("itype", reportDtail.getIType()==null?"":reportDtail.getIType());//4.检验类型
					repotDtailJson.put("bctype", reportDtail.getBCType());//6.浓度值类型。1-数字， 2-字符型
					repotDtailJson.put("sresult", reportDtail.getSResult());//9.结论
					repotDtailJson.put("babnormal", reportDtail.getBAbnormal());//11.结果是否异常，0-未判别，1-正常，2-偏低，3-偏高
					repotDtailJson.put("sabnormal", reportDtail.getSAbnormal()==null?"":reportDtail.getSAbnormal());//12.异常符号
					repotDtailJson.put("sname", reportDtail.getSName());//13.检验项目名称
					repotDtailJson.put("sunit", reportDtail.getSUnit()==null?"":reportDtail.getSUnit());//14.单位
					
					//1-数字， 2-字符型
					if(reportDtail.getBCType().equals("1")) {
						repotDtailJson.put("fconc", reportDtail.getFConc());//7.浓度值，数字
						repotDtailJson.put("normalfr",reportDtail.getFRLow()+"-"+reportDtail.getFRHigh());//15.正常值
					}else if(reportDtail.getBCType().equals("0")) {
						repotDtailJson.put("fconc", "");//7.浓度值，数字
						repotDtailJson.put("normalfr", "阴性");//15.正常值
					}else if(reportDtail.getBCType().equals("2")) {
						repotDtailJson.put("fconc", reportDtail.getSConc());//8.浓度值，字符
						repotDtailJson.put("normalfr",reportDtail.getFRLow()+"-"+reportDtail.getFRHigh());//15.正常值
					}
					
					repotDetailArr.add(repotDtailJson);
				}
				repotSingle.put("userData", jo);//1.用户信息
				repotSingle.put("reportDtail", repotDetailArr);//2.检验报告数组
				
				dataArr.add(repotSingle);//3.再讲用户数据放入
			}
			
			jsonResult.put("code", 0);
			jsonResult.put("msg", "成功");
			jsonResult.put("data", dataArr);
			
			log.info("==============>获取检验报告数量(及明细)返前端：{},",jsonResult);
			
		} catch (Exception e) {
			jsonResult.put("code", 1001);
			jsonResult.put("msg", "失败");
			log.info("==============>获取检验报告数量(及明细)异常：{},",e);
			e.printStackTrace();
		}
		
		return jsonResult;
	}

	
	
	/**
	 * 获取检验报告明细
	 */
	@Override
	public JSONObject queryReportDetail(JSONObject json, HttpServletRequest request, HttpServletResponse response) {
		JSONObject jsonResult=new JSONObject();
		List<Report> reportList=new ArrayList<Report>();//检验报告明细
		String type = json.getString("type");
		
		
		log.info("==============>获取检验报告明细入参：{},",json);
		try {
			Map<String,String> map=new HashMap<String, String>();
			map.put("nIID", json.getString("nIID"));//1.仪器序号
			map.put("nSID", json.getString("nSID"));//2.样本号
			map.put("dDate", json.getString("dDate"));//3.查询日期
			map.put("nSDID", json.getString("nSDID"));//4.送检医生ID
			map.put("nTDID", json.getString("nTDID"));//5.检验医生
			map.put("nDID", json.getString("nDID"));//6.科别ID
			map.put("sPID", json.getString("sPID"));//7.住院号（patientId）
			
			
			reportList=lisDao.queryReportDetailList(map);
			JSONArray repotArr=new JSONArray();
			
			for(int i=0;i<reportList.size();i++) {
				JSONObject repotJson=new JSONObject();
				Report report = reportList.get(i);
				repotJson.put("sdoctor", report.getSDoctor());//1.送检医生
				repotJson.put("tdoctor", report.getTDoctor());//2.检验医生
				repotJson.put("dept", report.getDept());//3.科室
				repotJson.put("itype", report.getIType());//4.检验类型
				repotJson.put("fod", report.getFOD());//5.OD值
				repotJson.put("bctype", report.getBCType());//6.浓度值类型。1-数字， 2-字符型
				repotJson.put("fconc", report.getFConc());//7.浓度值，数字
				repotJson.put("sconc", report.getSConc());//8.浓度值，字符
				repotJson.put("sresult", report.getSResult());//9.结论
				repotJson.put("fcutOff", report.getFCutOff());//10.CutOff值
				repotJson.put("babnormal", report.getBAbnormal());//11.结果是否异常，0-未判别，1-正常，2-偏低，3-偏高
				repotJson.put("sabnormal", report.getSAbnormal());//12.异常符号
				repotJson.put("sname", report.getSName());//13.检验项目名称
				repotJson.put("sunit", report.getSUnit());//14.单位
				repotJson.put("normalfr", report.getFRLow()+"-"+report.getFRHigh());//15.正常值
				repotArr.add(repotJson);
			}
			
			if(type.equals("mz")) {
				//1.查询门诊的检验报告数量
				HttpSession session = request.getSession();
				Patient patient = (Patient) session.getAttribute("patient");//2.患者信息
				map.put("patientId", patient.getPatientId());
			}else if(type.equals("zy")) {
				//2.住院的输入住院号
				String zyh = json.getString("sPID");
				map.put("patientId", zyh);
			}
			
			reportList=lisDao.queryReportQuntity(map);
			log.info("==============>获取检验报告数量成功：{},",reportList);
			
			
			jsonResult.put("code", 0);
			jsonResult.put("msg", "成功");
			jsonResult.put("data", repotArr);
			jsonResult.put("userData", repotArr);
			
			
			log.info("==============>获取检验报告明细返前端json：{},",jsonResult);
			
		} catch (Exception e) {
			jsonResult.put("code", 1001);
			jsonResult.put("msg", "失败");
			log.info("============》获取检验报告明细异常：{},",e);
			e.printStackTrace();
		}
		
		return jsonResult;
	}
	
	public static void main(String[] args) {
		System.out.println(LocalDate.now().minusDays(30).toString()+"===="+LocalDate.now().toString());
	}
}
