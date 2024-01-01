package top.zynorl.demo.gatewaydemo.controller;

import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * @version 1.0
 * @Author niuzy
 * @Date 2024/01/01
 **/
@RestController
public class TestController {

    @RequestMapping("/test/sessionId")
    public Mono<String> normal(@RequestHeader("sessionId") String headerValue) {
        System.out.println(headerValue);
        return Mono.just(headerValue);
    }

}
