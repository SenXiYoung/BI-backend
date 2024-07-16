package com.yupi.springbootinit.manager;

import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.exception.BusinessException;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 *
 * 用于提供RedisLimiter 限流基础服务
 * @author cxy
 */
@Service
public class RedisLimiterManager {
    @Resource
    private RedissonClient redissonClient;

    /**
     * 限流操作
     * @param key 区分不同的限流器
     */
    public void doRateLimit(String key){
        //创建一个名为user_limiter的限流器 每秒最多访问2次
        RRateLimiter rRateLimiter = redissonClient.getRateLimiter(key);
        rRateLimiter.trySetRate(RateType.OVERALL,2,1, RateIntervalUnit.SECONDS);
        boolean canOp = rRateLimiter.tryAcquire();
        if(!canOp){
            throw new BusinessException(ErrorCode.TOO_MANY_REQUEST);
        }
    }
}
