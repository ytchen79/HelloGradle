package com.cyt.utils.ftp;

import com.cyt.utils.FileUtils;
import com.cyt.utils.consts.CharsetNameEnum;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import static com.cyt.utils.StringUtils.formatString;
import static com.cyt.utils.StringUtils.hasText;

public abstract class FtpUtils {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(FtpUtils.class);
	
	public static FTPClient getConnectedClient(String ftpHost, int ftpPort, String ftpUser, String ftpPasswd, String charsetName) throws Exception {
		FTPClient ftpClient = new FTPClient();
		ftpClient.connect(ftpHost, ftpPort);
		ftpClient.login(ftpUser, ftpPasswd);
		ftpClient.enterLocalPassiveMode();
		ftpClient.setControlEncoding(charsetName);
		ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
		int reply = ftpClient.getReplyCode();
		if (!FTPReply.isPositiveCompletion(reply)) {
			ftpClient.disconnect();
			return null;
		}
		return ftpClient;
	}

	public static void disConnect(FTPClient ftpClient) {
		if (ftpClient != null && ftpClient.isConnected()) {
			try {
				ftpClient.logout();
				ftpClient.disconnect();
			} catch (IOException e) {
				LOGGER.error("", e);
			}
		}
	}

	public static byte[] download(String ftpHost, int ftpPort, String ftpUser, String ftpPasswd, String ftpPath, String fileName, String charsetName) {
		if (!isExist(ftpHost, ftpPort, ftpUser, ftpPasswd, ftpPath, fileName)) {
			return null;
		}
		FTPClient ftpClient = null;
		try {
			ftpClient = getConnectedClient(ftpHost, ftpPort, ftpUser, ftpPasswd, charsetName);
			if (!ftpClient.changeWorkingDirectory(ftpPath)) {
				return null;
			}
			try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
				ftpClient.retrieveFile(fileName, baos);
				baos.flush();
				return baos.toByteArray();
			}
		} catch (Exception e) {
			LOGGER.error("", e);
			return null;
		} finally {
			if (ftpClient != null) {
				disConnect(ftpClient);
			}
		}
	}

	public static boolean isExist(String ftpHost, int ftpPort, String ftpUser, String ftpPasswd, String ftpPath, String fileName) {
		FTPClient ftpClient = null;
		try {
			ftpClient = getConnectedClient(ftpHost, ftpPort, ftpUser, ftpPasswd, CharsetNameEnum.UTF_8.value());
			if (!ftpClient.changeWorkingDirectory(ftpPath)) {
				return false;
			}
			FTPFile[] ftpFiles = ftpClient.listFiles(fileName);
			return ftpFiles != null && ftpFiles.length > 0;
		} catch (Exception e) {
			LOGGER.error("", e);
			return false;
		} finally {
			if (ftpClient != null) {
				disConnect(ftpClient);
			}
		}
	}

	public static Map<String, String> upload(String ftpHost, int ftpPort, String ftpUser, String ftpPasswd, String ftpPath, String fileName, String charsetName, String localPath) {
		Map<String, String> resultMap = new HashMap<>();
		File localFile = new File(localPath);
		if ((!localFile.exists()) || localFile.isDirectory()) {
			resultMap.put("resultCode", "ERROR");
			resultMap.put("resultMsg", "文件不存在且不支持上传目录！");
			return resultMap;
		}
		FTPClient ftpClient = null;
		try {
			ftpClient = getConnectedClient(ftpHost, ftpPort, ftpUser, ftpPasswd, charsetName);
			if (!ftpClient.changeWorkingDirectory(ftpPath)) {
				resultMap.put("resultCode", "ERROR");
				resultMap.put("resultMsg", "FTP文件目录改变失败！原因为:" + ftpClient.getReplyString());
				LOGGER.error("FTP文件目录改变失败！原因为:" + ftpClient.getReplyString());
				return resultMap;
			}
			try (InputStream is = new FileInputStream(localFile)) {
				if (!ftpClient.storeFile(localFile.getName(), is)) {
					resultMap.put("resultCode", "ERROR");
					resultMap.put("resultMsg", "文件上传失败！原因为:" + ftpClient.getReplyString());
					LOGGER.error("文件上传失败！原因为:" + ftpClient.getReplyString());
					return resultMap;
				}
			}
			resultMap.put("resultCode", "SUCCESS");
		} catch (Exception e) {
			LOGGER.error("", e);
			resultMap.put("resultCode", "ERROR");
			resultMap.put("resultMsg", e.getMessage());
			return resultMap;
		} finally {
			if (ftpClient != null) {
				disConnect(ftpClient);
			}
		}
		return resultMap;
	}
	
	public static Map<String, String> downloadDir(String ftpHost, int ftpPort, String ftpUser, String ftpPasswd, String ftpPath, String charsetName, String localPath) {
		Map<String, String> resultMap = new HashMap<>();
		File localDir = new File(localPath);
		if (!localDir.exists()) {
			localDir.mkdirs();
		}
		if (!FileUtils.isDirectory(localPath)) {
			resultMap.put("resultCode", "ERROR");
			resultMap.put("resultMsg", localPath + "不是一个目录！");
			return resultMap;
		}
		FTPClient ftpClient = null;
		try {
			ftpClient = getConnectedClient(ftpHost, ftpPort, ftpUser, ftpPasswd, charsetName);
			ftpClient.changeWorkingDirectory(ftpPath);
			FTPFile[] ftpFiles = ftpClient.listFiles();
			for (FTPFile ftpFile : ftpFiles) {
				try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
					String localFileName = localDir.getAbsolutePath() + File.separator + ftpFile.getName();
					if (ftpClient.retrieveFile(ftpFile.getName(), baos)) {
						if (!FileUtils.isExist(localPath, ftpFile.getName()) || !FileUtils.isSame(baos.toByteArray(), FileUtils.getFileBytes(localFileName))) {
							FileUtils.save(localPath, ftpFile.getName(), baos.toByteArray());
						}
						// 若下载成功，则将FTP上文件删除
						ftpClient.deleteFile(ftpFile.getName());
					}
				}
			}
		} catch (Exception e) {
			LOGGER.error("", e);
			resultMap.put("resultCode", "ERROR");
			resultMap.put("resultMsg", e.getMessage());
			return resultMap;
		} finally {
			if (ftpClient != null) {
				disConnect(ftpClient);
			}
		}
		resultMap.put("resultCode", "SUCCESS");
		return resultMap;
	}

	public static Map<String, byte[]> download(String ftpHost, int ftpPort, String ftpUser, String ftpPasswd, String ftpPath, String charsetName) {
		Map<String, byte[]> resutlMap = new HashMap<>();
		FTPClient ftpClient = null;
		try {
			ftpClient = getConnectedClient(ftpHost, ftpPort, ftpUser, ftpPasswd, charsetName);
			if (!ftpClient.changeWorkingDirectory(ftpPath)) {
				LOGGER.error("改变FTP远程目录失败，原因为：" + ftpClient.getReplyString());
				return null;
			}
			
			FTPFile[] ftpFiles = ftpClient.listFiles();
			for (FTPFile ftpFile : ftpFiles) {
				try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
					if (ftpClient.retrieveFile(ftpFile.getName(), baos)) {
						resutlMap.put(ftpFile.getName(), baos.toByteArray());
					} else {
						LOGGER.error("文件" + ftpFile.getName() + "下载失败！");
					}
				}
			}
		} catch (Exception e) {
			LOGGER.error("", e);
			return null;
		} finally {
			if (ftpClient != null) {
				disConnect(ftpClient);
			}
		}
		return resutlMap;
	}
	
	public static void delete(String ftpHost, int ftpPort, String ftpUser, String ftpPasswd, String ftpPath, String charsetName, Set<String> fileNameSet) {
		FTPClient ftpClient = null;
		try {
			ftpClient = getConnectedClient(ftpHost, ftpPort, ftpUser, ftpPasswd, charsetName);
			ftpClient.changeWorkingDirectory(ftpPath);
			for (String fileName : fileNameSet) {
				ftpClient.deleteFile(fileName);
			}
		} catch (Exception e) {
			LOGGER.error("", e);
		} finally {
			if (ftpClient != null) {
				disConnect(ftpClient);
			}
		}
	}

	public static boolean changeWorkingDirectory(FTPClient ftpClient, String path) {
		if (!hasText(path)) {
			return false;
		}
		try {
			if (ftpClient.changeWorkingDirectory(path)) {
				return true;
			}
			StringTokenizer tokenizer = new StringTokenizer(path, "\\//");
			while (tokenizer.hasMoreTokens()) {
				String pathNode = tokenizer.nextToken();
				ftpClient.makeDirectory(pathNode);
				ftpClient.changeWorkingDirectory(pathNode);
			}
			return true;
		} catch (Exception e) {
			LOGGER.error(formatString("改变ftp路径%s失败，", path), e);
			return false;
		}
	}

}
