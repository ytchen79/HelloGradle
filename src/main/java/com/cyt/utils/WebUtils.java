package com.cyt.utils;

import javax.net.ssl.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class WebUtils {
	
	private static SSLContext defaultCtx = null;

	private static SSLSocketFactory defaultSocketFactory = null;
	
	private static class DefaultTrustManager implements X509TrustManager {
		public X509Certificate[] getAcceptedIssuers() {
			return null;
		}

		public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		}

		public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		}
	}

	static {
		try {
			defaultCtx = SSLContext.getInstance("TLS");
			defaultCtx.init(new KeyManager[0], new TrustManager[] { new DefaultTrustManager() }, new SecureRandom());

			defaultCtx.getClientSessionContext().setSessionTimeout(15);
			defaultCtx.getClientSessionContext().setSessionCacheSize(1000);

			defaultSocketFactory = defaultCtx.getSocketFactory();
		} catch (Exception localException) {
		}
	}

	private static HostnameVerifier verifier = new HostnameVerifier() {
		public boolean verify(String hostname, SSLSession session) {
			return false;
		}
	};

	private static final String DEFAULT_CHARSET = "UTF-8";

	private static final String METHOD_POST = "POST";

	private static final String METHOD_GET = "GET";
	
	private static final String HTTPS = "https";
	

	public static String doPost(String url, Map<String, String> params, int connectTimeout, int readTimeout)
			throws IOException {
		return doPost(url, params, connectTimeout, readTimeout, null, null);
	}
	
	public static String doPost(String url, Map<String, String> params, int connectTimeout, int readTimeout, byte[] cert, String passwd)
			throws IOException {
		return doPost(url, params, DEFAULT_CHARSET, connectTimeout, readTimeout, cert, passwd);
	}

	public static String doPost(String url, Map<String, String> params, String charset, int connectTimeout, int readTimeout) throws IOException {
		return doPost(url, params, charset, connectTimeout, readTimeout, null, null);
	}
	
	public static String doPost(String url, Map<String, String> params, String charset, int connectTimeout, int readTimeout, byte[] cert, String passwd) throws IOException {
		String ctype = "application/x-www-form-urlencoded;charset=" + charset;
		String query = buildQuery(params, charset);
		byte[] content = new byte[0];
		if (query != null) {
			content = query.getBytes(charset);
		}
		return doPost(url, ctype, content, connectTimeout, readTimeout, cert, passwd);
	}

	public static String doPost(String url, String ctype, byte[] content, int connectTimeout, int readTimeout) throws IOException {
		return doPost(url, ctype, content, connectTimeout, readTimeout, null, null);
	}
	
	public static String doPost(String url, String ctype, byte[] content, int connectTimeout, int readTimeout, byte[] cert, String passwd) throws IOException {
		HttpURLConnection conn = null;
		conn = getConnection(new URL(url), METHOD_POST, ctype, cert, passwd);
		conn.setConnectTimeout(connectTimeout);
		conn.setReadTimeout(readTimeout);
		try (OutputStream os = conn.getOutputStream()) {
			os.write(content);
			os.flush();
			return getResponseAsString(conn);
		} catch (IOException e) { 
			throw e;
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
	}

	public static String doPost(String url, Map<String, String> params, Map<String, File> fileParams, int connectTimeout, int readTimeout) throws IOException {
		if ((fileParams == null) || (fileParams.isEmpty())) {
			return doPost(url, params, DEFAULT_CHARSET, connectTimeout, readTimeout);
		}
		return doPost(url, params, fileParams, DEFAULT_CHARSET, connectTimeout, readTimeout);
	}

	public static String doPost(String url, Map<String, String> params, Map<String, File> fileParams, String charset, int connectTimeout, int readTimeout) throws IOException {
		if ((fileParams == null) || (fileParams.isEmpty())) {
			return doPost(url, params, charset, connectTimeout, readTimeout);
		}
		String boundary = System.currentTimeMillis() + "";
		HttpURLConnection conn = null;
		String rsp = null;
		try {
			String ctype = "multipart/form-data;boundary=" + boundary + ";charset=" + charset;
			conn = getConnection(new URL(url), METHOD_POST, ctype);
			conn.setConnectTimeout(connectTimeout);
			conn.setReadTimeout(readTimeout);
			try (OutputStream os = conn.getOutputStream()) {
				byte[] entryBoundaryBytes = ("\r\n--" + boundary + "\r\n").getBytes(charset);
	
				for (Entry<String, String> textEntry : params.entrySet()) {
					byte[] textBytes = getTextEntry((String) textEntry.getKey(), (String) textEntry.getValue(), charset);
					os.write(entryBoundaryBytes);
					os.write(textBytes);
				}
				for (Entry<String, File> fileEntry : fileParams.entrySet()) {
					File fileItem = fileEntry.getValue();
					byte[] fileBytes = getFileEntry((String) fileEntry.getKey(), fileItem.getName(), "application/octet-stream", charset);
					os.write(entryBoundaryBytes);
					os.write(fileBytes);
					os.write(FileUtils.getFileBytes(fileItem.getAbsolutePath()));
				}
				byte[] endBoundaryBytes = ("\r\n--" + boundary + "--\r\n").getBytes(charset);
				os.write(endBoundaryBytes);
				rsp = getResponseAsString(conn);
			}
				
		} catch(Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
		return rsp;
	}


	private static byte[] getTextEntry(String fieldName, String fieldValue, String charset) throws IOException {
		StringBuilder entry = new StringBuilder();
		entry.append("Content-Disposition:form-data;name=\"");
		entry.append(fieldName);
		entry.append("\"\r\nContent-Type:text/plain\r\n\r\n");
		entry.append(fieldValue);
		return entry.toString().getBytes(charset);
	}

	private static byte[] getFileEntry(String fieldName, String fileName, String mimeType, String charset)
			throws IOException {
		StringBuilder entry = new StringBuilder();
		entry.append("Content-Disposition:form-data;name=\"");
		entry.append(fieldName);
		entry.append("\";filename=\"");
		entry.append(fileName);
		entry.append("\"\r\nContent-Type:");
		entry.append(mimeType);
		entry.append("\r\n\r\n");
		return entry.toString().getBytes(charset);
	}

	public static String doGet(String url, Map<String, String> params) throws IOException {
		return doGet(url, params, DEFAULT_CHARSET);
	}

	public static String doGet(String url, Map<String, String> params, String charset) throws IOException {
		return doGet(url, buildQuery(params, charset), charset);
	}
	
	public static String doGet(String url, String queryStr, String charset) throws IOException {
		HttpURLConnection conn = null;
		String rsp = null;
		try {
			String ctype = "application/x-www-form-urlencoded;charset=" + charset;
			conn = getConnection(buildGetUrl(url, queryStr), METHOD_GET, ctype);
			rsp = getResponseAsString(conn);
		} catch (IOException e) {
			throw e;
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
		return rsp;
	}

	private static HttpURLConnection getConnection(URL url, String method, String ctype) throws IOException {
		return getConnection(url, method, ctype, null, null);
	}
	
	public static HttpURLConnection getConnection(URL url, String method, String ctype, byte[] cert, String passwd) throws IOException {
		HttpURLConnection conn = null;
		if (HTTPS.equals(url.getProtocol())) {
			HttpsURLConnection connHttps = (HttpsURLConnection) url.openConnection();
			if (cert != null) {
				try {
					KeyStore keyStore  = KeyStore.getInstance("PKCS12");
			        try (InputStream is = new ByteArrayInputStream(cert)){
			            keyStore.load(is, passwd.toCharArray());
			        }
			        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory  
			                .getDefaultAlgorithm());  
			        kmf.init(keyStore, passwd.toCharArray()); 
			        SSLContext ctx = SSLContext.getInstance("TLS");
					ctx.init(kmf.getKeyManagers(), new TrustManager[] { new DefaultTrustManager() }, new SecureRandom());
					ctx.getClientSessionContext().setSessionTimeout(15);
					ctx.getClientSessionContext().setSessionCacheSize(1000);
					SSLSocketFactory socketFactory = ctx.getSocketFactory();
					connHttps.setSSLSocketFactory(socketFactory);
					connHttps.setHostnameVerifier(verifier);
					conn = connHttps;
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			} else {
				connHttps.setSSLSocketFactory(defaultSocketFactory);
				connHttps.setHostnameVerifier(verifier);
				conn = connHttps;
			}
			
		} else {
			conn = (HttpURLConnection) url.openConnection();
		}
		conn.setRequestMethod(method);
		conn.setDoInput(true);
		conn.setDoOutput(true);
		conn.setRequestProperty("Accept", "text/xml,text/javascript,text/html,application/json");
		conn.setRequestProperty("User-Agent", "cyt");
		conn.setRequestProperty("Content-Type", ctype);
		return conn;
	}

	private static URL buildGetUrl(String strUrl, String query) throws IOException {
		URL url = new URL(strUrl);
		if (!StringUtils.hasText(query)) {
			return url;
		}
		if (!StringUtils.hasText(url.getQuery())) {
			if (strUrl.endsWith("?")) {
				strUrl = strUrl + query;
			} else {
				strUrl = strUrl + "?" + query;
			}
		} else if (strUrl.endsWith("&")) {
			strUrl = strUrl + query;
		} else {
			strUrl = strUrl + "&" + query;
		}

		return new URL(strUrl);
	}

	public static String buildQuery(Map<String, String> params, String charset) throws IOException {
		if ((params == null) || (params.isEmpty())) {
			return null;
		}
		StringBuilder query = new StringBuilder();
		boolean hasParam = false;
		for (Entry<String, String> entry : params.entrySet()) {
			String name = (String) entry.getKey();
			String value = (String) entry.getValue();
			if (StringUtils.areNotEmpty(new String[] { name, value })) {
				if (hasParam) {
					query.append("&");
				} else {
					hasParam = true;
				}
				query.append(name).append("=").append(encode(value, charset));
			}
		}
		return query.toString();
	}

	public static String getASCIISortString(Map<String, String> params) {
		if (params == null || params.isEmpty()) {
			return "";
		}
		Map<String, String> sortMap = new TreeMap<>(params);
		StringBuilder sortStringBuider = new StringBuilder(128);
		for (Entry<String, String> entry : sortMap.entrySet()) {
			if (sortStringBuider.length() > 0) {
				sortStringBuider.append("&");
			}
			sortStringBuider.append(entry.getKey()).append("=").append(entry.getValue());
		}
		return sortStringBuider.toString();
	}

	protected static String getResponseAsString(HttpURLConnection conn) throws IOException {
		String charset = getResponseCharset(conn.getContentType());
		InputStream es = conn.getErrorStream();
		if (es == null) {
			return getStreamAsString(conn.getInputStream(), charset);
		}
		String msg = getStreamAsString(es, charset);
		if (StringUtils.isEmpty(msg)) {
			throw new IOException(conn.getResponseCode() + ":" + conn.getResponseMessage());
		}
		return msg;
	}

	private static String getStreamAsString(InputStream stream, String charset) throws IOException {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(stream, charset));
			StringWriter writer = new StringWriter();
			char[] chars = new char['?'];
			int count = 0;
			while ((count = reader.read(chars)) > 0) {
				writer.write(chars, 0, count);
			}
			return writer.toString();
		} finally {
			if (stream != null) {
				stream.close();
			}
		}
	}

	private static String getResponseCharset(String ctype) {
		String charset = DEFAULT_CHARSET;
		if (StringUtils.hasText(ctype)) {
			String[] params = ctype.split(";");
			for (String param : params) {
				param = param.trim();
				if (param.startsWith("charset")) {
					String[] pair = param.split("=", 2);
					if ((pair.length != 2) || (StringUtils.isEmpty(pair[1])))
						break;
					charset = pair[1].trim();
					break;
				}
			}
		}

		return charset;
	}

	public static String encode(String str) {
		return encode(str, DEFAULT_CHARSET);
	}

	public static String encode(String str, String charsetName) {
		String result = null;
		if (str != null) {
			try {
				result = URLEncoder.encode(str, charsetName);
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}
		}
		return result;
	}

	public static String decode(String str) {
		return decode(str, DEFAULT_CHARSET);
	}

	public static String decode(String str, String charsetName) {
		String result = null;
		if (str != null) {
			try {
				result = URLDecoder.decode(str, charsetName);
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}
		}
		return result;
	}

	private static Map<String, String> getParamsFromUrl(String url) {
		Map<String, String> map = null;
		if ((url != null) && (url.indexOf('?') != -1)) {
			map = splitUrlQuery(url.substring(url.indexOf('?') + 1));
		}
		return map == null ? new HashMap<String, String>() : map;
	}

	public static Map<String, String> splitUrlQuery(String query) {
		Map<String, String> result = new HashMap<>();
		String[] pairs = query.split("&");
		if ((pairs != null) && (pairs.length > 0)) {
			for (String pair : pairs) {
				String[] param = pair.split("=", 2);
				if ((param != null) && (param.length == 2)) {
					result.put(param[0], param[1]);
				}
			}
		}
		return result;
	}
	public static void main(String args[]) {
		try{
			Map<String,String> parm= new HashMap<>();
//			parm.put("test","test");
			String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=wxef31e694d92863a0&secret=45205df6fc094219eea156d78b21c257";
			System.out.println(
					WebUtils.doGet(url, parm, DEFAULT_CHARSET)
			);
		}catch (Exception e){
			e.printStackTrace();
		}
	}
}
