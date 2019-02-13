package com.cyt.utils.cache.redis;

import com.cyt.utils.Assert;
import com.cyt.utils.SystemUtils;
import com.cyt.utils.json.JSONUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.cyt.utils.StringUtils.*;

/**
 * Created by cyt on 2018/3/9.
 */
public class RedisClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisClient.class);

    private static final int DEFAULT_CONN_TMO = 60000;
    private static final int DEFAULT_MAX_ATTEMPTS = 5;

    private Map<String, JedisCluster> jedisClusterMap;

    private String defaultEnv;

    private ExecutorService executorService;

    private final String prefix;

    public RedisClient() {
        Assert.isTrue(hasText(System.getenv("REDIS_SERVER_INFO"))
                        && JSONUtils.isValidateJson(System.getenv("REDIS_SERVER_INFO")),
                formatString("环境变量redis服务器信息（REDIS_SERVER_INFO）不能为空"));
        Map<String, Map<String, Object>> redisServerInfoMap = JSONUtils.jsonToObject(System.getenv("REDIS_SERVER_INFO"), Map.class);
        jedisClusterMap = new HashMap<>();
        for (Map.Entry<String, Map<String, Object>> entryMapEntry : redisServerInfoMap.entrySet()) {
            Map<String, Object> redisServerInfo = entryMapEntry.getValue();
            List<Map<String, Object>> nodeInfoList = (List<Map<String, Object>>) redisServerInfo.get("nodes");
            Set<HostAndPort> nodeSet = new HashSet<>();
            for (Map<String, Object> nodeInfo : nodeInfoList) {
                nodeSet.add(new HostAndPort((String) nodeInfo.get("host"), Integer.valueOf((String) nodeInfo.get("port"))));
            }
            String requirepass = trim(redisServerInfo.get("requirepass"));
            JedisCluster jedisCluster = hasText(requirepass) ? new JedisCluster(nodeSet, DEFAULT_CONN_TMO, DEFAULT_CONN_TMO, DEFAULT_MAX_ATTEMPTS, requirepass, new GenericObjectPoolConfig())
                    : new JedisCluster(nodeSet, DEFAULT_CONN_TMO, DEFAULT_CONN_TMO, DEFAULT_MAX_ATTEMPTS, new GenericObjectPoolConfig());
            jedisClusterMap.put(entryMapEntry.getKey(), jedisCluster);
            if (redisServerInfo.get("default") != null && Boolean.valueOf((String) redisServerInfo.get("default"))) {
                defaultEnv = entryMapEntry.getKey();
            }
        }
        if (!hasText(defaultEnv)) {
            defaultEnv = jedisClusterMap.keySet().iterator().next();
        }
        executorService = Executors.newFixedThreadPool(2);
        prefix = SystemUtils.getEnv("ENV_KEY", "");
        LOGGER.info(formatString("加载到redis服务器信息%s", System.getenv("REDIS_SERVER_INFO")));
    }

    public void del(String key) {
        del(defaultEnv, key, true);
    }

    public void del(String env, String key) {
        del(env, key, true);
    }

    public void del(String env, String key, boolean isSync) {
        Assert.notNull(jedisClusterMap.get(env), formatString("redis服务器环境%s未配置", env));
        _del(env, key, isSync);
    }

    public String get(String key) {
        return get(defaultEnv, key);
    }

    public String get(String env, String key) {
        return get(env, key, true);
    }

    public String get(String env, String key, boolean isMulti) {
        return _get(env, key, isMulti);
    }

    public void set(String key, String value) {
        set(defaultEnv, key, value);
    }

    public void set(String env, String key, String value) {
        Assert.notNull(jedisClusterMap.get(env), formatString("redis服务器环境%s未配置", env));
        setex(env, key, value, -1);
    }

    public void set(String env, String key, String value, boolean isSync) {
        Assert.notNull(jedisClusterMap.get(env), formatString("redis服务器环境%s未配置", env));
        setex(env, key, value, -1, isSync);
    }

    public void setex(String key, String value, int expire) {
        setex(defaultEnv, key, value, expire);
    }

    public void setex(String env, String key, String value, int expire) {
        _set(env, key, value, expire, true);
    }

    public void setex(String env, String key, String value, int expire, boolean isSync) {
        Assert.notNull(jedisClusterMap.get(env), formatString("redis服务器环境%s未配置", env));
        _set(env, key, value, expire, isSync);
    }

    public JedisCluster getJedisCluster() {
        return jedisClusterMap.get(defaultEnv);
    }

    public JedisCluster getJedisCluster(String env) {
        return jedisClusterMap.get(env);
    }

    public String getDefaultEnv() {
        return defaultEnv;
    }

    private void _set(String env, String key, String value, int expire, boolean isSync) {
        if (expire > 0) {
            jedisClusterMap.get(env).setex(prefix + key, expire, value);
        } else {
            jedisClusterMap.get(env).set(prefix + key, value);
        }
        if (isSync) {
            synchronize(env, key, value, expire, SynchronizeModeEnum.SET);
        }
    }

    private String _get(String env, String key, boolean isMulti) {
        String value = jedisClusterMap.get(env).get(prefix + key);
        if (value == null && isMulti) {
            for (Map.Entry<String, JedisCluster> entry : jedisClusterMap.entrySet()) {
                if (entry.getKey().equals(env)) {
                    continue;
                }
                value = jedisClusterMap.get(entry.getKey()).get(prefix + key);
                if (value != null) {
                    break;
                }
            }
        }
        return value;
    }

    private void _del(String env, String key, boolean isSync) {
        jedisClusterMap.get(env).del(prefix + key);
        if (isSync) {
            synchronize(env, key, null, -1, SynchronizeModeEnum.DEL);
        }
    }

    private void synchronize(final String env, final String key, final String value, final int expire, final SynchronizeModeEnum mode) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                for (Map.Entry<String, JedisCluster> entry : jedisClusterMap.entrySet()) {
                    if (entry.getKey().equals(env)) {
                        continue;
                    }
                    switch (mode) {
                        case SET:
                            try {
                                _set(entry.getKey(), key, value, expire, false);
                            } catch (Exception e) {
                                LOGGER.error(formatString("写入%s Reids环境失败，", env), e);
                            }
                            break;
                        case DEL:
                            try {
                                _del(entry.getKey(), key, false);
                            } catch (Exception e) {
                                LOGGER.error(formatString("写入%s Reids环境失败，", env), e);
                            }
                            break;
                    }

                }
            }
        } );
    }

    private enum SynchronizeModeEnum {
        SET("set"),
        DEL("del");

        String value;

        SynchronizeModeEnum(String value) {
            this.value = value;
        }
    }



}
