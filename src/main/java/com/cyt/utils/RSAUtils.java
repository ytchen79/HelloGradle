package com.cyt.utils;

import com.cyt.utils.consts.CharsetNameEnum;

import javax.crypto.Cipher;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

public class RSAUtils {

	public static String rsaSign(String content, String privateKey, String charset, String signType) {
		if ("RSA".equals(signType)) {
			return rsaSign(content, privateKey, charset);
		}
		if ("RSA2".equals(signType)) {
			return rsa256Sign(content, privateKey, charset);
		}
		throw new RuntimeException("Sign Type is Not Support : signType=" + signType);
	}

	public static String rsa256Sign(String content, String privateKey, String charset) {
		try {
			PrivateKey priKey = getPrivateKeyFromPKCS8("RSA", new ByteArrayInputStream(privateKey.getBytes()));

			Signature signature = Signature.getInstance("SHA256WithRSA");

			signature.initSign(priKey);

			if (!StringUtils.hasText(charset)) {
				signature.update(content.getBytes());
			} else {
				signature.update(content.getBytes(charset));
			}

			byte[] signed = signature.sign();
			return new String(Base64Utils.encode(signed));
		} catch (Exception e) {
			throw new RuntimeException("RSAcontent = " + content + "; charset = " + charset, e);
		}
	}

	public static String rsaSign(String content, String privateKey, String charset) {
		try {
			PrivateKey priKey = getPrivateKeyFromPKCS8("RSA", new ByteArrayInputStream(privateKey.getBytes()));

			Signature signature = Signature.getInstance("SHA1WithRSA");

			signature.initSign(priKey);

			if (!StringUtils.hasText(charset)) {
				signature.update(content.getBytes());
			} else {
				signature.update(content.getBytes(charset));
			}

			byte[] signed = signature.sign();

			return new String(Base64Utils.encode(signed));
		} catch (InvalidKeySpecException ie) {
			throw new RuntimeException("RSA私钥格式不正确，请检查是否正确配置了PKCS8格式的私钥", ie);
		} catch (Exception e) {
			throw new RuntimeException("RSAcontent = " + content + "; charset = " + charset, e);
		}
	}

	public static String rsaSign(Map<String, String> params, String privateKey, String charset)
			throws RuntimeException {
		String signContent = getSignContent(params);

		return rsaSign(signContent, privateKey, charset);
	}

	public static String getSignContent(Map<String, String> params) {
		StringBuilder contentBuilder = new StringBuilder();
		TreeMap<String, String> sortedMap = new TreeMap<>(params);
		for (Map.Entry<String, String> entry : sortedMap.entrySet()) {
			if (contentBuilder.length() != 0) {
				contentBuilder.append("&");
			}
			contentBuilder.append(entry.getKey()).append("=").append(entry.getValue());
		}
		return contentBuilder.toString();
	}

	public static PrivateKey getPrivateKeyFromPKCS8(String algorithm, InputStream ins) throws Exception {
		if ((ins == null) || (!StringUtils.hasText(algorithm))) {
			return null;
		}

		KeyFactory keyFactory = KeyFactory.getInstance(algorithm);

		byte[] encodedKey = Base64Utils.decode(StreamUtils.copyToString(ins, CharsetNameEnum.UTF_8.charset()));

		return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(encodedKey));
	}

	public static String getSignCheckContentV1(Map<String, String> params) {
		if (params == null) {
			return null;
		}

		params.remove("sign");
		params.remove("sign_type");

		StringBuffer content = new StringBuffer();
		List<String> keys = new ArrayList(params.keySet());
		Collections.sort(keys);

		for (int i = 0; i < keys.size(); i++) {
			String key = (String) keys.get(i);
			String value = (String) params.get(key);
			content.append((i == 0 ? "" : "&") + key + "=" + value);
		}

		return content.toString();
	}

	public static String getSignCheckContentV2(Map<String, String> params) {
		if (params == null) {
			return null;
		}

		params.remove("sign");

		StringBuffer content = new StringBuffer();
		List<String> keys = new ArrayList(params.keySet());
		Collections.sort(keys);

		for (int i = 0; i < keys.size(); i++) {
			String key = (String) keys.get(i);
			String value = (String) params.get(key);
			content.append((i == 0 ? "" : "&") + key + "=" + value);
		}

		return content.toString();
	}

	public static boolean rsaCheckV1(Map<String, String> params, String publicKey, String charset)
			throws RuntimeException {
		String sign = (String) params.get("sign");
		String content = getSignCheckContentV1(params);

		return rsaCheckContent(content, sign, publicKey, charset);
	}

	public static boolean rsaCheckV1(Map<String, String> params, String publicKey, String charset, String signType)
			throws RuntimeException {
		String sign = (String) params.get("sign");
		String content = getSignCheckContentV1(params);

		return rsaCheck(content, sign, publicKey, charset, signType);
	}

	public static boolean rsaCheckV2(Map<String, String> params, String publicKey, String charset)
			throws RuntimeException {
		String sign = (String) params.get("sign");
		String content = getSignCheckContentV2(params);

		return rsaCheckContent(content, sign, publicKey, charset);
	}

	public static boolean rsaCheckV2(Map<String, String> params, String publicKey, String charset, String signType)
			throws RuntimeException {
		String sign = (String) params.get("sign");
		String content = getSignCheckContentV2(params);

		return rsaCheck(content, sign, publicKey, charset, signType);
	}

	public static boolean rsaCheck(String content, String sign, String publicKey, String charset, String signType)
			throws RuntimeException {
		if ("RSA".equals(signType)) {
			return rsaCheckContent(content, sign, publicKey, charset);
		}
		if ("RSA2".equals(signType)) {
			return rsa256CheckContent(content, sign, publicKey, charset);
		}

		throw new RuntimeException("Sign Type is Not Support : signType=" + signType);
	}

	public static boolean rsa256CheckContent(String content, String sign, String publicKey, String charset)
			throws RuntimeException {
		try {
			PublicKey pubKey = getPublicKeyFromX509("RSA", new ByteArrayInputStream(publicKey.getBytes()));

			Signature signature = Signature.getInstance("SHA256WithRSA");

			signature.initVerify(pubKey);

			if (!StringUtils.hasText(charset)) {
				signature.update(content.getBytes());
			} else {
				signature.update(content.getBytes(charset));
			}

			return signature.verify(Base64Utils.decode(sign));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static boolean rsaCheckContent(String content, String sign, String publicKey, String charset)
			throws RuntimeException {
		try {
			PublicKey pubKey = getPublicKeyFromX509("RSA", new ByteArrayInputStream(publicKey.getBytes()));

			Signature signature = Signature.getInstance("SHA1WithRSA");

			signature.initVerify(pubKey);

			if (!StringUtils.hasText(charset)) {
				signature.update(content.getBytes());
			} else {
				signature.update(content.getBytes(charset));
			}

			return signature.verify(Base64Utils.decode(sign));
		} catch (Exception e) {
			throw new RuntimeException("RSAcontent = " + content + ",sign=" + sign + ",charset = " + charset, e);
		}
	}

	public static PublicKey getPublicKeyFromX509(String algorithm, InputStream ins) throws Exception {
		KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
		byte[] encodedKey = Base64Utils.decode(StreamUtils.copyToString(ins, CharsetNameEnum.UTF_8.charset()));
		return keyFactory.generatePublic(new X509EncodedKeySpec(encodedKey));
	}

	public static String checkSignAndDecrypt(Map<String, String> params, String alipayPublicKey, String cusPrivateKey,
			boolean isCheckSign, boolean isDecrypt) throws RuntimeException {
		String charset = (String) params.get("charset");
		String bizContent = (String) params.get("biz_content");
		if ((isCheckSign) && (!rsaCheckV2(params, alipayPublicKey, charset))) {
			throw new RuntimeException("rsaCheck failure:rsaParams=" + params);
		}

		if (isDecrypt) {
			return rsaDecrypt(bizContent, cusPrivateKey, charset);
		}

		return bizContent;
	}

	public static String checkSignAndDecrypt(Map<String, String> params, String alipayPublicKey, String cusPrivateKey,
			boolean isCheckSign, boolean isDecrypt, String signType) throws RuntimeException {
		String charset = (String) params.get("charset");
		String bizContent = (String) params.get("biz_content");
		if ((isCheckSign) && (!rsaCheckV2(params, alipayPublicKey, charset, signType))) {
			throw new RuntimeException("rsaCheck failure:rsaParams=" + params);
		}

		if (isDecrypt) {
			return rsaDecrypt(bizContent, cusPrivateKey, charset);
		}

		return bizContent;
	}

	public static String encryptAndSign(String bizContent, String alipayPublicKey, String cusPrivateKey, String charset,
			boolean isEncrypt, boolean isSign) throws RuntimeException {
		StringBuilder sb = new StringBuilder();
		
		if (!StringUtils.hasText(charset)) {
			charset = "GBK";
		}
		sb.append("<?xml version=\"1.0\" encoding=\"" + charset + "\"?>");
		if (isEncrypt) {
			sb.append("<alipay>");
			String encrypted = rsaEncrypt(bizContent, alipayPublicKey, charset);
			sb.append("<response>" + encrypted + "</response>");
			sb.append("<encryption_type>RSA</encryption_type>");
			if (isSign) {
				String sign = rsaSign(encrypted, cusPrivateKey, charset);
				sb.append("<sign>" + sign + "</sign>");
				sb.append("<sign_type>RSA</sign_type>");
			}
			sb.append("</alipay>");
		} else if (isSign) {
			sb.append("<alipay>");
			sb.append("<response>" + bizContent + "</response>");
			String sign = rsaSign(bizContent, cusPrivateKey, charset);
			sb.append("<sign>" + sign + "</sign>");
			sb.append("<sign_type>RSA</sign_type>");
			sb.append("</alipay>");
		} else {
			sb.append(bizContent);
		}
		return sb.toString();
	}

	public static String encryptAndSign(String bizContent, String alipayPublicKey, String cusPrivateKey, String charset,
			boolean isEncrypt, boolean isSign, String signType) throws RuntimeException {
		StringBuilder sb = new StringBuilder();
		if (!StringUtils.hasText(charset)) {
			charset = "GBK";
		}
		sb.append("<?xml version=\"1.0\" encoding=\"" + charset + "\"?>");
		if (isEncrypt) {
			sb.append("<alipay>");
			String encrypted = rsaEncrypt(bizContent, alipayPublicKey, charset);
			sb.append("<response>" + encrypted + "</response>");
			sb.append("<encryption_type>RSA</encryption_type>");
			if (isSign) {
				String sign = rsaSign(encrypted, cusPrivateKey, charset, signType);
				sb.append("<sign>" + sign + "</sign>");
				sb.append("<sign_type>");
				sb.append(signType);
				sb.append("</sign_type>");
			}
			sb.append("</alipay>");
		} else if (isSign) {
			sb.append("<alipay>");
			sb.append("<response>" + bizContent + "</response>");
			String sign = rsaSign(bizContent, cusPrivateKey, charset, signType);
			sb.append("<sign>" + sign + "</sign>");
			sb.append("<sign_type>");
			sb.append(signType);
			sb.append("</sign_type>");
			sb.append("</alipay>");
		} else {
			sb.append(bizContent);
		}
		return sb.toString();
	}

	public static String rsaEncrypt(String content, String publicKey, String charset) throws RuntimeException {
		try {
			PublicKey pubKey = getPublicKeyFromX509("RSA", new ByteArrayInputStream(publicKey.getBytes()));
			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(1, pubKey);

			byte[] data = !StringUtils.hasText(charset) ? content.getBytes() : content.getBytes(charset);
			int inputLen = data.length;
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			int offSet = 0;

			int i = 0;

			while (inputLen - offSet > 0) {
				byte[] cache;
				if (inputLen - offSet > 117) {
					cache = cipher.doFinal(data, offSet, 117);
				} else {
					cache = cipher.doFinal(data, offSet, inputLen - offSet);
				}
				out.write(cache, 0, cache.length);
				i++;
				offSet = i * 117;
			}
			byte[] encryptedData = Base64Utils.encode(out.toByteArray()).getBytes();
			out.close();

			return !StringUtils.hasText(charset) ? new String(encryptedData) : new String(encryptedData, charset);
		} catch (Exception e) {
			throw new RuntimeException("EncryptContent = " + content + ",charset = " + charset, e);
		}
	}

	public static String rsaDecrypt(String content, String privateKey, String charset) throws RuntimeException {
		try {
			PrivateKey priKey = getPrivateKeyFromPKCS8("RSA", new ByteArrayInputStream(privateKey.getBytes()));
			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(2, priKey);

			byte[] encryptedData = !StringUtils.hasLength(charset) ? Base64Utils.decode(content)
					: Base64Utils.decode(content);

			int inputLen = encryptedData.length;
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			int offSet = 0;

			int i = 0;

			while (inputLen - offSet > 0) {
				byte[] cache;
				if (inputLen - offSet > 128) {
					cache = cipher.doFinal(encryptedData, offSet, 128);
				} else {
					cache = cipher.doFinal(encryptedData, offSet, inputLen - offSet);
				}
				out.write(cache, 0, cache.length);
				i++;
				offSet = i * 128;
			}
			byte[] decryptedData = out.toByteArray();
			out.close();

			return !StringUtils.hasText(charset) ? new String(decryptedData) : new String(decryptedData, charset);
		} catch (Exception e) {
			throw new RuntimeException("EncodeContent = " + content + ",charset = " + charset, e);
		}
	}

}
