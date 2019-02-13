package com.cyt.utils.sequence;

import com.cyt.utils.Assert;
import com.cyt.utils.DateUtils;
import com.cyt.utils.SystemUtils;
import com.cyt.utils.zookeeper.ZooKeeperUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.cyt.utils.StringUtils.*;

public class Snowflake implements SequenceGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Snowflake.class);

    private static final String DEFAULT_START_TIMESTAMP = "20180601000000";

    private final long startTimestamp;

    // 0 - 00000000000000000000000000000000000000000 - 0000000000 - 000000000000

    private final String machinePath;

    private long mask = -1 << 12;

    private volatile Map<String, Map<String, Long>> sequenceTypeMap = new HashMap<>();

    public Snowflake() {
        this(DEFAULT_START_TIMESTAMP);
    }

    public Snowflake(String startTime) {
        Assert.isTrue(DateUtils.isFormatDate(startTime, DateUtils.yyyyMMddHHmmss), formatString("流水号起始时间%s格式错误，正确格式为%s",
                startTime, DateUtils.yyyyMMddHHmmss));
        ZooKeeperUtils.init();
        startTimestamp = DateUtils.parseYyyyMMddHHmmss(startTime).getTime();
        machinePath = "/" + SystemUtils.getEnv("ENV_KEY", "PROD") + "/utils/sequence";
        LOGGER.info("正在加载流水号类型配置列表");
        try {
            final ZooKeeper zooKeeper = ZooKeeperUtils.getDefaultZookeeper();
            List<String> sequenceTypeList = zooKeeper.getChildren(machinePath, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    try {
                        List<String> sequenceTypeList = zooKeeper.getChildren(machinePath, this);
                        if (event.getType() == Event.EventType.NodeChildrenChanged) {
                            refreshSequenceTypeList(sequenceTypeList);
                        }
                    } catch (Exception e) {
                        LOGGER.error("更新流水号类型失败, ", e);
                    }
                }
            });
            refreshSequenceTypeList(sequenceTypeList);
            LOGGER.info("加载流水号类型配置列表完成");
        } catch (Exception e) {
            LOGGER.error("初始化流水号类型失败, ", e);
        }
    }

    public void refreshSequenceTypeList(List<String> sequenceTypeList) {
        try {
            if (sequenceTypeList == null || sequenceTypeList.size() == 0) {
                LOGGER.warn(" 流水号类型列表为空 ");
                return;
            }
            ZooKeeper zooKeeper = ZooKeeperUtils.getDefaultZookeeper();
            Set<String> existSequenceTypeSet = new HashSet<>(sequenceTypeMap.keySet());
            for (String sequenceType : sequenceTypeList) {
                if (sequenceTypeMap.containsKey(sequenceType)) {
                    continue;
                }
                String path = ZooKeeperUtils.createPath(zooKeeper, machinePath + "/" + sequenceType + "/", "", CreateMode.EPHEMERAL_SEQUENTIAL);
                Map<String, Long> sequenceConfigMap = new HashMap<>();
                sequenceConfigMap.put("machine-id", Long.valueOf(path.substring(path.lastIndexOf("/") + 1)) % Long.valueOf("1111111111", 2));
                sequenceConfigMap.put("last-timestamp", System.currentTimeMillis());
                sequenceConfigMap.put("last-mills-sequence", 0L);
                sequenceTypeMap.put(sequenceType, sequenceConfigMap);
                existSequenceTypeSet.remove(sequenceType);
            }
            for (String sequenceType : existSequenceTypeSet) {
                sequenceTypeMap.remove(sequenceType);
            }
            LOGGER.info(formatString(" 更新流水号，流水号配置信息：%s ", sequenceTypeMap));
        } catch (Exception e) {
            LOGGER.error("更新流水号类型失败, ", e);
        }

    }

    public long nextId(String sequenceType) {
        long sequenceId = 0L;
        if (!sequenceTypeMap.containsKey(sequenceType)) {
            LOGGER.error(formatString("不存在流水号类型为[%s]的配置", sequenceType));
            return sequenceId;
        }
        long startTime = System.currentTimeMillis();
        Map<String, Long> sequenceConfigMap = sequenceTypeMap.get(sequenceType);
        Long machine = sequenceConfigMap.get("machine-id");
        try {
            synchronized (machine) {
                while (sequenceId == 0) {
                    long lastMills =  sequenceConfigMap.get("last-timestamp");
                    long curTimestamp = System.currentTimeMillis();
                    Assert.isTrue(curTimestamp >= lastMills, "机器时间被回退");
                    if (lastMills != curTimestamp) {
                        sequenceConfigMap.put("last-timestamp", curTimestamp);
                        sequenceConfigMap.put("last-mills-sequence", 0L);
                        sequenceId = 0 | ((lastMills - startTimestamp) << 22) | (machine << 12);
                    } else {
                        long millsSequence = sequenceConfigMap.get("last-mills-sequence") + 1;
                        if ((millsSequence & mask) == 0) {
                            sequenceConfigMap.put("last-mills-sequence", millsSequence);
                            sequenceId = 0 | ((lastMills - startTimestamp) << 22) | (machine << 12) | millsSequence;
                        } else {
                            LOGGER.warn("毫秒内序列号已经用完");
                        }
                    }
                }
            }
        } finally {
            long consumeTime = System.currentTimeMillis() - startTime;
            if (consumeTime > 1) {
                LOGGER.warn(formatString("生成流水号耗时：%s ms", consumeTime));
            }
        }
        return sequenceId;
    }

    @Override
    public String generateSequence(String sequenceType) {
        long nextId = nextId(sequenceType);
        if (nextId == 0) {
            return null;
        }
        return fixLeftStringLength(String.valueOf(nextId).getBytes(), ZERO_BYTE, 19);
    }

}
