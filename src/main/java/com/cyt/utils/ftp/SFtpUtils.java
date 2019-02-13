package com.cyt.utils.ftp;

import com.cyt.utils.FileUtils;
import com.cyt.utils.StringUtils;
import com.jcraft.jsch.*;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

/**
 * FTP服务
 * 
 * @author cyt
 */
public abstract class SFtpUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(SFtpUtils.class);

	/**
	 * 获取文件列表,返回文件绝对路径
	 */
	public static List<String> fetchFileList(String ftpHost, int ftpPort, String ftpUser, String ftpPasswd, String ftpPath) {
		List<String> fileNameList = new ArrayList<>();
		ChannelSftp sftp = null;
		try {
			sftp = getConnectedClient(ftpHost, ftpPort, ftpUser, ftpPasswd);
			List<LsEntry> lsEntryList = sftp.ls(ftpPath);
			for (LsEntry entry : lsEntryList) {
				/*
				 * isReg - 是否是一个常规文件.
				 * isDir - 是否是一个常规文件.
				 * isChr - 是否是一个字符设备.
				 * isBlk - 是否是一个块设备.
				 * isFifo - 是否是一个FIFO文件.
				 * isSock - 是否是一个SOCKET文件.
				 */
				if (entry.getAttrs().isReg()) {
					fileNameList.add(entry.getFilename());
				}
			}
		} catch (SftpException e) {
			LOGGER.error("获取文件列表出错，错误为:" + e.getMessage());
			throw new RuntimeException(e);
		} finally {
			if (null != sftp) {
				disConnect(sftp);
			}
		}
		return fileNameList;
	}

	/**
	 * 获取文件内容
	 */
	public static byte[] fetchAllFile(String ftpHost, int ftpPort, String ftpUser, String ftpPasswd, String ftpPath, String fileName) {
		byte[] buff = null;
		ChannelSftp sftp = null;
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()){
			sftp = getConnectedClient(ftpHost, ftpPort, ftpUser, ftpPasswd);
			sftp.cd(ftpPath);
			sftp.ls(fileName);
			sftp.get(fileName, baos);
			buff = baos.toByteArray();
		} catch (Exception e) {
			LOGGER.error(StringUtils.formatString("获取文件%s出错，错误为:", fileName), e);
		} finally {
			if (sftp != null) {
				disConnect(sftp);
			}
		}
		return buff;
	}
	
	public static void delete(String ftpHost, int ftpPort, String ftpUser, String ftpPasswd, String ftpPath, Set<String> fileNameSet) {
		ChannelSftp sftp = null;
		try {
			sftp = getConnectedClient(ftpHost, ftpPort, ftpUser, ftpPasswd);
			sftp.cd(ftpPath);
			for (String fileName : fileNameSet) {
				if (sftp.ls(fileName).size() > 0) {
					sftp.rm(fileName);
				}
			}
		} catch (Exception e) {
			LOGGER.error("", e);
		} finally {
			if (sftp != null) {
				disConnect(sftp);
			}
		}
	}
	
	public static Map<String, byte[]> download(String ftpHost, int ftpPort, String ftpUser, String ftpPasswd, String ftpPath, String fileName) {
		Map<String, byte[]> resutlMap = new HashMap<>();
		ChannelSftp sftp = null;
		try {
			sftp = getConnectedClient(ftpHost, ftpPort, ftpUser, ftpPasswd);
			sftp.cd(ftpPath);
			List<LsEntry> lsEntryList = sftp.ls("./");
			for (LsEntry entry : lsEntryList) {
				if (entry.getAttrs().isReg()) {
					try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
						sftp.get(entry.getFilename(), baos);
						resutlMap.put(entry.getFilename(), baos.toByteArray());
					}
				}
			}
		} catch (Exception e) {
			LOGGER.error("", e);
			return null;
		} finally {
			if (sftp != null) {
				disConnect(sftp);
			}
		}
		return resutlMap;
	}
	
	public static byte[] download(String ftpHost, int ftpPort, String ftpUser, String ftpPasswd, String ftpPath) {
		Map<String, byte[]> resutlMap = new HashMap<>();
		ChannelSftp sftp = null;
		try {
			sftp = getConnectedClient(ftpHost, ftpPort, ftpUser, ftpPasswd);
			List<LsEntry> lsEntryList = sftp.ls(ftpPath);
			if (lsEntryList.size() > 0 && lsEntryList.get(0).getAttrs().isReg()) {
				try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
					sftp.get(ftpPath, baos);
					return baos.toByteArray();
				}
			}
		} catch (Exception e) {
			LOGGER.error("", e);
			return null;
		} finally {
			if (sftp != null) {
				disConnect(sftp);
			}
		}
		return null;
	}
	
	public static boolean download(String ftpHost, int ftpPort, String ftpUser, String ftpPasswd, String ftpPath, String charsetName, String localPath) {
		Map<String, String> resultMap = new HashMap<>();
		File localDir = new File(localPath);
		if (!(localDir.exists() && localDir.isDirectory())) {
			localDir.mkdirs();
		}
		ChannelSftp sftp = null;
		try {
			sftp = getConnectedClient(ftpHost, ftpPort, ftpUser, ftpPasswd);
			sftp.cd(ftpPath);
			List<LsEntry> lsEntryList = sftp.ls("./");
			for (LsEntry entry : lsEntryList) {
				if (!entry.getAttrs().isReg()) {
					continue;
				}
				try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
					String localFileName = localDir.getAbsolutePath() + File.separator + entry.getFilename();
					sftp.get(entry.getFilename(), baos);
					if (!FileUtils.isExist(localPath, entry.getFilename()) || !FileUtils.isSame(baos.toByteArray(), FileUtils.getFileBytes(localFileName))) {
						FileUtils.save(localPath, entry.getFilename(), baos.toByteArray());
					}
				}
			}
		} catch (Exception e) {
			LOGGER.error("下载SFTP文件失败", e);
			return false;
		} finally {
			if (sftp != null) {
				disConnect(sftp);
			}
		}
		return true;
	}

	
	
	public static Map<String, String> upload(String ftpHost, int ftpPort, String ftpUser, String ftpPasswd, String remotePath, String fileName, byte[] content) {
		try (InputStream is = new ByteArrayInputStream(content)) {
			 return upload(ftpHost, ftpPort, ftpUser, ftpPasswd, remotePath, fileName, is);
		} catch (Exception e) {
			LOGGER.error("", e);
			Map<String, String> resultMap = new HashMap<>();
			resultMap.put("resultCode", "ERROR");
			resultMap.put("resultMsg", e.getMessage());
			return resultMap;
		}
	}
	
	public static Map<String, String> upload(String ftpHost, int ftpPort, String ftpUser, String ftpPasswd, String remotePath, String filePath) {
		Map<String, String> resultMap = new HashMap<>();
		if (!FileUtils.isExist(filePath) && FileUtils.isDirectory(filePath)) {
			resultMap.put("resultCode", "ERROR");
			resultMap.put("resultMsg", filePath + "不存在或不是一个文件！");
			return resultMap;
		}
		File file = new File(filePath);
		try (InputStream is = new FileInputStream(filePath)) {
			 return upload(ftpHost, ftpPort, ftpUser, ftpPasswd, remotePath, file.getName(), is);
		} catch (Exception e) {
			LOGGER.error("", e);
			resultMap.put("resultCode", "ERROR");
			resultMap.put("resultMsg", e.getMessage());
			return resultMap;
		}
	}
	
	public static Map<String, String> upload(String ftpHost, int ftpPort, String ftpUser, String ftpPasswd, String remotePath, String fileName, InputStream is) {
		Map<String, String> resultMap = new HashMap<>();
		ChannelSftp sftp = null;
		try {
			sftp = getConnectedClient(ftpHost, ftpPort, ftpUser, ftpPasswd);
			cd(sftp, remotePath);
			if (isFileExist(sftp, fileName)) {
				sftp.rm(remotePath + "/" + fileName);
			}
			sftp.put(is, fileName);
		} catch (Exception e) {
			LOGGER.error("", e);
			resultMap.put("resultCode", "ERROR");
			resultMap.put("resultMsg", e.getMessage());
			return resultMap;
		} finally {
			if (sftp != null) {
				disConnect(sftp);
			}
		}
		resultMap.put("resultCode", "SUCCESS");
		return resultMap;
	}

	public static ChannelSftp getConnectedClient(String ftpHost, int ftpPort, String ftpUser, String ftpPasswd) {
		try {
			JSch jsch = new JSch();
			Session sshSession = jsch.getSession(ftpUser, ftpHost, ftpPort);
			sshSession.setPassword(ftpPasswd);
			Properties sshConfig = new Properties();
			sshConfig.put("StrictHostKeyChecking", "no");
			sshSession.setConfig(sshConfig);
			sshSession.connect();
			Channel channel = sshSession.openChannel("sftp");
			channel.connect();
			ChannelSftp sftp = (ChannelSftp) channel;
			LOGGER.debug("FTP连接成功");
			return sftp;
		} catch (Exception e) {
			LOGGER.error(ftpHost + ":" + ftpPort + "建立连接失败，失败原因堆栈:", e);
			throw new RuntimeException(e);
		}
	}
	
	public static void cd(ChannelSftp sftp, String filePath) throws Exception {
		String[] paths = filePath.split("/");
		StringBuilder dir = new StringBuilder();
		if (filePath.startsWith("/")) {
			dir.append("/");
		}
		for (String pathNode : paths) {
			if (StringUtils.trim(pathNode) == null) {
				continue;
			}
			dir.append(pathNode).append("/");
			if (isFileExist(sftp, dir.toString())) {
				sftp.cd(dir.toString());
			} else {
				sftp.mkdir(dir.toString());
				sftp.cd(dir.toString());
			}
		}
	}
	
	public static boolean isFileExist(String ftpHost, int ftpPort, String ftpUser, String ftpPasswd, String filePath) {
		ChannelSftp sftp = null;
		try {
			sftp = getConnectedClient(ftpHost, ftpPort, ftpUser, ftpPasswd);
			return isFileExist(sftp, filePath);
		} finally {
			if (sftp != null) {
				disConnect(sftp);
			}
		}
	}
	
	public static boolean isFileExist(ChannelSftp sftp, String filePath) {
		try {
			sftp.ls(filePath);
			return true;
		} catch (Exception e) {
			if ("No such file".indexOf(e.getMessage()) != -1) {
				return false;
			}
			throw new RuntimeException(e);
		}
	}

	public static void disConnect(ChannelSftp sftp) {
		if (sftp == null) {
			return;
		}
		try {
			if (sftp.getSession().isConnected()) {
				sftp.getSession().disconnect();
			}
		} catch (JSchException e) {
			LOGGER.warn("FTP关闭出错啦！" + e.getMessage());
		}
	}
	
}