package com.sinosafe.util;

import com.alibaba.fastjson.JSONObject;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.request.UploadFileRequest;
import com.qcloud.cos.sign.Credentials;

public class CloudStoreUtil {
	private static COSClient cosClient=null;
	private static String bucketName="meitu";
	static{
		long appId = 1253165703;
        String secretId = "AKIDZCgKp7t8pNkvvkwxJZfaIn9JH3JVoJvK";
        String secretKey = "LUQLA5uXyDdw67wZ3LTP7gaLix0rZ4NZ";
        // 初始化秘钥信息
        Credentials cred = new Credentials(appId, secretId, secretKey);
        
        // 初始化客户端配置
        ClientConfig clientConfig = new ClientConfig();
        // 设置bucket所在的区域，比如华南园区：gz； 华北园区：tj；华东园区：sh ；
        clientConfig.setRegion("gz");
        
        // 初始化cosClient
        cosClient = new COSClient(clientConfig, cred);
	}
	
	public static COSClient getClient(){
		return cosClient;
	}
	
	public static String send2Cloud(String cosPath,String localPath){
		UploadFileRequest uploadFileRequest = new UploadFileRequest(bucketName,cosPath, localPath);
		String retString = cosClient.uploadFile(uploadFileRequest);
		JSONObject retJson=JSONObject.parseObject(retString);
		System.out.println(retString);
		if(retJson.getIntValue("code")==0){
			return retJson.getJSONObject("data").getString("access_url");
		}
		return null;
	}
	
	public static void main(String[] args){
		String retString=CloudStoreUtil.send2Cloud("/20170607/haixiuzu/test.png", "D:\\meitu\\20170606\\haixiuzu\\p80795366.jpg");
		System.out.println(retString);
	}
}
