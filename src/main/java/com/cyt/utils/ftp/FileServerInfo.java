package com.cyt.utils.ftp;

public class FileServerInfo {

    private String host;

    private int port;

    private String userName;

    private String password;

    private String rootPath;

    private String matchPattern;

    private String ignoreSuffix;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRootPath() {
        return rootPath;
    }

    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }

    public String getMatchPattern() {
        return matchPattern;
    }

    public void setMatchPattern(String matchPattern) {
        this.matchPattern = matchPattern;
    }

    public String getIgnoreSuffix() {
        return ignoreSuffix;
    }

    public void setIgnoreSuffix(String ignoreSuffix) {
        this.ignoreSuffix = ignoreSuffix;
    }

}
