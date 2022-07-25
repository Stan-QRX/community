package com.nowcoder.community.config;

import com.google.code.kaptcha.Producer;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class KaptchaConfig {

    /**
     * 实例化一个Kaptcha工具对象
     * 使用@Configuration + @Bean, 方法名是实例化对象的名字
     * Producer：接口   DefaultKaptcha：实现类
     * @return
     */
    @Bean
    public Producer kaptchaProducer() {
        Properties properties = new Properties();
        // 验证码图片的宽度
        properties.setProperty("kaptcha.image.width", "100");
        // 验证码图片的高度
        properties.setProperty("kaptcha.image.height", "40");
        // 验证码图片的字体大小
        properties.setProperty("kaptcha.textproducer.font.size", "32");
        // 验证码图片的字体颜色
        properties.setProperty("kaptcha.textproducer.font.color", "0,0,0");
        // 验证码图片的随机字符范围
        properties.setProperty("kaptcha.textproducer.char.string", "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYAZ");
        // 验证码图片的字符长度
        properties.setProperty("kaptcha.textproducer.char.length", "4");
        // 验证码图片的噪声 干扰 防止机器破解
        properties.setProperty("kaptcha.noise.impl", "com.google.code.kaptcha.impl.NoNoise");

        DefaultKaptcha kaptcha = new DefaultKaptcha();
        Config config = new Config(properties);
        kaptcha.setConfig(config);
        return kaptcha;
    }

}
