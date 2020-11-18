package com.jdm.hospital.common.domain;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import lombok.Data;

/**
 * HIS配置类
 *@author Allen
 *@date 2019年10月30日 下午2:17:55
 */

@Component
@Data
public class HisConfig {
	//his地址
	@Value("${his.url}")
	private String hisUrl;
	
	//his账号
	@Value("${his.account}")
	private String account;
	
	//his密码
	@Value("${his.pwd}")
	private String pwd;
	

	//his渠道类型
	@Value("${his.type}")
	private String type;
	
	//医院名称
	@Value("${his.hosname}")
	private String hosname;
	//his.sumPiaoHao
	@Value("${his.sumPiaoHao}")
	private String sumPiaoHao;
}

