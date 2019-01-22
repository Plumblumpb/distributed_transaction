package com.plumblum.lock;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @Auther: cpb
 * @Date: 2019/1/22 20:13
 * @Description:
 */
@Component
public class RedisLock  {

    @Autowired
    private RedissonClient redissonClient;

    public void redisLock(){
        // 还可以getFairLock(), getReadWriteLock()
        //设置lock名称
        RLock redLock = redissonClient.getLock("REDLOCK_KEY");
        boolean isLock;
        try {
            isLock = redLock.tryLock();
            // 500ms拿不到锁, 就认为获取锁失败。10000ms即10s是锁失效时间。
            isLock = redLock.tryLock(500, 10000, TimeUnit.MILLISECONDS);
            if (isLock) {
                //TODO if get lock success, do something;
            }
        } catch (Exception e) {
        } finally {
            // 无论如何, 最后都要解锁
            redLock.unlock();
        }
    }



}
