package com.jdm.hospital.gh.domain;

import lombok.Data;

/**
 * 科室列表
 *@author Allen
 *@date 2019年10月30日 下午1:50:19
 */
@Data
public class Dept {
	
	private String depId;//科室ID
	private String deptInitials;//科室首字母拼音
	private String parDeptId;//父科室ID
	private String depName;//科室名称
	private String pbxh;//排班序号
	private String floor;//楼层位置
	private String date;//日期
	private String des;//描述
	
	
	
}
