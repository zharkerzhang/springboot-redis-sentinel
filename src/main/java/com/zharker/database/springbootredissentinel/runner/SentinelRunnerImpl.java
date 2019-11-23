package com.zharker.database.springbootredissentinel.runner;

import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
public class SentinelRunnerImpl implements ApplicationRunner {

    @Value("${redis.keep.time:60}")
    private long keepTime;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    @Override
    public void run(ApplicationArguments args) throws Exception {

        boolean sentinelConnected = redisTemplate.getRequiredConnectionFactory().getSentinelConnection().isOpen();
        log.info("redis sentinel is open:[{}]",sentinelConnected);
        Map<String,String> argsMap =
        args.getOptionNames().stream().collect(Collectors.toMap(arg->arg,arg->args.getOptionValues(arg).stream().collect(Collectors.joining(","))));

        if(argsMap.containsKey("operate") && argsMap.get("operate").equals("get")
            && argsMap.containsKey("key")){
            String key = argsMap.get("key");
            if(StringUtil.isNullOrEmpty(key)){
                return ;
            }
            String value = redisTemplate.opsForValue().get(key);
            log.info("get key: {} from redis, the value is: {}",key,value);
        }else if(argsMap.containsKey("operate") && argsMap.get("operate").equals("set")
                && argsMap.containsKey("key") && argsMap.containsKey("value") ){
            String key = argsMap.get("key");
            String value = argsMap.get("value");
            redisTemplate.opsForValue().set(key,value,keepTime, TimeUnit.SECONDS);
            log.info("set value: {} of key: {} on redis temporarily save {} seconds",value,key,keepTime);
        }
    }
}
