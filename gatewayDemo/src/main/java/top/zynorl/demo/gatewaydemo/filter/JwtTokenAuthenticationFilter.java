package top.zynorl.demo.gatewaydemo.filter;

import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import top.zynorl.demo.gatewaydemo.config.JwtProperties;
import top.zynorl.demo.gatewaydemo.util.JwtTokenUtil;

import java.util.Optional;

public class JwtTokenAuthenticationFilter implements WebFilter {
    private final JwtProperties jwtProperties;

    public JwtTokenAuthenticationFilter(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange serverWebExchange, WebFilterChain webFilterChain) {
        String token = resolveToken(serverWebExchange.getRequest());
        if (StringUtils.hasLength(token)) {
            // 验证token有效性
            Optional<Authentication> authenticationOptional = JwtTokenUtil.getAuthenticationByToken(token, jwtProperties.getSecretKey());
            Optional<String> sessionIdOptional = JwtTokenUtil.getSessionIdByToken(token, jwtProperties.getSecretKey());
            if (authenticationOptional.isPresent() && sessionIdOptional.isPresent()) {
                // 将sessionId给微服务
                // 获取原始请求对象和 HttpHeaders
                ServerHttpRequest request = serverWebExchange.getRequest();
                HttpHeaders headers = request.getHeaders();

                // 创建新的 HttpHeaders 对象，并添加自定义的请求头
                HttpHeaders modifiedHeaders = new HttpHeaders();
                modifiedHeaders.addAll(headers);
                modifiedHeaders.add("sessionId", sessionIdOptional.get());

                // 使用修改后的 HttpHeaders 设置为请求的标头
                ServerHttpRequest modifiedRequest = request.mutate()
                        .headers(httpHeaders -> httpHeaders.addAll(modifiedHeaders))
                        .build();

                // 使用修改后的请求对象创建新的 ServerWebExchange
                ServerWebExchange modifiedExchange = serverWebExchange.mutate().request(modifiedRequest).build();

                return webFilterChain.filter(modifiedExchange)
                        .subscriberContext(ReactiveSecurityContextHolder.withAuthentication(authenticationOptional.get()));
            }

        }
        return webFilterChain.filter(serverWebExchange);
    }

    private String resolveToken(ServerHttpRequest request) {
        String authorizationHeader = request.getHeaders().getFirst("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7); // 去掉 "Bearer " 前缀获取令牌值
            return token;
        }

        return null;
    }
}
