package com.nowcoder.community;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

// 服务器运行该方法
public class CommunityServletInit extends SpringBootServletInitializer {

    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder)
    {
        return builder.sources(CommunityApplication.class);
    }
}
