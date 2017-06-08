package com.sinosafe.meitu.service;

import java.io.IOException;

import org.apache.http.impl.client.CloseableHttpClient;

public interface HttpClientService {
	public CloseableHttpClient getConnection();
	public String httpPost(String url, String reqData);
	public String httpGet(String url);
	public String download(String url, String filepath) throws IOException;
}
