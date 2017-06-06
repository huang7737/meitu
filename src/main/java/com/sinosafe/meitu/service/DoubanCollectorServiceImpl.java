package com.sinosafe.meitu.service;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Resource;

import org.apache.http.client.utils.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.sinosafe.dao.CommonDao;
import com.sinosafe.util.NetUtil;

@Service
public class DoubanCollectorServiceImpl implements MeituCollectorService{
	private  Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private int pageSize=25;
	
	@Value("${meitu.folder}")
	private String folder;
	
	@Value("${meitu.groups}")
	private String groups;
	
	@Resource
    private CommonDao dao;
	

	@Override
	public void excute() {
		String[] groupss=groups.split(",");
		for(String group:groupss){
			String groupUrl="https://www.douban.com/group/"+group+"/discussion?start=";
			collectByGroup(group,groupUrl);
		}
	}
	
	public void collectByGroup(String group,String groupUrl){
		int start=0;
		while(start<=50){
			try {
				String pageUrl=groupUrl+start;
				start+=pageSize;
				String topicListPage=NetUtil.httpGet(pageUrl, NetUtil.TEXT_FORMAT_PLAIN);
				Pattern pattern = Pattern.compile("<a href=\"([^\"]+?/group/topic/[^\"]+?)\"");
				Matcher matcher = pattern.matcher(topicListPage);
				boolean isFind = matcher.find();
				if(!isFind){
					logger.warn(pageUrl);
				}
				while (isFind) {
					String topicPage=matcher.group(1);
					logger.info("Find topic:"+topicPage);
					collectImg(topicPage,group);
					isFind=matcher.find();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private void collectImg(String topicUrl,String group) throws Exception{
		String topicListPage=NetUtil.httpGet(topicUrl, NetUtil.TEXT_FORMAT_PLAIN);
		Pattern pattern = Pattern.compile("src=\"([^\"]+?/large/[^\"]+?)\"");
		Matcher matcher = pattern.matcher(topicListPage);
		boolean isFind = matcher.find();
		if(!isFind){
			logger.info("No picture:"+topicUrl);
		}
		while (isFind) {
			String imageUrl=matcher.group(1);
			logger.info(imageUrl);
			try{
				String localPath=NetUtil.download(imageUrl, folder+DateUtils.formatDate(new Date(), "yyyyMMdd")+File.separator+group+File.separator);
				Map<String,String> paramObject=new HashMap<String,String>();
				paramObject.put("localPath", localPath);
				paramObject.put("topicUrl", topicUrl);
				paramObject.put("groupId", group);
				paramObject.put("imageId", imageUrl);
				store2DB(paramObject);
			}catch(Exception e){
				e.printStackTrace();
			}
			isFind=matcher.find();
		}
	}
	
	private void store2DB(Map<String,String> paramObject){
		List list=dao.selectList("com.sinosafe.meitu.findPicture", paramObject);
		if(CollectionUtils.isEmpty(list)){
			dao.update("com.sinosafe.meitu.addPicture", paramObject);
		}
	}
	
	public static void main(String[] args){
		MeituCollectorService service=new DoubanCollectorServiceImpl();
		service.excute();
	}

}
