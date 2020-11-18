package com.jdm.hospital.common.domain;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import lombok.Data;

@Component
@ConfigurationProperties(prefix="bootdo")
@Data
public class BootdoConfig {
	//上传路径
	private String uploadPath;
	private String username;
	private String password;

	
}
