package com.sinosafe.meitu.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sinosafe.dao.CommonDao;

@Controller
@RequestMapping("/app")
public class MeituController {
	
	@Resource
    private CommonDao dao;
	
	@RequestMapping("/listPicture")
	@ResponseBody
	public  Map<String, Object> test(HttpServletRequest request,HttpServletResponse response,@RequestParam("page")String page) throws Exception {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			Map<String,String> paramObject=new HashMap<String,String>();
			int pageSize=20;
			paramObject.put("startIndex",Integer.parseInt(page)*pageSize+"");
			paramObject.put("pageSize",pageSize+"");
			List dataList=dao.selectList("com.sinosafe.meitu.findAllPicture", paramObject);
			resultMap.put("dataList", dataList);
			resultMap.put("resultCode", "0000");
			resultMap.put("resultMsg", "成功");
		}catch (Exception e) {
			e.printStackTrace();
			resultMap.put("resultCode", "0000");
			resultMap.put("resultMsg", "系统异常");
		}
		return resultMap;
	}
}
