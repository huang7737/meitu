package com.sinosafe.util.http;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import com.sinosafe.util.security.AssertUtils;
import com.sinosafe.util.security.certificate.GalaxyX509TrustManager;


public class HttpJson {  
    
    public enum Method {  
        GET, POST  
    }  
      
    public static HttpJson connect(String url) {  
        HttpJson http = new HttpJson();  
        http.url(url);  
        return http;  
    }  
  
    public static HttpJson connect(URL url) {  
        HttpJson http = new HttpJson();  
        http.url(url);  
        return http;  
    }  
  
    private HttpJson.Request req;  
    private HttpJson.Response res;  
  
    private HttpJson() {  
        req = new Request();  
        res = new Response();  
    }  
  
    public HttpJson url(URL url) {  
        req.url(url);  
        return this;  
    }  
  
    public HttpJson url(String url) {  
    	AssertUtils.hasText(url, "Must supply a valid URL");
        try {  
            req.url(new URL(url));  
        } catch (MalformedURLException e) {  
            throw new IllegalArgumentException("Malformed URL: " + url, e);  
        }  
        return this;  
    }  
      
    public HttpJson userAgent(String userAgent) {  
    	AssertUtils.notNull(userAgent, "User agent must not be null");  
        req.header("User-Agent", userAgent);  
        return this;  
    }  
  
    public HttpJson timeout(int millis) {  
        req.timeout(millis);  
        return this;  
    }  
  
    public HttpJson maxBodySize(int bytes) {  
        req.maxBodySize(bytes);  
        return this;  
    }  
  
    public HttpJson followRedirects(boolean followRedirects) {  
        req.followRedirects(followRedirects);  
        return this;  
    }  
  
    public HttpJson referrer(String referrer) {  
    	AssertUtils.notNull(referrer, "Referrer must not be null");  
        req.header("Referer", referrer);  
        return this;  
    }  
  
    public HttpJson method(Method method) {  
        req.method(method);  
        return this;  
    }  
  
    public HttpJson ignoreHttpErrors(boolean ignoreHttpErrors) {  
        req.ignoreHttpErrors(ignoreHttpErrors);  
        return this;  
    }  
  
    public HttpJson data(String key, String value) {  
        req.data(KeyVal.create(key, value));  
        return this;  
    }  
  
    public HttpJson data(Map<String, String> data) {  
    	AssertUtils.notNull(data, "Data map must not be null");  
        for (Map.Entry<String, String> entry : data.entrySet()) {  
            req.data(KeyVal.create(entry.getKey(), entry.getValue()));  
        }  
        return this;  
    }  
  
  
    public HttpJson header(String name, String value) {  
        req.header(name, value);  
        return this;  
    }  
  
    public HttpJson cookie(String name, String value) {  
        req.cookie(name, value);  
        return this;  
    }  
  
    public HttpJson cookies(Map<String, String> cookies) {  
    	AssertUtils.notNull(cookies, "Cookie map must not be null");  
        for (Map.Entry<String, String> entry : cookies.entrySet()) {  
            req.cookie(entry.getKey(), entry.getValue());  
        }  
        return this;  
    }  
  
    public HttpJson.Response get() throws Exception {  
        req.method(Method.GET);  
        res = Response.execute(req);  
        return res;  
    }  
  
    public HttpJson.Response post() throws Exception {  
        req.method(Method.POST);  
        res = Response.execute(req);  
        return res;  
    }  
    public HttpJson.Response post(String data) throws Exception {  
        req.method(Method.POST);  
        res = Response.execute(req,data);  
        return res;  
    } 
    public HttpJson.Request request() {  
        return req;  
    }  
  
    public HttpJson request(HttpJson.Request request) {  
        req = request;  
        return this;  
    }  
      
    public HttpJson charset(String charset) {  
    	AssertUtils.isTrue(Charset.isSupported(charset), String.format("The charset of '%s' is not supported", charset));  
        req.charset(charset);  
        return this;  
    }  
  
    @SuppressWarnings({ "unchecked", "rawtypes" })  
    private static abstract class Base<T extends Base> {  
        private URL url;  
        private Method method;  
        private Map<String, String> headers;  
        private Map<String, String> cookies;  
  
        private Base() {  
            headers = new LinkedHashMap<String, String>();  
            cookies = new LinkedHashMap<String, String>();  
        }  
  
        public URL url() {  
            return url;  
        }  
  
        public T url(URL url) {  
        	AssertUtils.notNull(url, "URL must not be null");  
            this.url = url;  
            return (T) this;  
        }  
  
        public Method method() {  
            return method;  
        }  
  
        public T method(Method method) {  
        	AssertUtils.notNull(method, "Method must not be null");  
            this.method = method;  
            return (T) this;  
        }  
  
        public String header(String name) {  
        	AssertUtils.notNull(name, "Header name must not be null");  
            return getHeaderCaseInsensitive(name);  
        }  
  
        public T header(String name, String value) {  
        	AssertUtils.hasText(name, "Header name must not be empty");  
        	AssertUtils.notNull(value, "Header value must not be null");  
            removeHeader(name); 
            headers.put(name, value);  
            return (T) this;  
        }  
  
        public boolean hasHeader(String name) {  
            AssertUtils.hasText(name, "Header name must not be empty");  
            return getHeaderCaseInsensitive(name) != null;  
        }  
  
        public T removeHeader(String name) {  
            AssertUtils.hasText(name, "Header name must not be empty");  
            Map.Entry<String, String> entry = scanHeaders(name); 
            if (entry != null) {  
                headers.remove(entry.getKey()); 
            }  
            return (T) this;  
        }  
  
        public Map<String, String> headers() {  
            return headers;  
        }  
  
        private String getHeaderCaseInsensitive(String name) {  
            AssertUtils.notNull(name, "Header name must not be null");  
            
            String value = headers.get(name);  
            if (value == null) {  
                value = headers.get(name.toLowerCase());  
            }  
            if (value == null) {  
                Map.Entry<String, String> entry = scanHeaders(name);  
                if (entry != null) {  
                    value = entry.getValue();  
                }  
            }  
            return value;  
        }  
  
        private Map.Entry<String, String> scanHeaders(String name) {  
            String lc = name.toLowerCase();  
            for (Map.Entry<String, String> entry : headers.entrySet()) {  
                if (entry.getKey().toLowerCase().equals(lc)) {  
                    return entry;  
                }  
            }  
            return null;  
        }  
  
        public String cookie(String name) {  
            AssertUtils.notNull(name, "Cookie name must not be null");  
            return cookies.get(name);  
        }  
  
        public T cookie(String name, String value) {  
            AssertUtils.hasText(name, "Cookie name must not be empty");  
            AssertUtils.notNull(value, "Cookie value must not be null");  
            cookies.put(name, value);  
            return (T) this;  
        }  
  
        public boolean hasCookie(String name) {  
            AssertUtils.hasText(name,"Cookie name must not be empty");  
            return cookies.containsKey(name);  
        }  
  
        public T removeCookie(String name) {  
            AssertUtils.hasText(name,"Cookie name must not be empty");  
            cookies.remove(name);  
            return (T) this;  
        }  
  
        public Map<String, String> cookies() {  
            return cookies;  
        }  
    }  
  
    public static class Request extends Base<Request> {  
          
        private int timeoutMilliseconds;  
        private int maxBodySizeBytes;  
        private boolean followRedirects;  
        private Collection<HttpJson.KeyVal> data;
        private boolean ignoreHttpErrors = false;  
        private String charset;  
  
        private Request() {  
            timeoutMilliseconds = 3000;  
            maxBodySizeBytes = 3 * 1024 * 1024; 
            followRedirects = true;  
            data = new ArrayList<HttpJson.KeyVal>();  
            super.method = Method.GET;  
            super.headers.put("Accept-Encoding", "gzip");  
        }  
  
        public int timeout() {  
            return timeoutMilliseconds;  
        }  
  
        public Request timeout(int millis) {  
            AssertUtils.isTrue(millis >= 0, "Timeout milliseconds must be 0 (infinite) or greater");  
            timeoutMilliseconds = millis;  
            return this;  
        }  
  
        public int maxBodySize() {  
            return maxBodySizeBytes;  
        }  
  
        public HttpJson.Request maxBodySize(int bytes) {  
            AssertUtils.isTrue(bytes >= 0, "maxSize must be 0 (unlimited) or larger");  
            maxBodySizeBytes = bytes;  
            return this;  
        }  
  
        public boolean followRedirects() {  
            return followRedirects;  
        }  
  
        public HttpJson.Request followRedirects(boolean followRedirects) {  
            this.followRedirects = followRedirects;  
            return this;  
        }  
  
        public boolean ignoreHttpErrors() {  
            return ignoreHttpErrors;  
        }  
  
        public HttpJson.Request ignoreHttpErrors(boolean ignoreHttpErrors) {  
            this.ignoreHttpErrors = ignoreHttpErrors;  
            return this;  
        }  
  
        public Request data(HttpJson.KeyVal keyval) {  
            AssertUtils.notNull(keyval, "Key val must not be null");  
            data.add(keyval);  
            return this;  
        }  
  
        public Collection<HttpJson.KeyVal> data() {  
            return data;  
        }  
          
        public String charset() {  
            return charset;  
        }  
          
        public void charset(String charset) {  
            this.charset = charset;  
        }  
          
    }  
  
    public static class Response extends Base<Response> {  
          
        private static final String defaultCharset = "UTF-8"; 
          
        private static final int MAX_REDIRECTS = 20;  
        private int statusCode;  
        private String statusMessage;  
        private ByteBuffer byteData;  
        private String charset;  
        private String contentType;  
        private boolean executed = false;  
        private int numRedirects = 0;  
        @SuppressWarnings("unused")  
        private HttpJson.Request req;  
  
        Response() {  
            super();  
        }  
  
        private Response(Response previousResponse) throws IOException {  
            super();  
            if (previousResponse != null) {  
                numRedirects = previousResponse.numRedirects + 1;  
                if (numRedirects >= MAX_REDIRECTS) {  
                    throw new IOException(String.format("Too many redirects occurred trying to load URL %s", previousResponse.url()));  
                }  
            }  
        }  
        private static Response execute(HttpJson.Request req,String data) throws Exception {  
            return execute(req, null,data);  
        }  
        private static Response execute(HttpJson.Request req) throws Exception {  
            return execute(req, null,null);  
        }  
  
        private static Response execute(HttpJson.Request req, Response previousResponse,String data) throws Exception {  
              
            AssertUtils.notNull(req, "Request must not be null");  
            String protocol = req.url().getProtocol(); 
            if (!protocol.equals("http") && !protocol.equals("https")) {  
                throw new MalformedURLException("Only http & https protocols supported");  
            }  
            
            if (req.method() == HttpJson.Method.GET && req.data().size() > 0) {  
                serialiseRequestUrl(req); 
            }  
            HttpURLConnection conn = createConnection(req);
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);
            conn.setRequestProperty("Content-type", "application/json");
            Response res;  
            try {  
                conn.connect();  
                if (req.method() == Method.POST) {  
                	writePost(req.data(),data, conn.getOutputStream());  
                }  
                int status = conn.getResponseCode();  
                boolean needsRedirect = false;  
                if (status != HttpURLConnection.HTTP_OK) {  
                    if (status == HttpURLConnection.HTTP_MOVED_TEMP || status == HttpURLConnection.HTTP_MOVED_PERM || status == HttpURLConnection.HTTP_SEE_OTHER) {  
                        needsRedirect = true;  
                    } else if (!req.ignoreHttpErrors()) {  
                        throw new HttpStatusException("HTTP error fetching URL", status, req.url().toString());  
                    }  
                }  
                res = new Response(previousResponse);  
                res.setupFromConnection(conn, previousResponse);  
                if (needsRedirect && req.followRedirects()) {  
                    req.method(Method.GET); 
                    req.data().clear();  
                    req.url(new URL(req.url(), res.header("Location")));  
                    for (Map.Entry<String, String> cookie : res.cookies().entrySet()) { 
                        req.cookie(cookie.getKey(), cookie.getValue());  
                    }  
                    return execute(req, res,null);  
                }  
                res.req = req;  
  
                InputStream dataStream = null;  
                InputStream bodyStream = null;  
                try {  
                    dataStream = conn.getErrorStream() != null ? conn.getErrorStream() : conn.getInputStream();  
                    bodyStream = res.hasHeader("Content-Encoding") && res.header("Content-Encoding").equalsIgnoreCase("gzip") ?  
                            new BufferedInputStream(new GZIPInputStream(dataStream)) :  
                            new BufferedInputStream(dataStream);  
  
                    res.byteData = readToByteBuffer(bodyStream, req.maxBodySize());  
                    if(req.charset() == null) {  
                        res.charset = getCharsetFromContentType(res.contentType); 
                    } else {  
                        res.charset = req.charset;  
                    }  
                } finally {  
                    if (bodyStream != null) {   
                        bodyStream.close();  
                    }  
                    if (dataStream != null) {  
                        dataStream.close();  
                    }  
                }  
            } finally {  
                conn.disconnect();  
            }  
  
            res.executed = true;  
            return res;  
        }  
  
        public int statusCode() {  
            return statusCode;  
        }  
  
        public String statusMessage() {  
            return statusMessage;  
        }  
  
        public String charset() {  
            return charset;  
        }  
          
        public String contentType() {  
            return contentType;  
        }  
  
        public String html() throws Exception {  
              
            AssertUtils.isTrue(executed, "Request must be executed (with .execute(), .get(), or .post() before parsing response");  
              
            String content = null;  
              
            if(charset == null) {  
                  
                content = Charset.forName(defaultCharset).decode(byteData).toString();  
                  
                Pattern pattern = Pattern.compile("<meta[^>]*content=\"(.+?)\"[^>]*>");  
                  
                Matcher matcher = pattern.matcher(content);  
                  
                String foundCharset = null;  
                  
                while (matcher.find()) {  
                    foundCharset = getCharsetFromContentType(matcher.group(1));  
                    if(foundCharset != null) {  
                        break;  
                    }  
                }  
                  
                if(foundCharset == null) {  
                    pattern = Pattern.compile("<meta[^>]*charset=\"(.+?)\"[^>]*>");  
                    matcher = pattern.matcher(content);  
                    while (matcher.find()) {  
                        foundCharset = matcher.group(1);  
                        if(foundCharset != null) {  
                            break;  
                        }  
                    }  
                }  
                  
                if(foundCharset != null) {  
                    charset = foundCharset;  
                    content = Charset.forName(foundCharset).decode(byteData).toString();  
                }  
            } else {  
                content = Charset.forName(charset).decode(byteData).toString();  
            }  
              
            if (content.length() > 0 && content.charAt(0) == 65279) {  
                content = content.substring(1);  
            }  
            byteData.rewind();  
              
            return content;  
        }  
          
        public void toFile(String to) {  
              
            AssertUtils.notNull(to, "Data value must not be null");  
              
            makeDirs(to);  
              
            generateFile(to);  
              
        }  
          
        private static void makeDirs(String path) {  
            try {  
                int i = path.lastIndexOf("/");  
                if (i < 1) {  
                    i = path.lastIndexOf("\\");  
                }  
                path = path.substring(0, i);  
                File file = new File(path);  
                if (!file.exists()) {  
                    file.mkdirs();  
                }  
            } catch (Exception e) {  
                e.printStackTrace();  
            }  
        }  
          
        private void generateFile(String to) {  
            OutputStream bos = null;  
            try {  
                bos = new BufferedOutputStream(new FileOutputStream(to));  
                if(byteData != null) {  
                    bos.write(byteData.array(), 0, byteData.array().length);  
                }  
                bos.flush();  
            } catch (FileNotFoundException e) {  
                e.printStackTrace();  
            } catch (IOException e) {  
                e.printStackTrace();  
            } finally {  
                if (bos != null) {  
                    try {  
                        bos.close();  
                    } catch (IOException e) {  
                        e.printStackTrace();  
                    }  
                }  
            }  
        }  
  
        
        private static HttpURLConnection createConnection(HttpJson.Request req) throws Exception { 
        	 
        	
            HttpURLConnection conn = (HttpURLConnection) req.url().openConnection();  
            conn.setRequestMethod(req.method().name());  
            conn.setInstanceFollowRedirects(false); 
            conn.setConnectTimeout(req.timeout());  
            conn.setReadTimeout(req.timeout());  
            if (req.method() == Method.POST) {  
                conn.setDoOutput(true);  
            }  
            if (req.cookies().size() > 0) {  
                conn.addRequestProperty("Cookie", getRequestCookieString(req));  
            }  
            for (Map.Entry<String, String> header : req.headers().entrySet()) {  
                conn.addRequestProperty(header.getKey(), header.getValue());  
            }  
            if(req.url().getProtocol().equals("https")){
            	TrustManager[] tm = { new GalaxyX509TrustManager() };  
                SSLContext sslContext = SSLContext.getInstance("SSL", "SunJSSE");  
                sslContext.init(null, tm, new java.security.SecureRandom());  
                
                SSLSocketFactory ssf = sslContext.getSocketFactory();  
                HttpsURLConnection conn2=(HttpsURLConnection)conn;
                conn2.setSSLSocketFactory(ssf);  
            }
            return conn;  
        }  
  
        
        private void setupFromConnection(HttpURLConnection conn, HttpJson.Response previousResponse) throws IOException {  
            super.method = HttpJson.Method.valueOf(conn.getRequestMethod());  
            super.url = conn.getURL();  
            statusCode = conn.getResponseCode();  
            statusMessage = conn.getResponseMessage();  
            contentType = conn.getContentType();  
  
            Map<String, List<String>> resHeaders = conn.getHeaderFields();  
            processResponseHeaders(resHeaders);  
  
            
            if (previousResponse != null) {  
                for (Map.Entry<String, String> prevCookie : previousResponse.cookies().entrySet()) {  
                    if (!hasCookie(prevCookie.getKey())) {  
                        cookie(prevCookie.getKey(), prevCookie.getValue());  
                    }  
                }  
            }  
        }  
  
        private void processResponseHeaders(Map<String, List<String>> resHeaders) {  
            for (Map.Entry<String, List<String>> entry : resHeaders.entrySet()) {  
                String name = entry.getKey();  
                if (name == null) {  
                    continue; 
                }  
  
                List<String> values = entry.getValue();  
                if (name.equalsIgnoreCase("Set-Cookie")) {  
                    for (String value : values) {  
                        if (value == null){  
                            continue;  
                        }  
                        TokenQueue cd = new TokenQueue(value);  
                        String cookieName = cd.chompTo("=").trim();  
                        String cookieVal = cd.consumeTo(";").trim();  
                        if (cookieVal == null) {  
                            cookieVal = "";  
                        }  
                        
                        
                        if (cookieName != null && cookieName.length() > 0) {  
                            cookie(cookieName, cookieVal);  
                        }  
                    }  
                } else { 
                    if (!values.isEmpty()) {  
                        header(name, values.get(0));  
                    }  
                }  
            }  
        }  
  
        private static void writePost(Collection<HttpJson.KeyVal> data,String strData, OutputStream outputStream) throws IOException {  
        	
            OutputStreamWriter w = new OutputStreamWriter(outputStream, defaultCharset);  
            
            if(strData!=null&&!strData.trim().equals("")){
            	 w.write(strData); 
            }
            boolean first = true; 
            if(data!=null&&data.size()>0){
	            for (HttpJson.KeyVal keyVal : data) {  
	                if (!first) {  
	                    w.append('&');  
	                } else {  
	                    first = false;  
	                }  
	                  
	                w.write(URLEncoder.encode(keyVal.key(), defaultCharset));  
	                w.write('=');  
	                w.write(URLEncoder.encode(keyVal.value(), defaultCharset));  
	            }
            }
            w.close();  
        }  





        private static String getRequestCookieString(HttpJson.Request req) {  
            StringBuilder sb = new StringBuilder();  
            boolean first = true;  
            for (Map.Entry<String, String> cookie : req.cookies().entrySet()) {  
                if (!first) {  
                    sb.append("; ");  
                } else {  
                    first = false;  
                }  
                sb.append(cookie.getKey()).append('=').append(cookie.getValue());  
            }  
            return sb.toString();  
        }  
  
        
        private static void serialiseRequestUrl(HttpJson.Request req) throws IOException {  
            URL in = req.url();  
            StringBuilder url = new StringBuilder();  
            boolean first = true;  
            
            url.append(in.getProtocol())  
                .append("://")  
                .append(in.getAuthority()) 
                .append(in.getPath())  
                .append("?");  
            if (in.getQuery() != null) {  
                url.append(in.getQuery());  
                first = false;  
            }  
            for (HttpJson.KeyVal keyVal : req.data()) {  
                if (!first) {  
                    url.append('&');  
                } else {  
                    first = false;  
                }  
                url.append(URLEncoder.encode(keyVal.key(), defaultCharset))  
                    .append('=')  
                    .append(URLEncoder.encode(keyVal.value(), defaultCharset));  
            }  
            req.url(new URL(url.toString()));  
            req.data().clear(); 
        }  
    }  
      
    private static class TokenQueue {  
        private String queue;  
        private int pos = 0;  
          
        public TokenQueue(String data) {  
            AssertUtils.notNull(data);  
            queue = data;  
        }  
  
        public boolean isEmpty() {  
            return remainingLength() == 0;  
        }  
          
        private int remainingLength() {  
            return queue.length() - pos;  
        }  
  
        public boolean matches(String seq) {  
            return queue.regionMatches(true, pos, seq, 0, seq.length());  
        }  
  
        public boolean matchChomp(String seq) {  
            if (matches(seq)) {  
                pos += seq.length();  
                return true;  
            } else {  
                return false;  
            }  
        }  
  
        public char consume() {  
            return queue.charAt(pos++);  
        }  
  
        public String consumeTo(String seq) {  
            int offset = queue.indexOf(seq, pos);  
            if (offset != -1) {  
                String consumed = queue.substring(pos, offset);  
                pos += consumed.length();  
                return consumed;  
            } else {  
                return remainder();  
            }  
        }  
          
        public String chompTo(String seq) {  
            String data = consumeTo(seq);  
            matchChomp(seq);  
            return data;  
        }  
  
        public String remainder() {  
            StringBuilder accum = new StringBuilder();  
            while (!isEmpty()) {  
                accum.append(consume());  
            }  
            return accum.toString();  
        }  
          
        public String toString() {  
            return queue.substring(pos);  
        }  
    }  
  
    public static class KeyVal {  
          
        private String key;  
        private String value;  
  
        public static KeyVal create(String key, String value) {  
            AssertUtils.hasText(key, "Data key must not be empty");  
            AssertUtils.notNull(value, "Data value must not be null");  
            return new KeyVal(key, value);  
        }  
  
        private KeyVal(String key, String value) {  
            this.key = key;  
            this.value = value;  
        }  
  
        public KeyVal key(String key) {  
            AssertUtils.hasText(key, "Data key must not be empty");  
            this.key = key;  
            return this;  
        }  
  
        public String key() {  
            return key;  
        }  
  
        public KeyVal value(String value) {  
            AssertUtils.notNull(value, "Data value must not be null");  
            this.value = value;  
            return this;  
        }  
  
        public String value() {  
            return value;  
        }  
  
    }  
      
    @SuppressWarnings("unused")  
    private static class HttpStatusException extends IOException {  
  
        private static final long serialVersionUID = -2926428810498166324L;  
        private int statusCode;  
        private String url;  
  
        public HttpStatusException(String message, int statusCode, String url) {  
            super(message);  
            this.statusCode = statusCode;  
            this.url = url;  
        }  
  
        public int getStatusCode() {  
            return statusCode;  
        }  
  
        public String getUrl() {  
            return url;  
        }  
  
        public String toString() {  
            return super.toString() + ". Status=" + statusCode + ", URL=" + url;  
        }  
    }  
      
    @SuppressWarnings("unused")  
    private static class UnsupportedMimeTypeException extends IOException {  
          
        private static final long serialVersionUID = 2535952512520299658L;  
        private String mimeType;  
        private String url;  
  
        public UnsupportedMimeTypeException(String message, String mimeType, String url) {  
            super(message);  
            this.mimeType = mimeType;  
            this.url = url;  
        }  
  
        public String getMimeType() {  
            return mimeType;  
        }  
  
        public String getUrl() {  
            return url;  
        }  
  
        public String toString() {  
            return super.toString() + ". Mimetype=" + mimeType + ", URL="+url;  
        }  
    }  
      
    private static ByteBuffer readToByteBuffer(InputStream inStream, int maxSize) throws IOException {  
        AssertUtils.isTrue(maxSize >= 0, "maxSize must be 0 (unlimited) or larger");  
        final boolean capped = maxSize > 0;  
        byte[] buffer = new byte[0x20000];  
        ByteArrayOutputStream outStream = new ByteArrayOutputStream(0x20000);  
        int read;  
        int remaining = maxSize;  
  
        while (true) {  
            read = inStream.read(buffer);  
            if (read == -1) break;  
            if (capped) {  
                if (read > remaining) {  
                    outStream.write(buffer, 0, remaining);  
                    break;  
                }  
                remaining -= read;  
            }  
            outStream.write(buffer, 0, read);  
        }  
        ByteBuffer byteData = ByteBuffer.wrap(outStream.toByteArray());  
        return byteData;  
    }  
      
    private static String getCharsetFromContentType(String contentType) {  
        if (contentType == null) {  
            return null;  
        }  
        Pattern charsetPattern = Pattern.compile("(?i)\\bcharset=\\s*\"?([^\\s;\"]*)");  
        Matcher m = charsetPattern.matcher(contentType);  
        if (m.find()) {  
            String charset = m.group(1).trim();  
            if (Charset.isSupported(charset)) {  
                return charset;  
            }  
            charset = charset.toUpperCase(Locale.ENGLISH);  
            if (Charset.isSupported(charset)) {  
                return charset;  
            }  
        }  
        return null;  
    }  
    

}
