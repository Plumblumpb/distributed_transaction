package com.plumblum.lock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

import java.util.Collections;


/**
 * @Auther: cpb
 * @Date: 2019/1/22 10:41
 * @Description:
 */
@Component
public class RedisLock {
    private static final Logger logger = LoggerFactory.getLogger(RedisLock.class);

    private static final String LOCK_SUCCESS = "OK";
    private static final String SET_IF_NOT_EXIST = "NX";
    private static final String SET_WITH_EXPIRE_TIME = "PX";

    /**
     * 尝试获取分布式锁
     * @param jedis Redis客户端
     * @param lockKey 锁
     * @param uuid 请求标识(用于防止，解锁异常)
     * @param expireTime 超期时间
     * @return 是否获取成功
     */
    public static boolean tryGetDistributedLock(Jedis jedis, String lockKey, String uuid, int expireTime) {

        String result = jedis.set(lockKey, uuid, SET_IF_NOT_EXIST, SET_WITH_EXPIRE_TIME, expireTime);

        if (LOCK_SUCCESS.equals(result)) {
            return true;
        }
        return false;

    }

    private static final Long RELEASE_SUCCESS = 1L;

    /**
     * 释放分布式锁
     * @param jedis Redis客户端
     * @param lockKey 锁
     * @param uuid 请求标识
     * @return 是否释放成功
     */
    public static boolean releaseDistributedLock(Jedis jedis, String lockKey, String uuid) {
//        使参数KEYS[1]赋值为lockKey，ARGV[1]赋值为uuid。eval()方法是将Lua代码交给Redis服务端执行。
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        Object result = jedis.eval(script, Collections.singletonList(lockKey), Collections.singletonList(uuid));

        if (RELEASE_SUCCESS.equals(result)) {
            return true;
        }
        return false;

    }

}
