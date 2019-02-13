package com.cyt.utils.concurrent;

import com.cyt.utils.SystemUtils;
import com.cyt.utils.zookeeper.ZooKeeperUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.cyt.utils.StringUtils.formatString;
import static com.cyt.utils.StringUtils.hasText;

public class ZooKeeperLocker implements Locker {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZooKeeperLocker.class);

    private static final int DEFAULT_LOCKER_THRESHOLD = 60;

    private volatile Map<String, ZooKeeper> lockerMap = new ConcurrentHashMap<>();

    private Semaphore lockerThreshold;

    private final String lockerPathTemplate;

    public ZooKeeperLocker(int lockerThreshold, String systemName) {
        this.lockerThreshold = new Semaphore(lockerThreshold);
        ZooKeeperUtils.init();
        lockerPathTemplate = "/" + SystemUtils.getEnv("ENV_KEY", "PROD") + "/utils/locker/" +  systemName + "/%s";
    }

    public ZooKeeperLocker(String systemName) {
        this(DEFAULT_LOCKER_THRESHOLD, systemName);
    }

    @Override
    public boolean lock(final String lockerName) {
        if (checkLock(lockerName)) {
            return true;
        }
        try {
            lockerThreshold.acquire();
            return _lock(getLockerKey(lockerName), getLockerPath(lockerName), Long.MAX_VALUE);
        } catch (Exception e) {
            LOGGER.error("acquire concurrent locker fail.", e);
            if (!(e instanceof InterruptedException)) {
                lockerThreshold.release();
            }
            return false;
        }
    }

    @Override
    public boolean tryLock(String lockerName) {
        return tryLock(lockerName, 0);
    }

    @Override
    public boolean tryLock(String lockerName, long wait) {
        if (checkLock(lockerName)) {
            return true;
        }
        try {
            long startTime = System.currentTimeMillis();
            if (!lockerThreshold.tryAcquire(wait, TimeUnit.MILLISECONDS)) {
                return false;
            }
            return _lock(getLockerKey(lockerName), getLockerPath(lockerName), (wait - (System.currentTimeMillis() - startTime)));
        } catch (Exception e) {
            LOGGER.error("acquire concurrent locker fail. ", e);
            if (!(e instanceof InterruptedException)) {
                lockerThreshold.release();
            }
            return false;
        }
    }

    @Override
    public boolean unLock(String lockerName) {
        try {
            String lockerKey = getLockerKey(lockerName);
            if (lockerMap.containsKey(lockerKey)) {
                String lockerPath = getLockerPath(lockerName);
                ZooKeeper zooKeeper = lockerMap.get(lockerKey);
                ZooKeeperUtils.deletePath(zooKeeper, lockerPath);
                ZooKeeperUtils.close(zooKeeper);
                lockerMap.remove(lockerKey);
            }
            return true;
        } catch (Exception e) {
            LOGGER.error("release concurrent locker fail.", e);
            return false;
        } finally {
            lockerThreshold.release();
        }
    }

    private boolean _lock(ZooKeeper zooKeeper, String lockerName) {
        return _lock(zooKeeper, getLockerKey(lockerName), getLockerPath(lockerName));
    }

    private boolean _lock(ZooKeeper zooKeeper, String lockerKey, String lockerPath) {
        if (hasText(ZooKeeperUtils.createPath(zooKeeper, lockerPath, "", CreateMode.EPHEMERAL))) {
            lockerMap.put(lockerKey, zooKeeper);
            return true;
        }
        return false;
    }

    private boolean _lock(final String lockerKey, final String lockerPath, long wait) {
        long startTime = System.currentTimeMillis();
        final AtomicBoolean isLock = new AtomicBoolean(false);
        final ZooKeeper zooKeeper = ZooKeeperUtils.createZkCli(ZooKeeperUtils.getDefaultZkServerInfoMap(), null);
        try {
            final Stat stat = zooKeeper.exists(lockerPath, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    try {
                        zooKeeper.exists(lockerPath, this);
                        if (event.getType() == Event.EventType.NodeDeleted
                                && !lockerMap.containsKey(lockerKey)
                                && _lock(zooKeeper, lockerKey, lockerPath)) {
                            isLock.set(true);
                        }
                    } catch (Exception e) {
                        LOGGER.error("acquire concurent locker fail.", e);
                    }
                }
            });
            if (stat == null && _lock(zooKeeper, lockerKey, lockerPath)) {
                isLock.set(true);
            }
            while (!isLock.get() && (System.currentTimeMillis() - startTime) < wait) {}
            if (isLock.get()) {
                return true;
            }
            ZooKeeperUtils.close(zooKeeper);
            return false;
        } catch (Exception e) {
            ZooKeeperUtils.close(zooKeeper);
            LOGGER.error("accquire concurrent lock fail ,", e);
            return false;
        }

    }

    private boolean checkLock(String lockerName) {
        if (lockerMap.containsKey(getLockerKey(lockerName))) {
            return true;
        }
        return false;
    }

    private String getLockerKey(String lockerName) {
        return String.valueOf(Thread.currentThread().getId()) + "_" + lockerName;
    }

    private String getLockerPath(String lockerName) {
        return formatString(lockerPathTemplate, lockerName);
    }

}
