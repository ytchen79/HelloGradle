package com.cyt.utils.zookeeper;

import com.cyt.utils.Assert;
import com.cyt.utils.consts.CharsetNameEnum;
import com.cyt.utils.json.JSONUtils;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import static com.cyt.utils.StringUtils.*;

;

/**
 * Created by cyt on 2018/2/27.
 */
public class ZooKeeperUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZooKeeperUtils.class);

    private static final String ZOOKEEPER_SERVERS_INFO = "ZOOKEEPER_SERVERS_INFO";

    private static final String CONN_STRING = "CONN_STRING";

    private static final String SESSION_TMO = "SESSION_TMO";

    private static final String SESSION_PASSWD = "SESSION_PASSWD";

    private static Map<String, Map<String, String>> zooKeeperServerInfoMap;

    private static Map<String, String> defaultServerInfoMap;

    private static ZooKeeper defaultZookeeper;

    private static boolean isInit;

    public static synchronized void init() {
        if (isInit) {
            return;
        }
        Assert.notNull(trim(System.getenv(ZOOKEEPER_SERVERS_INFO)), formatString("环境变量zookeeper服务器信息（ZOOKEEPER_SERVERS_INFO）未设置"));
        Assert.isTrue(JSONUtils.isValidateJson(System.getenv(ZOOKEEPER_SERVERS_INFO)), formatString("环境变量zookeeper服务器信息为非JSON格式"));
        try {
            zooKeeperServerInfoMap = JSONUtils.jsonToObject(System.getenv(ZOOKEEPER_SERVERS_INFO), Map.class);
            for (Map.Entry<String, Map<String, String>> entry : zooKeeperServerInfoMap.entrySet()) {
                defaultServerInfoMap = entry.getValue();
                defaultZookeeper = createZkCli(defaultServerInfoMap, null);
                break;
            }
            LOGGER.info(formatString("加载到zookeeper服务器信息%s", System.getenv(ZOOKEEPER_SERVERS_INFO)));
            isInit = true;
        } catch (Exception e) {
            isInit = false;
            throw new RuntimeException(formatString("初始化zookeeper服务器信息失败"), e);
        }
    }

    public static ZooKeeper createZkCli(Map<String, String> zkServerInfoMap, Watcher watcher) {
        Assert.notNull(trim(zkServerInfoMap.get(CONN_STRING)), formatString("zookeeper客户端配置连接信息(CONN_STRING)为空"));
        String connectString = zkServerInfoMap.get(CONN_STRING);
        int sessionTimeout = zkServerInfoMap.containsKey(SESSION_TMO) ? Integer.valueOf(zkServerInfoMap.get(SESSION_TMO)) : 30000;
        if (watcher == null) {
            watcher = new DefaultWatcher();
        }
        try {
            return zkServerInfoMap.containsKey(SESSION_PASSWD) ? new ZooKeeper(connectString, sessionTimeout, watcher, System.currentTimeMillis(), zkServerInfoMap.get(SESSION_PASSWD).getBytes())
                    : new ZooKeeper(connectString, sessionTimeout, watcher);
        } catch (Exception e) {
            throw new RuntimeException(formatString("创建zookeeper client失败！"), e);
        }
    }

    public static String createPath(ZooKeeper zooKeeper, String path, String data, CreateMode createMode) {
        return createPath(zooKeeper, path, data.getBytes(CharsetNameEnum.UTF_8.charset()), createMode);
    }

    public static String createPath(ZooKeeper zooKeeper, String path, byte[] data, CreateMode createMode) {
        try {
            if (!hasText(path) || !path.startsWith("/")) {
                return null;
            }
            String[] nodes = path.substring(1).split("/");
            StringBuilder paths = new StringBuilder(path.length());
            for (int i = 0; i < nodes.length - 1; ++i) {
                paths.append("/").append(nodes[i]);
                if (isPathExists(zooKeeper, paths.toString())) {
                    continue;
                }
                try {
                    zooKeeper.create(paths.toString(), null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            return zooKeeper.create(path, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, createMode);
        } catch (Exception e) {
            LOGGER.error(formatString("zookeeper创建节点%s失败,错误信息：%s", path, e.getMessage()));
            return null;
        }
    }

    public static String getData(ZooKeeper zooKeeper, String path, String charsetName) {
        byte[] resultBytes = getData(zooKeeper, path);
        return resultBytes == null ? null : new String(resultBytes, Charset.forName(charsetName));
    }

    public static boolean isPathExists(ZooKeeper zooKeeper, String path) {
        try {
            return zooKeeper.exists(path, false) != null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] getData(ZooKeeper zooKeeper, String path) {
        try {
            Stat stat = zooKeeper.exists(path, false);
            if (stat == null) {
                return null;
            }
            return zooKeeper.getData(path, false, stat);
        } catch (Exception e) {
            LOGGER.error(formatString("zookeeper获取节点%s数据失败,错误信息：%s", path, e.getMessage()));
            return null;
        }
    }

    public static List<String> getChildren(ZooKeeper zooKeeper, String path) {
        try {
            Stat stat = zooKeeper.exists(path, false);
            if (stat == null) {
                return null;
            }
            return zooKeeper.getChildren(path, false);
        } catch (Exception e) {
            LOGGER.error(formatString("zookeeper获取子节点列表%s失败, 错误信息：%s", path, e.getMessage()));
            return null;
        }
    }

    public static boolean deletePath(ZooKeeper zooKeeper, String path) {
        try {
            Stat stat = zooKeeper.exists(path, false);
            if (stat == null) {
                return true;
            }
            zooKeeper.delete(path, stat.getVersion());
            return true;
        } catch (Exception e) {
            LOGGER.error(formatString("zookeeper删除路径%s失败, 错误信息：%s", path), e);
            return false;
        }
    }

    public static void close(ZooKeeper zooKeeper) {
        try {
            zooKeeper.close();
        } catch (Exception e) {
            LOGGER.error(formatString("关闭zookeeper连接失败"), e);
        }
    }

    public static ZooKeeper getDefaultZookeeper() {
        Assert.isTrue(isInit , "zookeeper信息未初始化");
        Assert.notNull(defaultZookeeper, "zookeeper配置未初始化");
        return defaultZookeeper;
    }

    public static Map<String, String> getDefaultZkServerInfoMap() {
        Assert.isTrue(isInit , "zookeeper信息未初始化");
        Assert.notNull(defaultServerInfoMap, formatString("zookeeper配置未初始化"));
        return defaultServerInfoMap;
    }

    public static Map<String, String> getZkServerInfoMap(String zooKeeperName) {
        Assert.isTrue(isInit , "zookeeper信息未初始化");
        Assert.notNull(zooKeeperServerInfoMap, formatString("zookeeper配置未初始化"));
        Assert.notNull(zooKeeperServerInfoMap.get(zooKeeperName), formatString("缺失%s的zookeeper配置", zooKeeperName));
        return zooKeeperServerInfoMap.get(zooKeeperName);
    }

    public static class DefaultWatcher implements Watcher {

        private static final Logger WATCHER_LOGGER = LoggerFactory.getLogger(DefaultWatcher.class);

        @Override
        public void process(WatchedEvent event) {
            WATCHER_LOGGER.info(formatString("path: %s, type: %s, stat:%s", event.getPath(), event.getType(), event.getState()));
        }
    }

}
