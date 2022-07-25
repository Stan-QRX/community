package com.newcoder.community;

import com.nowcoder.community.CommunityApplication;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
@RunWith(SpringRunner.class)
public class KafkaTests {


    @Autowired
    private KafkaProducer kafkaProducer;

    @Test
    public void testKafka() {
        kafkaProducer.sendMessage("test","你好");
        kafkaProducer.sendMessage("test","你、好");
        kafkaProducer.sendMessage("test","你、真好");
        try {
            Thread.sleep(1000*10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

@Component
class KafkaProducer {
    @Autowired
    public KafkaTemplate kafkaTemplate;

    public void sendMessage(String topic, String message) {
        kafkaTemplate.send(topic, message);
    }
}

@Component
class KafkaConsumer {
    // 有消息就自动触发执行
    // 监听test主题
    // spring（线程阻塞）监听主题是否有新消息
    @KafkaListener(topics = "test")
    public void handleMessage(ConsumerRecord record) {
        System.out.println(record.value());
    }
}