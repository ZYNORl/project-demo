package top.zynorl.demo.kafkademo.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * @version 1.0
 * @Author niuzy
 * @Date 2023/12/28
 **/
@Component
public class KafkaConsumer {
    @KafkaListener(topics = "my_topic")
    public void receiveMessage(String message) {
        System.out.println("Received message: " + message);
        // 处理接收到的消息逻辑
    }
}
