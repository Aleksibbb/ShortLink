package com.nageoffer.shortlink.gateway.filter;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.nageoffer.shortlink.gateway.config.Config;
import com.nageoffer.shortlink.gateway.dto.GatewayErrorResult;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

import static com.nageoffer.shortlink.gateway.constant.RedisConstant.USER_LOGIN_KEY;

/**
 * SpringCloud GateWay Token 拦截器
 * 作用：代替了 原后管系统中的 UserTransmitFilter
 */
@Component
public class TokenValidateGatewayFilterFactory extends AbstractGatewayFilterFactory<Config> {

    private final StringRedisTemplate stringRedisTemplate;

    public TokenValidateGatewayFilterFactory(StringRedisTemplate stringRedisTemplate) {
        super(Config.class);
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public GatewayFilter apply(Config config) {
        // exchange：表示当前的请求——响应交换对象； chain：表示网关过滤器链
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            // 获取请求路径 "/api/short-link/admin/v1/user"
            String requestPath = request.getPath().toString();
            // 获取请求的Http方法 post、get
            String requestMethod = request.getMethod().name();
            // 判断当前请求的路径和方法是否在白名单，不在白名单的需要执行该代码块
            if (!isPathInWhiteList(requestPath, requestMethod, config.getWhitePathList())) {
                String username = request.getHeaders().getFirst("username");
                String token = request.getHeaders().getFirst("token");
                Object userInfo;
                // 判断 username 、token、以及Redis中与用户登陆Key、token对应用户信息 是否存在
                if (StringUtils.hasText(username) && StringUtils.hasText(token) && (userInfo = stringRedisTemplate.opsForHash().get(USER_LOGIN_KEY + username, token)) != null) {
                    // 将用户信息解析为Json对象
                    JSONObject userInfoJsonObject = JSON.parseObject(userInfo.toString());
                    // 将 userId 、 realName 设为请求头信息
                    ServerHttpRequest.Builder builder = exchange.getRequest().mutate().headers(httpHeaders -> {
                        httpHeaders.set("userId", userInfoJsonObject.getString("id"));
                        httpHeaders.set("realName", URLEncoder.encode(userInfoJsonObject.getString("realName"), StandardCharsets.UTF_8));
                    });
                    // 将修改后的请求继续传递给下一个过滤器
                    return chain.filter(exchange.mutate().request(builder.build()).build());
                }
                // 用户信息不存在，设置 401 未授权状态码
                ServerHttpResponse response = exchange.getResponse();
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                // 将自定义错误信息写入响应体
                return response.writeWith(Mono.fromSupplier(() -> {
                    DataBufferFactory bufferFactory = response.bufferFactory();
                    GatewayErrorResult resultMessage = GatewayErrorResult.builder()
                            .status(HttpStatus.UNAUTHORIZED.value())
                            .message("Token validation error")
                            .build();
                    return bufferFactory.wrap(JSON.toJSONString(resultMessage).getBytes());
                }));
            }
            // 当前请求在白名单，继续传递给下一个过滤器
            return chain.filter(exchange);
        };
    }

    /**
     * 检查当前请求路径和方法是否在白名单
     * @param requestPath：请求路径
     * @param requestMethod：请求方法
     * @param whitePathList：白名单List
     * @return true：在白名单
     */
    private boolean isPathInWhiteList(String requestPath, String requestMethod, List<String> whitePathList) {
        return (!CollectionUtils.isEmpty(whitePathList) && whitePathList.stream().anyMatch(requestPath::startsWith)) || (Objects.equals(requestPath, "/api/short-link/admin/v1/user") && Objects.equals(requestMethod, "POST"));
    }
}
