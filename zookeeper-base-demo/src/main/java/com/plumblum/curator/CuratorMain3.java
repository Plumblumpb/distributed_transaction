package com.plumblum.curator;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.Stat;

/**
 * @Auther: cpb
 * @Date: 2019/1/23 14:37
 * @Description:
 */
public class CuratorMain3 {

    private CuratorFramework client;

    private static final String ZK_SERVER_URL = "localhost:2181";

    //启动客户端
    public CuratorMain3(){
        //重试策略 ExponentialBackoffRetry【推荐】参数 baseSleepTimeMs：初始sleep时间(ms) maxRetries：最大重试次数，maxSleepMs：最大重试时间(ms)
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000,5);
        //创建客户端
        client = CuratorFrameworkFactory.builder()
                .connectString(ZK_SERVER_URL)
                .sessionTimeoutMs(10000)
                .retryPolicy(retryPolicy)
                .namespace("testCurator") //命名空间，使客户端对节点的操作只能在当前空间下执行
                .build();
        client.start();
    }

    //关闭客户端
    public void closedClient(){
        if(client!= null){
            this.client.close();
        }
    }

    public static void main(String[] args) {
        try {
            CuratorMain3 curatorMain = new CuratorMain3();
            boolean isStart = curatorMain.client.isStarted();
            System.out.println("当前客户端状态："+(isStart?"连接中":"已关闭"));
            CuratorFramework curator = curatorMain.client;
            //创建节点
            String nodePath = "/curator/test";
            String data = "测试数据分析";
            //判断是否存在节点
            Stat stat =  curator.checkExists().forPath(nodePath);
            if (null== stat) {
                curator.create()
                        .creatingParentContainersIfNeeded() //递归创建节点
                        .withMode(CreateMode.PERSISTENT) //设置节点模式
                        .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)  //设置加密模式
                        .forPath(nodePath, data.getBytes());//设置path和data
            }



            //获取节点信息
            String test = new String(curator.getData().forPath(nodePath));
            System.out.println("数据1："+test);
            //注册usingWatcher事件，只执行一次
            //绑定事件只有三个操作：getData、exists、getChildren。
//            CuratorWatcher curatorWatcher = new CuratorWatcher() {
//                @Override
//                public void process(WatchedEvent watchedEvent) throws Exception {
//                    System.out.println("触发了watcher事件，节点路径为："+watchedEvent.getPath()+"，事件类型为："+watchedEvent.getType());
//                }
//            };
            curator.getData().usingWatcher((CuratorWatcher) event -> {
                System.out.println("触发了watcher事件，节点路径为："+event.getPath()+"，事件类型为："+event.getType());
            }).forPath(nodePath);

            //监听数据节点的变更，会触发事件
            //构造NodeCache实例
            NodeCache nodeCache = new NodeCache(curator,nodePath);
            //建立Cache
            //该方法有个boolean类型的参数，默认是false，如果设置为true，那么NodeCache在第一次启动的时候就会立刻从ZooKeeper上读取对应节点的数据内容，并保存在Cache中。
            nodeCache.start(true);
            if(nodeCache.getCurrentData()!=null){
                System.out.println("节点初始化数据为："+new String(nodeCache.getCurrentData().getData()));
            }else {
                System.out.println("节点数据为空！");
            }
            //添加事件（也有remove），还可以知道Excutor
            nodeCache.getListenable().addListener(() -> {
                String data1 = new String(nodeCache.getCurrentData().getData());
                System.out.println("节点路径："+nodeCache.getCurrentData().getPath()+"，节点数据为："+data1);
            });

            //修改节点
            String data2 = "测试数据分析2";
            curator.setData().forPath(nodePath,data2.getBytes());

//            删除节点
            curator.delete()
                    .guaranteed()  //防止网络抖动，只要客户端会话有效，那么Curator 会在后台持续进行删除操作，直到节点删除成功
                    .deletingChildrenIfNeeded()  //如果有子节点会删除，注意除非人为删除namespace，否则namespace不会删除
                    .withVersion(-1)
                    .forPath(nodePath);
            curatorMain.closedClient();

            boolean isStart2 = curatorMain.client.isStarted();
            System.out.println("当前客户端状态："+(isStart2?"连接中":"已关闭"));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
