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
	
	@Resource
	private HttpClientService httpClientService;
	

	@Override
	public void excute() {
		String[] groupss=groups.split(",");
		for(String group:groupss){
			try {
				String groupUrl="https://www.douban.com/group/"+group+"/discussion?start=";
				collectByGroup(group,groupUrl);
			}catch(Exception e){
				logger.error("error",e);
			}
		}
	}
	
	public void collectByGroup(String group,String groupUrl){
		int start=0;
		while(start<=25){
			try {
				String pageUrl=groupUrl+start;
				start+=pageSize;
				int num = (int) (Math.random() * 10);
				logger.info("sleep "+num+" seconds");
				Thread.sleep(num*1000);
				logger.info(pageUrl);
				String topicListPage=httpClientService.httpGet(pageUrl);
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
		String topicListPage=httpClientService.httpGet(topicUrl);
		if(StringUtils.isBlank(topicListPage)){
			return;
		}
		Pattern pattern = Pattern.compile("src=\"([^\"]+?/(large|llarge)/[^\"]+?)\"");
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
					int num = (int) (Math.random() * 10);
					logger.info("sleep "+num+" seconds");
					Thread.sleep(num*1000);
					String filePath=DateUtils.formatDate(new Date(), "yyyyMMdd")+File.separator+group+File.separator;
					String fileName= getFileName(imageUrl);  
		            String cosPath="/"+filePath+fileName;
					String localPath=httpClientService.download(imageUrl, folder+filePath+fileName);
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
	
	private String getFileName(String url) {  
        return url.substring(url.lastIndexOf("/")+1,url.length());
    }
	
	public static void main(String[] args){
		MeituCollectorService service=new DoubanCollectorServiceImpl();
		service.excute();
	}

}
