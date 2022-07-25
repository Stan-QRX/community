package com.nowcoder.community;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class CommunityApplication {
    @Autowired
    private RedisTemplate redisTemplate;
    @PostConstruct
    public void init(){
        // 解决netty启动冲突问题
        // 从Netty4Utils.setAvailableProcessors()找到的解决办法
        System.getProperty("es.set.netty.runtime.available.processors", "false");
    }

    public static void main(String[] args) {

        SpringApplication.run(CommunityApplication.class, args);

    }

}
