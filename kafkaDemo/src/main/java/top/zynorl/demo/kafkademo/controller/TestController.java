package top.zynorl.demo.kafkademo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.zynorl.demo.kafkademo.config.KafkaProducer;

/**
 * @version 1.0
 * @Author niuzy
 * @Date 2023/12/28
 **/
@RestController
public class TestController {

    @Autowired
    private KafkaProducer kafkaProducer;

    @RequestMapping("/hello")
    public String hello(){
        return "hello-world";
    }
    @RequestMapping("/testKafka")
    public void testKafka(){
        kafkaProducer.sendMessage("my_topic", "hello???");
    }
}
