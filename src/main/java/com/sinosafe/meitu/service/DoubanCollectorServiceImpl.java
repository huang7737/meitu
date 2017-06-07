package com.sinosafe.meitu.service;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.sinosafe.dao.CommonDao;
import com.sinosafe.util.CloudStoreUtil;
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
		while(start<=25){
			try {
				String pageUrl=groupUrl+start;
				start+=pageSize;
				logger.info(pageUrl);
				String topicListPage=NetUtil.httpGet(pageUrl, NetUtil.TEXT_FORMAT_PLAIN);
				if(topicListPage!=null){
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
				Map<String,String> paramObject=new HashMap<String,String>();
				paramObject.put("topicUrl", topicUrl);//查询必须
				paramObject.put("groupId", group);//查询必须
				paramObject.put("imageId", imageUrl);
				List list=dao.selectList("com.sinosafe.meitu.findPicture", paramObject);
				if(CollectionUtils.isEmpty(list)){
					String filePath=DateUtils.formatDate(new Date(), "yyyyMMdd")+File.separator+group+File.separator;
					String fileName= NetUtil.getFileName(imageUrl);  
		            String cosPath="/"+filePath+fileName;
					String localPath=NetUtil.download(imageUrl, folder+filePath);
					if(StringUtils.isNoneBlank(localPath)){
						String access_url=CloudStoreUtil.send2Cloud(cosPath.replace("\\", "/"), localPath);
						if(StringUtils.isNoneBlank(access_url)){
							paramObject.put("localPath", localPath);
							paramObject.put("access_url", access_url);
							dao.update("com.sinosafe.meitu.addPicture", paramObject);
						}
					}
				}
			}catch(Throwable e){
				e.printStackTrace();
			}
			isFind=matcher.find();
		}
	}
	
	public static void main(String[] args){
		MeituCollectorService service=new DoubanCollectorServiceImpl();
		service.excute();
	}

}
