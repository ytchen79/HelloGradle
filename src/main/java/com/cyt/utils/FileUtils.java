package com.cyt.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.math.BigInteger;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.*;

/**
 * 文件操作工具类
 * 
 * @author cyt
 */
public class FileUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(FileUtils.class);
	
	/**
	 * 保存文件，若存在同名文件，则覆盖
	 * 
	 * @param parent
	 *            父目录路径
	 * @param file
	 *            文件名
	 * @param fileContent
	 *            文件内容
	 * @param charsetName
	 *            文件字符编码
	 * @throws Exception
	 */
	public static void save(String parent, String file, String fileContent, String charsetName) throws Exception {
		save(parent, file, fileContent.getBytes(charsetName));
	}

	/**
	 * 保存文件，若存在同名文件，则覆盖
	 * 
	 * @param parent
	 *            父目录路径
	 * @param file
	 *            文件名
	 * @param is
	 *            文件输入流
	 * @throws Exception
	 */
	public static void save(String parent, String file, InputStream is) throws Exception {
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			byte[] buff = new byte[1024];
			int len;
			while ((len = is.read(buff)) != -1) {
				baos.write(buff, 0, len);
			}
			save(parent, file, baos.toByteArray());
		}
	}

	/**
	 * 保存文件，若存在同名文件，则覆盖
	 * 
	 * @param parent
	 *            父目录路径
	 * @param file
	 *            文件名
	 * @param fileBytes
	 *            文件内容（字节流）
	 * @throws Exception
	 */
	public static void save(String parent, String file, byte[] fileBytes) throws Exception {
		File dir = new File(parent);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		if (!dir.isDirectory()) {
			dir.delete();
			throw new RuntimeException(parent + "不是一个目录!");
		}
		File tmpFile = File.createTempFile(file, ".tmp");
		try (OutputStream os = new FileOutputStream(tmpFile)) {
			os.write(fileBytes);
			os.flush();
		}
		move(tmpFile.getAbsolutePath(), parent + File.separator + file);
	}

	public static boolean isExist(String parent, String fileName) {
		return isExist(parent + File.separator + fileName);
	}
	
	public static boolean isExist(String filePath) {
		File file = new File(filePath);
		return file.exists();
	}
	
	public static long getFileLastestUpdateTime(String filePath) {
		if (!isExist(filePath)) {
			return 0;
		}
		File file = new File(filePath);
		return file.lastModified();
	}
	
	/**
	 * 删除文件
	 */
	public static void delete(String fileName) {
		File file = new File(fileName);
		if (file.exists()) {
			file.delete();
		}
	}

	/**
	 * 移动文件
	 */
	public static boolean move(String src, String dest) throws Exception {
		File srcFile = new File(src);
		if (!srcFile.exists()) {
			throw new RuntimeException("源文件不存在！");
		}
		File destFile = new File(dest);
		if (destFile.exists()) {
			destFile.delete();
		} else if (!destFile.getParentFile().exists()) {
			destFile.getParentFile().mkdirs();
		}
		return srcFile.renameTo(destFile);
	}

	public static void copy(String src, String dest) throws Exception {
		Path srcPath = Paths.get(src);
		if (!Files.exists(srcPath) && Files.isDirectory(srcPath)) {
			throw new RuntimeException("原文件" + src + "不存在或是一个目录！");
		}
		Path destPath = Paths.get(dest);
		if (!Files.exists(destPath)) {
			Files.createDirectories(destPath);
		}
		if (!Files.isDirectory(destPath)) {
			throw new RuntimeException("目标目录" + dest + "不是一个目录！");
		}
		Files.copy(srcPath, Paths.get(dest + File.separator + srcPath.getFileName()));
	}

	/**
	 * 判断两个文件是否相等
	 * 
	 * @param b1
	 *            文件1字节
	 * @param b2
	 *            文件2字节
	 * @throws Exception
	 */
	public static boolean isSame(byte[] b1, byte[] b2) throws Exception {
		MessageDigest md = MessageDigest.getInstance("MD5");
		BigInteger bi1 = new BigInteger(md.digest(b1));
		BigInteger bi2 = new BigInteger(md.digest(b2));
		if (bi1.equals(bi2)) {
			return true;
		}
		return false;
	}

	/**
	 * 判断两个文件是否相等
	 * 
	 * @param fileName1
	 *            文件1路径名
	 * @param fileName2
	 *            文件2路径名
	 * @throws Exception
	 */
	public static boolean isSame(String fileName1, String fileName2) throws Exception {
		byte[] b1 = getFileBytes(fileName1);
		byte[] b2 = getFileBytes(fileName2);
		if (null == b1 || null == b2) {
			return false;
		}
		return isSame(b1, b2);
	}

	/**
	 * 生成文件的MD5值
	 */
	public static String md5Str(String fileName) {
		File file = new File(fileName);
		if (!file.isFile()) {
			throw new RuntimeException(fileName + "不是一个文件！");
		}
		try (InputStream is = new FileInputStream(file); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			byte[] buff = new byte[256];
			int len = 0;
			while (-1 != (len = is.read(buff))) {
				baos.write(buff, 0, len);
			}
			MessageDigest md = MessageDigest.getInstance("MD5");
			return byteToHexString(md.digest(baos.toByteArray()));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 将字节转换为16进制的字符串
	 */
	public static String byteToHexString(byte[] rawByte) {
		if (null == rawByte || rawByte.length == 0) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < rawByte.length; ++i) {
			int tmp = rawByte[i] & 0xFF;
			String raw = Integer.toHexString(tmp);
			if (raw.length() == 1) {
				sb.append("0" + raw);
			} else {
				sb.append(raw);
			}
		}
		return sb.toString();
	}
	
	/**
	 * 通过文件名获取文件的字节码
	 * 
	 * @param fileName
	 *            文件名
	 * @return
	 * @throws Exception
	 */
	public static byte[] getFileBytes(String fileName) throws Exception {
		File file = new File(fileName);
		if (!(file.exists() && file.isFile())) {
			return null;
		}
		try (InputStream is = new FileInputStream(fileName); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			byte[] buff = new byte[512];
			int len = 0;
			while ((len = is.read(buff)) != -1) {
				baos.write(buff, 0, len);
			}
			return baos.toByteArray();
		}
	}
	
	public static boolean isDirectory(String filePath) {
		File file = new File(filePath);
		return file.exists() && file.isDirectory();
	}
	
	public static List<File> getFilesList(String dirPath) {
		File dir = new File(dirPath);
		if (!(dir.exists() && dir.isDirectory())) {
			return new ArrayList<>();
		}
		return Arrays.asList(dir.listFiles());
	}

	public static Map<String, byte[]> getFilesByte(String dirPath) {
		List<File> fileList = getFilesList(dirPath);
		Map<String, byte[]> resutlMap = new HashMap<String, byte[]>(fileList.size());
		for (File f : fileList) {
			if (!f.isFile()) {
				continue;
			}
			try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
					InputStream is = new FileInputStream(f)) {
				byte[] buff = new byte[1024];
				int len;
				while ((len = is.read(buff)) != -1) {
					baos.write(buff, 0, len);
				}
				resutlMap.put(f.getName(), baos.toByteArray());
			} catch (Exception e) {
				LOGGER.error("", e);
			}
		}
		return resutlMap;
	}
	
	public static URL getFileURLByClassPath(String fileClassPath) {
		return FileUtils.class.getResource(fileClassPath);
	}
	
	public static boolean deleteFile(String filePath) {
		File file = new File(filePath);
		if (!file.exists()) {
			return true;
		}
		if (file.isDirectory()) {
			String[] subFileNameList = file.list();
			for (String sufFileName : subFileNameList) {
				if (!deleteFile(filePath + File.separator + sufFileName)) {
					return false;
				}
			}
		}
	    return file.delete();
	}
	
}
