package com.jdm.wechat.pay.service.impl;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jdm.hospital.common.domain.WeChatOrder;
import com.jdm.wechat.pay.dao.PayNotifyDao;
import com.jdm.wechat.pay.service.PayNotifyService;


@Service
public class PayNotifyServiceImpl implements PayNotifyService {
	
	@Autowired
	PayNotifyDao payNotifyDao;

	@Override
	public WeChatOrder queryWechatOrderByOrderNo(String orderNo) {
		return payNotifyDao.queryWechatOrderByOrderNo(orderNo);
	}

	@Override
	public int updateWechatOrderStatus(Map<String,Object> map) {
		return payNotifyDao.updateWechatOrderStatus(map);
	}
}
