package top.zynorl.demo.gatewaydemo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import top.zynorl.demo.gatewaydemo.domin.AuthUserDetails;
import top.zynorl.demo.gatewaydemo.domin.DBUser;
import top.zynorl.demo.gatewaydemo.mapper.UserMapper;
import top.zynorl.demo.gatewaydemo.util.JwtTokenUtil;
import top.zynorl.demo.gatewaydemo.util.RedisCache;
import top.zynorl.demo.gatewaydemo.util.UUIDUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @version 1.0
 * @Author niuzy
 * @Date 2024/01/01
 **/
@Configuration
public class CustomAuthenticationProvider implements AuthenticationProvider {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private JwtProperties jwtProperties;
    @Autowired
    private RedisCache redisCache;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        // 获取用户提供的凭证信息
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();

        // 验证用户凭证，并根据验证结果生成认证对象
        DBUser dbUser = validateCredentials(username, password);
        if (dbUser != null) {
            AuthUserDetails userDetails = buildUserDetail(dbUser);
            Map<String, Object> map = new HashMap<>(1);
            map.put("user-info", userDetails);
            // 为此次验证生成sessionID,和authentication一并存入redis，实现分布式会话
            String sessionId = UUIDUtil.getRandomSlimUUID();
            map.put("sessionId", sessionId);
            redisCache.setCacheObject(sessionId, authentication, jwtProperties.getValidateInMs(), TimeUnit.SECONDS);
            // 生成token
            String jwtToken = JwtTokenUtil.createJavaWebToken4JwtAuth(map, jwtProperties.getSecretKey(), jwtProperties.getValidateInMs());
            return new UsernamePasswordAuthenticationToken(userDetails, jwtToken, userDetails.getAuthorities());
        } else {
            throw new BadCredentialsException("Invalid credentials");
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return false;
    }

    /**
     * 待登录人与数据库的验证
     *
     * @param username
     * @param password
     * @return
     */
    private DBUser validateCredentials(String username, String password) {
        DBUser dbUser = userMapper.selectUserByUsername(username);
        if (dbUser != null) {
            BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
            boolean isMatch = bCryptPasswordEncoder.matches(password, dbUser.getPassword());
            return isMatch ? dbUser : null;
        }
        return null;
    }

    /**
     * 构建UserDetail
     *
     * @param dbUser
     * @return
     */
    private AuthUserDetails buildUserDetail(DBUser dbUser) {
        AuthUserDetails authUserDetails = new AuthUserDetails();
        // 从数据库获取
        authUserDetails.setUsername(dbUser.getUsername());
        authUserDetails.setRole(dbUser.getRole());
        return authUserDetails;
    }
}
