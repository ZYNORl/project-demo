package top.zynorl.demo.gatewaydemo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import reactor.core.publisher.Mono;
import top.zynorl.demo.gatewaydemo.filter.JwtTokenAuthenticationFilter;
import top.zynorl.demo.gatewaydemo.handler.CustomHttpBasicServerAuthenticationEntryPoint;
import top.zynorl.demo.gatewaydemo.handler.TimingLogoutSuccessHandler;

@EnableWebFluxSecurity
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    //security的鉴权排除的url列表
    private static final String[] excludedAuthPages = {
            "/common",
    };

    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http,
                                                      CustomHttpBasicServerAuthenticationEntryPoint customHttpBasicServerAuthenticationEntryPoint,
                                                      TimingLogoutSuccessHandler timingLogoutSuccessHandler,
                                                      JwtProperties jwtProperties,
                                                      ReactiveAuthenticationManager reactiveAuthenticationManager) throws Exception {
        http.csrf().disable()
                .httpBasic().disable()
                .authenticationManager(reactiveAuthenticationManager)
                .exceptionHandling()
                .authenticationEntryPoint(customHttpBasicServerAuthenticationEntryPoint) // 自定义authenticationEntryPoint
                .accessDeniedHandler((swe, e) -> {
                    swe.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                    return swe.getResponse().writeWith(Mono.just(new DefaultDataBufferFactory().wrap("FORBIDDEN".getBytes())));
                })
                .and()
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                .authorizeExchange()
                .pathMatchers(excludedAuthPages).permitAll() //白名单
                .pathMatchers(HttpMethod.OPTIONS).permitAll()// option请求默认放行
                .anyExchange()
                .authenticated()
                .and()
                .logout().logoutUrl("/auth/logout") //退出
                .logoutSuccessHandler(timingLogoutSuccessHandler) // logoutSuccessHandler
                .and()
                .addFilterAt(new JwtTokenAuthenticationFilter(jwtProperties), SecurityWebFiltersOrder.HTTP_BASIC) //  增加tokenFilter
        ;

        return http.build();
    }
}
