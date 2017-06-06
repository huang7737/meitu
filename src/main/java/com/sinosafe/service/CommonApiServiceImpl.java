package com.sinosafe.service;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.sinosafe.dao.CommonDao;

@Service("commonApiServiceImpl")
public class CommonApiServiceImpl implements CommonApiService {
	@Resource
    private CommonDao dao;

	@Override
	public List<Map<String, Object>> queryList(String cardBin) {
		return dao.selectList("com.sinosafe.demo.findCardInfo", cardBin);
	}
	
}
