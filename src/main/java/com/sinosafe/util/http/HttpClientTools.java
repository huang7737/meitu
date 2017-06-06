package com.sinosafe.util.http;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpHost;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

public class HttpClientTools {
	  private static DefaultHttpClient httpclient = initHttpClientPool();
	  private static X509TrustManager tm = new X509TrustManager() {
	    public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {
	    }

	    public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {
	    }

	    public X509Certificate[] getAcceptedIssuers() {
	      return null;
	    }
	  };

	  private static DefaultHttpClient initHttpClientPool()
	  {
	    HttpParams params = new BasicHttpParams();
	    HttpConnectionParams.setConnectionTimeout(params, 40000);
	    HttpConnectionParams.setSoTimeout(params, 40000);
	    ConnManagerParams.setMaxTotalConnections(params, 50);
	    HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);

	    SchemeRegistry schemeRegistry = new SchemeRegistry();
	    schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
	    schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));

	    if ((System.getProperty("proxyHost") != null) && (System.getProperty("proxyPort") != null) && (System.getProperty("proxyPort").matches("[\\d]{1,}"))) {
	      HttpHost proxyHost = new HttpHost(System.getProperty("proxyHost"), Integer.parseInt(System.getProperty("proxyPort")), "http");
	      params.setParameter("http.route.default-proxy", proxyHost);
	    }

	    ClientConnectionManager cm = new ThreadSafeClientConnManager(params, schemeRegistry);
	    DefaultHttpClient client = new DefaultHttpClient(cm, params);
	    HttpClientParams.setCookiePolicy(client.getParams(), "compatibility");

	    client.getParams().setParameter("http.protocol.content-charset", "UTF-8");
	    client.getParams().setParameter("Content-Encoding", "UTF-8");
	    client.getParams().setParameter("; charset=", "UTF-8");
	    client.getParams().setParameter("US-ASCII", "UTF-8");
	    client.getCookieStore().addCookie(new BasicClientCookie("sig", "h01a4f7f483617e0c95ac326ca0ffc937db10e16e51f535714210065ae7b188499fecd6167ae644f2a4"));
	    client.getCookieStore().addCookie(new BasicClientCookie("cert", "x7UjzxjLYrZMnuT_SGrKJbsFRHzwArk5"));
	    try
	    {
	      SSLContext ctx = SSLContext.getInstance("TLS");
	      ctx.init(null, new TrustManager[] { tm }, null);
	      SSLSocketFactory ssf = new SSLSocketFactory(ctx);
	      ssf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
	      ClientConnectionManager ccm = client.getConnectionManager();
	      SchemeRegistry sr = ccm.getSchemeRegistry();
	      sr.register(new Scheme("https", ssf, 443));
	    }
	    catch (NoSuchAlgorithmException e) {
	      e.printStackTrace();
	    }
	    catch (KeyManagementException e) {
	      e.printStackTrace();
	    }

	    return wrapClient(client);
	  }

	  public static DefaultHttpClient wrapClient(HttpClient base) {
	    try {
	      SSLContext ctx = SSLContext.getInstance("TLS");
	      X509TrustManager tm = new X509TrustManager() {
	        public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {
	        }
	        public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {  } 
	        public X509Certificate[] getAcceptedIssuers() { return null; }

	      };
	      ctx.init(null, new TrustManager[] { tm }, null);
	      SSLSocketFactory ssf = new SSLSocketFactory(ctx);
	      ssf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
	      ClientConnectionManager ccm = base.getConnectionManager();
	      SchemeRegistry sr = ccm.getSchemeRegistry();
	      sr.register(new Scheme("https", ssf, 443));
	      return new DefaultHttpClient(ccm, base.getParams());
	    } catch (Exception ex) {
	      ex.printStackTrace();
	    }return null;
	  }

	  public static DefaultHttpClient getHttpClient()
	  {
	    return httpclient;
	  }

	  public static DefaultHttpClient getNewHttpClient()
	  {
	    return initHttpClientPool();
	  }

	  public static List<NameValuePair> createNamePair(Map<String, String> vars)
	  {
	    List nvps = new ArrayList();
	    Iterator iterator = vars.keySet().iterator();
	    while (iterator.hasNext()) {
	      String key = (String)iterator.next();
	      nvps.add(new BasicNameValuePair(key, (String)vars.get(key)));
	    }
	    return nvps;
	  }
}
