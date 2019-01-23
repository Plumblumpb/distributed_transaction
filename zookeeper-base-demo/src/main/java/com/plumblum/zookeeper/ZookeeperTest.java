package com.plumblum.zookeeper;

import org.apache.zookeeper.*;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * @Auther: cpb
 * @Date: 2019/1/23 09:59
 * @Description:
 */
public class ZookeeperTest {
    /** zookeeper地址 */
    static final String CONNECT_ADDR = "localhost:2181";
    /** session超时时间 */
    static final int SESSION_OUTTIME = 2000;//ms
    /** 信号量，阻塞程序执行，用于等待zookeeper连接成功，发送成功信号 */
    static final CountDownLatch connectedSemaphore = new CountDownLatch(1);
    public static void main(String[] args) {
        Watcher watcher = new Watcher() {
            public void process(WatchedEvent watchedEvent) {
                //获取事件状态
                Event.KeeperState keeperState = watchedEvent.getState();
                //获取事件类型
                Event.EventType eventType = watchedEvent.getType();
                //如果建立连接
                if(Event.KeeperState.SyncConnected == keeperState){
                    if (Event.EventType.None == eventType){
                        //连接成功，发送信号，后面程序阻塞
                        connectedSemaphore.countDown();
                        System.out.println("zookeeper 建立连接");
                    }
                }

            }
        };
        //构造函数参数：地址，session过期时间，watcher回调函数
        try {
            ZooKeeper zooKeeper = new ZooKeeper(CONNECT_ADDR,SESSION_OUTTIME,watcher);
            //进行阻塞，直达watcher监听到连接事件
            connectedSemaphore.await();
            //判断是否存在节点
            if(null == zooKeeper.exists("/myroot",false)) {
                //创建父节点 参数：path，data(byte类型),acl策略（加密策略），节点类型(是否持久化，是否顺序)
                zooKeeper.create("/myroot", "root data".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
            if(null == zooKeeper.exists("/myroot/child",false)) {
                //创建临时子节点（发送异常会自动移除）
                zooKeeper.create("/myroot/child", "child data".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
            //获取节点数据 参数：path(路径),watch（监听器）,stat(各种源数据，事务id，版本号，时间戳，大小)
            String data = new String(zooKeeper.getData("/myroot/child",false,null));
            System.out.println(data);
            System.out.println(zooKeeper.getChildren("/myroot",false));

            //修改节点数据 参数：path，data ，version
            zooKeeper.setData("/myroot/child","你好吗".getBytes(),-1);
            System.out.println(new String(zooKeeper.getData("/myroot/child",false,null)));


            //判断节点是否存在
            System.out.println(zooKeeper.exists("/myroot/child",false));


            //删除节点
            zooKeeper.delete("root/child",-1);
            System.out.println(zooKeeper.exists("/myroot/child",false));

            zooKeeper.close();
        }catch (KeeperException e){
            e.printStackTrace();
        }catch (InterruptedException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
}
