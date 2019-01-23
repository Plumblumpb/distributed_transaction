package com.plumblum.curator;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

/**
 * @Auther: cpb
 * @Date: 2019/1/23 14:37
 * @Description:
 */
public class CuratorMain1 {

    private CuratorFramework client;

    private static final String ZK_SERVER_URL = "localhost:2181";

    //启动客户端
    public CuratorMain1(){
        //重试策略 ExponentialBackoffRetry【推荐】参数 baseSleepTimeMs：初始sleep时间(ms) maxRetries：最大重试次数，maxSleepMs：最大重试时间(ms)
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000,5);
        //创建客户端
        client = CuratorFrameworkFactory.builder()
                .connectString(ZK_SERVER_URL)
                .sessionTimeoutMs(10000)
                .retryPolicy(retryPolicy)
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
        CuratorMain1 curatorMain = new CuratorMain1();
        boolean isStart = curatorMain.client.isStarted();
        System.out.println("当前客户端状态："+(isStart?"连接中":"已关闭"));

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        curatorMain.closedClient();
        boolean isStart2 = curatorMain.client.isStarted();
        System.out.println("当前客户端状态："+(isStart2?"连接中":"已关闭"));

    }
}
