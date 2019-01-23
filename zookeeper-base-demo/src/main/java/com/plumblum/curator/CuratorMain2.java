package com.plumblum.curator;

import com.alibaba.druid.support.json.JSONUtils;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.Stat;

/**
 * @Auther: cpb
 * @Date: 2019/1/23 14:37
 * @Description:
 */
public class CuratorMain2 {

    private CuratorFramework client;

    private static final String ZK_SERVER_URL = "localhost:2181";

    //启动客户端
    public CuratorMain2(){
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
            CuratorMain2 curatorMain = new CuratorMain2();
            boolean isStart = curatorMain.client.isStarted();
            System.out.println("当前客户端状态："+(isStart?"连接中":"已关闭"));
            CuratorFramework curator = curatorMain.client;
            //创建节点
            String nodePath = "/curator/test";
            String data = "测试数据分析";
            //判断是否存在节点
            Stat stat =  curator.checkExists().forPath(nodePath);
            if (null == stat) {
                curator.create()
                        .creatingParentContainersIfNeeded() //递归创建节点
                        .withMode(CreateMode.PERSISTENT) //设置节点模式
                        .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)  //设置加密模式
                        .forPath(nodePath, data.getBytes());//设置path和data
            }

            //获取节点信息
            String test = new String(curator.getData().forPath(nodePath));
            System.out.println("数据1："+test);


            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            curatorMain.closedClient();
            boolean isStart2 = curatorMain.client.isStarted();
            System.out.println("当前客户端状态："+(isStart2?"连接中":"已关闭"));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
