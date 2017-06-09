package com.sinosafe.meitu.service;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class ZhihuQuestionServiceImpl implements ZhihuQuestionService{
	
	public void getPictureByQuestionId(String questionId) {
		HttpClientService service=new HttpClientServiceImpl();
		int pageSize=5;
		for(int page=0;page<200;page++){
			String url="https://www.zhihu.com/api/v4/questions/"+questionId+"/answers?"
					+ "include=data%5B*%5D.content%2Cvoteup_count%2Ccomment_count%2Cis_top%2Cis_normal%2C"
					+ "suggest_edit%3Bdata%5B*%5D.author.badge%5B*%5D.topics&limit="+pageSize+"&offset="+page*pageSize;
			String str=service.httpGet(url,createHeader(questionId));
			if(StringUtils.isBlank(str)){
				break;
			}
			JSONObject json=JSONObject.parseObject(str);
			JSONArray datas=json.getJSONArray("data");
			if(datas==null||datas.size()==0){
				break;
			}
			for(int i=0;i<datas.size();i++){
				String content=((JSONObject)datas.get(i)).getString("content");
				Element root=Jsoup.parse(content);
				Elements imgs=root.select("noscript").select("img[data-original]");
				for(Element img:imgs){
					String imgUrl=img.attr("data-original");
					try {
						service.download(imgUrl, "d:\\zhihu\\"+questionId+"\\"+imgUrl.substring(imgUrl.lastIndexOf("/")+1));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			page++;
		}
	}
	
	private Map<String,String> createHeader(String questionId){
		Map<String,String> headers=new HashMap<String,String>();
		headers.put("authorization","oauth c3cef7c66a1843f8b3a9e6a1e3160e20");
		headers.put("Accept", "application/json, text/plain, */*");    
		headers.put("Cache-Control", "no-cache");
		headers.put("Accept-Encoding", "gzip, deflate");    
		headers.put("Accept-Language", "zh-CN,zh;q=0.8");    
		headers.put("Connection", "keep-alive");    
		headers.put("Cookie", "q_c1=9f049d8ff7db49c39dd1c270fdb20bc4|1496993589000|1496993589000; q_c1=2ce913986889428994d59df169f4ba1a|1496993589000|1496993589000; d_c0=\"AJDCzRUF4wuPTkN97TpH_tEhkPHI5HKE6NI=|1496993606\"; __utma=51854390.135471675.1496993607.1496993607.1496993607.1; __utmb=51854390.0.10.1496993607; __utmc=51854390; __utmz=51854390.1496993607.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none); __utmv=51854390.010--; r_cap_id=\"YmM3OGY5YzQwMzZiNDcxYThhNTY4YzIzMDhkMjc2NDQ=|1496994575|009bf1ccb9dff80812c99d804c9e3d9ef867c618\"; cap_id=\"ZDQxNzgxZGI5M2MwNDlkMWIzYThlYTVhZjY3YmRmOTM=|1496994575|69d864755ae461063136ea05aee2f72c63366bec\"; l_cap_id=\"MjEzYThmY2YzOWE5NDBhYWIwYTYyZTMxNTBmZGQ1ZDk=|1496994576|f3019ca3df0b12d1f1f8ded1c3bb85974d0542b0\"");    
		headers.put("Host", "www.zhihu.com");    
		headers.put("refer", "https://www.zhihu.com/question/"+questionId);    
		headers.put("User-Agent", "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Mobile Safari/537.36");
		return headers;
	}
	
	public static void main(String[] args){
		ZhihuQuestionService questionService=new ZhihuQuestionServiceImpl();
		questionService.getPictureByQuestionId("60288863");
	}
	
}
