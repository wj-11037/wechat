package com.jdm.wechat.pay.service;

import java.util.Map;

import com.jdm.hospital.common.domain.WeChatOrder;

public interface PayNotifyService {

	WeChatOrder queryWechatOrderByOrderNo(String orderNo);

	int updateWechatOrderStatus(Map<String,Object> map);

}
