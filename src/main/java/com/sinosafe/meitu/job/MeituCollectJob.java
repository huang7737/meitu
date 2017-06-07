package com.sinosafe.meitu.job;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.sinosafe.meitu.service.MeituCollectorService;

/**
 * 
 * @author huangping5
 * 适应单个服务器情况，如果多服务器考虑使用quartz，或者用redis加锁
 */
@Component
public class MeituCollectJob {
	private  Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Resource
	MeituCollectorService service;
	
	@Scheduled(fixedDelay=1800000)//30分钟执行一次
    public void execute(){
		logger.info("start collect run");
		int num = (int) (Math.random() * 100);
		try {
			logger.info("sleep "+num+" seconds");
			Thread.sleep(num*1000);
			service.excute();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		logger.info("end collect run");
    }
}
