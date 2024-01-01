package top.zynorl.demo.gatewaydemo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "timing.jwt")
@Data
public class JwtProperties {

    private String secretKey;
    private int validateInMs;

}
