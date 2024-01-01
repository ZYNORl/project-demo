package top.zynorl.demo.gatewaydemo.handler;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.authentication.HttpBasicServerAuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import top.zynorl.demo.gatewaydemo.config.CustomAuthenticationProvider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

@Slf4j
@Component
public class CustomHttpBasicServerAuthenticationEntryPoint extends HttpBasicServerAuthenticationEntryPoint {
    @Autowired
    private CustomAuthenticationProvider authenticationProvider;

    @Override
    public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException e) {
        // 获取请求路径
        String requestPath = exchange.getRequest().getPath().toString();
        // 判断是否为登录接口
        if (requestPath.equals("/auth/login")) {
            //只适用于标准的 application/x-www-form-urlencoded 类型的登录表单数据
            return exchange.getFormData().flatMap(data -> {
                List<String> usernames = data.get("username");
                String username;
                List<String> passwords = data.get("password");
                String password;
                if (!CollectionUtils.isEmpty(usernames) && !CollectionUtils.isEmpty(passwords)) {
                    username = usernames.get(0);
                    password = passwords.get(0);
                    // 创建一个认证对象
                    Authentication authentication = new UsernamePasswordAuthenticationToken(username, password);
                    try {
                        // 进行身份验证并获取认证结果
                        Authentication authenticated = authenticationProvider.authenticate(authentication);

                        return handleLoginSuccessRequest(exchange, authenticated.getCredentials().toString());
                    } catch (AuthenticationException exception) {
                        // 认证失败，返回相应的错误响应
                        return handleLoginFailedRequest(exchange, exception);
                    }
                } else {
                    return handleLoginFailedRequest(exchange, null);
                }
            });
        } else {
            // 非登录接口，正常拦截
            return handleNonLoginRequest(exchange);
        }
    }

    private Mono<Void> handleLoginSuccessRequest(ServerWebExchange exchange, String token) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.OK);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON_UTF8);
        response.getHeaders().add("Authorization", "Bearer " + token);
        Map<String, Object> result = new HashMap<>();
        result.put("status", HttpStatus.OK);
        result.put("token", token);
        result.put("message", "登录成功");
        byte[] dataBytes = JSON.toJSONString(result).getBytes();
        DataBuffer bodydataBuffer = response.bufferFactory().wrap(dataBytes);
        return response.writeWith(Mono.just(bodydataBuffer));
    }


    private Mono<Void> handleLoginFailedRequest(ServerWebExchange exchange, AuthenticationException exception) {
        if (exception != null) {
            log.debug("login exception:" + exception);
        }
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.OK);
        response.getHeaders().add("Content-Type", APPLICATION_JSON_UTF8_VALUE);
        Map<String, Object> result = new HashMap<>();
        result.put("status", HttpStatus.UNAUTHORIZED);
        result.put("message", "登录失败");
        byte[] dataBytes = JSON.toJSONString(result).getBytes();
        DataBuffer bodydataBuffer = response.bufferFactory().wrap(dataBytes);
        return response.writeWith(Mono.just(bodydataBuffer));
    }

    private Mono<Void> handleNonLoginRequest(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", APPLICATION_JSON_UTF8_VALUE);
        Map<String, Object> result = new HashMap<>();
        result.put("status", HttpStatus.UNAUTHORIZED);
        result.put("message", "鉴权失败");
        byte[] dataBytes = JSON.toJSONString(result).getBytes();
        DataBuffer bodydataBuffer = response.bufferFactory().wrap(dataBytes);
        return response.writeWith(Mono.just(bodydataBuffer));
    }

}
