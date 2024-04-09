package com.nageoffer.shortlink.project.mq.idempotent;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 消息队列幂等处理器
 */
@Component
@RequiredArgsConstructor
public class MessageQueueIdempotentHandler {

    private final StringRedisTemplate stringRedisTemplate;

    private static final String IDEMPOTENT_KEY_PREFIX = "short-link:idempotent:";

    /**
     * 判断当前消息是否消费过
     * @param messageId：消息唯一标识
     * @return 消息是否消费过
     */
    public boolean isMessageProcessed(String messageId) {
        String key = IDEMPOTENT_KEY_PREFIX + messageId;
        // set成功，说明这个键不存在，当前消息可以消费，返回true
        // set失败，说明这个键已存在，当前消息不能消费，返回false
        return Boolean.TRUE.equals(stringRedisTemplate.opsForValue().setIfAbsent(key, "0", 5, TimeUnit.MINUTES));
    }

    /**
     * 判断消息消费流程是否执行完
     * @param messageId：消息唯一标识
     * @return 消息是否执行完
     */
    public boolean isAccomplish(String messageId) {
        String key = IDEMPOTENT_KEY_PREFIX + messageId;
        return Objects.equals(stringRedisTemplate.opsForValue().get(key), "1");
    }

    /**
     * 设置消息流程执行完成
     * @param messageId：消息唯一标识
     */
    public void setAccomplish(String messageId) {
        String key = IDEMPOTENT_KEY_PREFIX + messageId;
        // 先判断Key值是否为0，不为0说明已被删除
        // 在抛出异常之后，这里其实也不需要进行判断了
        if(Objects.equals(stringRedisTemplate.opsForValue().get(key), "0")){
            stringRedisTemplate.opsForValue().set(key, "1", 2, TimeUnit.MINUTES);
        }
    }

    /**
     * 如果消息处理遇到异常情况，删除幂等标识
     * @param messageId：消息唯一标识
     */
    public void delMessageProcessed(String messageId){
        String key = IDEMPOTENT_KEY_PREFIX + messageId;
        stringRedisTemplate.delete(key);
    }
}
