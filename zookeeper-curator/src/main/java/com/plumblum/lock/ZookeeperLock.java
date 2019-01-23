package com.plumblum.lock;

import org.apache.zookeeper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

/**
 * @Auther: cpb
 * @Date: 2019/1/23 17:40
 * @Description:
 */
public class ZookeeperLock {

        private static final Logger logger = LoggerFactory.getLogger(ZookeeperLock.class);

        private static CountDownLatch connectedSemaphore = new CountDownLatch(1);

        private ZooKeeper zookeeper;

        public ZookeeperLock() {

            // 连接zookeeper server，是异步创建会话的，那我们怎么知道zk session建立成功了呢？
            // 通过一个监听器+CountDownLatch，来确认真正建立了zk server的连接
            try {
                this.zookeeper = new ZooKeeper(
                        "localhost:2181",
                        50000,
                        new ZooKeeperWatcher());

                //打印即使状态：验证其是不是异步的？
                logger.info(String.valueOf(zookeeper.getState()));

                try {
                    // CountDownLatch：简而言之 初始化——非0；非0——等待；0——往下执行
                    connectedSemaphore.await();
                } catch(InterruptedException e) {
                    e.printStackTrace();
                }

                logger.info("ZooKeeper session established......");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * 初始化实例：
         */
        public static void init() {
            getInstance();
        }

        /**
         * 建立zk session的watcher：
         */
        private class ZooKeeperWatcher implements Watcher {

            public void process(WatchedEvent event) {
                if(Event.KeeperState.SyncConnected == event.getState()) {
                    connectedSemaphore.countDown();
                }
            }

        }

        /**
         * 静态内部类实现单例：
         */
        private static class Singleton {

            private static ZookeeperLock instance;

            static {
                instance = new ZookeeperLock();
            }

            public static ZookeeperLock getInstance() {
                return instance;
            }

        }

        /**
         * 获取单例：
         *
         * @return
         */
        public static ZookeeperLock getInstance() {
            return Singleton.getInstance();
        }

        /**
         * 重试获取分布式锁：
         *
         * @param adId
         */
        public void acquireDistributedLock(Long adId) {
            String path = "/ad-lock-" + adId;

            try {
                zookeeper.create(path, "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
                logger.info("success to acquire lock for adId = " + adId);
            } catch (Exception e) {
                // 如果那个广告对应的锁node，已经存在了，就是已经被别人加锁了，那么就这里就会报错
                // NodeExistsException
                int count = 0;
                while(true) {
                    try {
                        Thread.sleep(1000);
                        zookeeper.create(path, "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
                    } catch (Exception e2) {
                        count++;
                        logger.info("the " + count + " times try to acquire lock for adId = " + adId);
                        continue;
                    }
                    logger.info("success to acquire lock for adId = " + adId + " after " + count + " times try......");
                    break;
                }
            }
        }

        /**
         * 释放掉分布式锁：
         *
         * @param adId
         */
        public void releaseDistributedLock(Long adId) {
            String path = "/ad-lock-" + adId;
            try {
                zookeeper.delete(path, -1);
                logger.info("release the lock for adId = " + adId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public static void main(String[] args) {
            Long adId = 1L;
            ZookeeperLock zkSession = ZookeeperLock.getInstance();
            //1、获取锁：
            zkSession.acquireDistributedLock(adId);

            //2、执行一些修改共享资源的操作
            logger.info("I am updating common resource！");

            //3、释放锁
            zkSession.releaseDistributedLock(adId);
        }

}
