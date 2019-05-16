package stephen.springboot.apigateway.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import stephen.springboot.apigateway.entity.ApiConfig;

import java.util.concurrent.TimeUnit;

/**
 * 授权服务
 * <p>
 * 校验用户是否具有访问权限
 */
@Slf4j
@Service
public class AuthorizationService {
    @Autowired
    private RedisTemplate<String, Long> redisTemplate;

    public boolean authorize(ApiConfig apiConfig) {
        int qps = apiConfig.getQps();
        long quota = apiConfig.getQuota();

        long time = System.currentTimeMillis() / 1000;
        String qpsUserKey = "__USER_QPS__" + apiConfig.getUser().getId();
        String qpsUserApiKey = "__USER_API_QPS__" + apiConfig.getUser().getId()
                + "_" + apiConfig.getApiMeta().getId()
                + "_" + time;

        Long cur = redisTemplate.opsForValue().get(qpsUserApiKey);
        if (cur != null && cur > qps) {
            return false;
        }
        long res = redisTemplate.opsForValue().increment(qpsUserApiKey);
        redisTemplate.expire(qpsUserApiKey, 5, TimeUnit.SECONDS);
        if (res > qps) {
            System.out.println(qpsUserApiKey + " incr to:" + res + ", fail");
            return false;
        } else {
            System.out.println(qpsUserApiKey + " incr to:" + res + ",success");
            return true;
        }
    }
}
