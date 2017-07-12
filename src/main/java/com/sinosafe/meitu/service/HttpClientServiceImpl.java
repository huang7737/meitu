package com.sinosafe.meitu.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import org.apache.commons.lang.CharEncoding;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 网络通信相关工具类，比如http发送
 * 
 * @author huangping5
 *
 */
@Service
public class HttpClientServiceImpl implements HttpClientService{
	private  Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private PoolingHttpClientConnectionManager poolConnManager;
	private final int maxTotalPool = 200;
	private final int maxConPerRoute = 20;
	private final int socketTimeout = 2000;
	private final int connectionRequestTimeout = 3000;
	private final int connectTimeout = 1000;

//	@PostConstruct
	public void init() {
		try {
			SSLContext sslcontext = SSLContexts.custom().loadTrustMaterial(null, new TrustSelfSignedStrategy()).build();
			HostnameVerifier hostnameVerifier = SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
			SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext, hostnameVerifier);
			Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
					.register("http", PlainConnectionSocketFactory.getSocketFactory()).register("https", sslsf).build();
			poolConnManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
			// Increase max total connection to 200
			poolConnManager.setMaxTotal(maxTotalPool);
			// Increase default max connection per route to 20
			poolConnManager.setDefaultMaxPerRoute(maxConPerRoute);
			SocketConfig socketConfig = SocketConfig.custom().setSoTimeout(socketTimeout).build();
			poolConnManager.setDefaultSocketConfig(socketConfig);
		} catch (Exception e) {

		}
	}
	
	public CloseableHttpClient getConnection(){  
	    RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(connectionRequestTimeout)  
	            .setConnectTimeout(connectTimeout).setSocketTimeout(socketTimeout).build();  
	    CloseableHttpClient httpClient = HttpClients.custom()  
	                .setConnectionManager(poolConnManager).setDefaultRequestConfig(requestConfig).build();  
	    if(poolConnManager!=null&&poolConnManager.getTotalStats()!=null){  
	    	logger.info("Now client pool "+poolConnManager.getTotalStats().toString());  
	    }
	    return httpClient;  
	}
	
	public String httpPost(String url, String reqData) {
		String returnStr = null;
		// 参数检测
		if (StringUtils.isBlank(url)) {
			return returnStr;
		}
		HttpPost httpPost = new HttpPost(url);
		try {
			httpPost.setEntity(new StringEntity(reqData,ContentType.create("text/plain", "UTF-8")));

			CloseableHttpClient client = this.getConnection();
			CloseableHttpResponse response = client.execute(httpPost);
			int status = response.getStatusLine().getStatusCode();
			if (status >= 200 && status < 300) {
				HttpEntity entity = response.getEntity();
				String resopnse = "";
				if (entity != null) {
					resopnse = EntityUtils.toString(entity, "utf-8");
				}
				logger.info("Receive response: url" + url + " status=" + status);
				return entity != null ? resopnse : null;
			} else {
				HttpEntity entity = response.getEntity();
				httpPost.abort();
				logger.info("Receive response: url" + url + " status=" + status + " resopnse=" + EntityUtils.toString(entity, "utf-8"));
				throw new ClientProtocolException("Unexpected response status: " + status);
			}
		} catch (Exception e) {
			httpPost.abort();
			logger.error(" Exception" + e.toString() + " url=" + url + " reqData=" + reqData);
		}
		return returnStr;
	}
	
	public String sendFile(String url, Map<String,String> header,File file) {
		String returnStr = null;
		// 参数检测
		if (StringUtils.isBlank(url)) {
			return returnStr;
		}
		HttpPost httpPost = new HttpPost(url);
		try {
			MultipartEntityBuilder meBuilder = MultipartEntityBuilder.create();
			if(header!=null){
				Set<String> keys=header.keySet();
				for(String key:keys){
					meBuilder.addPart(key,new StringBody(header.get(key), ContentType.TEXT_PLAIN));
				}
			}
	        FileBody fileBody = new FileBody(file);    
	        meBuilder.addPart("image_file", fileBody);     
	        HttpEntity reqEntity = meBuilder.build();    
	        httpPost.setEntity(reqEntity);  

			CloseableHttpClient client = this.getConnection();
			CloseableHttpResponse response = client.execute(httpPost);
			int status = response.getStatusLine().getStatusCode();
			if (status >= 200 && status < 300) {
				HttpEntity entity = response.getEntity();
				String resopnse = "";
				if (entity != null) {
					resopnse = EntityUtils.toString(entity, "utf-8");
				}
				logger.info("Receive response: url" + url + " status=" + status);
				return entity != null ? resopnse : null;
			} else {
				HttpEntity entity = response.getEntity();
				httpPost.abort();
				logger.info("Receive response: url" + url + " status=" + status + " resopnse=" + EntityUtils.toString(entity, "utf-8"));
				throw new ClientProtocolException("Unexpected response status: " + status);
			}
		} catch (Exception e) {
			httpPost.abort();
			logger.error(" Exception" + e.toString() + " url=" + url);
		}
		return returnStr;
	}
	
	public String httpGet(String url){
		return httpGet(url,null);
	}
	
	public String httpGet(String url,Map<String,String> headers){
		HttpGet httpGet = new HttpGet(url);
		setHeader(httpGet,headers);
		try {
			CloseableHttpClient client = this.getConnection();
			CloseableHttpResponse response = client.execute(httpGet);
			int status = response.getStatusLine().getStatusCode();
			if (status >= 200 && status < 300) {
				HttpEntity entity = response.getEntity();
				String resopnse = "";
				if (entity != null) {
					resopnse = EntityUtils.toString(entity, CharEncoding.UTF_8);
				}
				logger.info("Receive response: url" + url + " status=" + status);
				return entity != null ? resopnse : null;
			} else {
				HttpEntity entity = response.getEntity();
				httpGet.abort();
				logger.error("Receive response: url" + url + " status=" + status + " resopnse=" + EntityUtils.toString(entity, "utf-8"));
				throw new ClientProtocolException("Unexpected response status: " + status);
			}
		} catch (Exception e) {
			httpGet.abort();
			logger.error("error",e);
		}
		
		return null;
	}
	
	private void setHeader(HttpGet httpGet,Map<String,String> headers){
		if(headers!=null){
			Set<String> keys=headers.keySet();
			for(String key:keys){
				httpGet.setHeader(key, headers.get(key));
			}
		}
	}
	
    public String download(String url, String filepath) throws IOException {
    	InputStream is=null;
    	FileOutputStream fileout=null;
    	HttpGet httpget = new HttpGet(url);  
        CloseableHttpClient client = this.getConnection();
        CloseableHttpResponse response = client.execute(httpget);  
        try {
        	int status = response.getStatusLine().getStatusCode();
        	HttpEntity entity = response.getEntity();
			if (status >= 200 && status < 300) {
				is = entity.getContent();  
	            File file = new File(filepath);  
	            file.getParentFile().mkdirs();  
	            fileout = new FileOutputStream(file);  
	            /** 
	             * 根据实际运行效果 设置缓冲区大小 
	             */  
	            byte[] buffer=new byte[1024];  
	            int ch = 0;  
	            while ((ch = is.read(buffer)) != -1) {  
	                fileout.write(buffer,0,ch);  
	            }  
	            fileout.flush();  
	            String path=file.getPath();
	            logger.info("Download successful:path="+path+",url="+url);
	            return path;
			}
        } catch (Exception e) {  
        	logger.error("error",e); 
        }finally{
        	if(is!=null){
        		is.close();
        	}
			if(fileout!=null){
				fileout.close();
			}
        	response.close();
        }
        return null;  
    }
	
}
