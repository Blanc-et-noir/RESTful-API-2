package com.spring.api.util;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisUtil {
	public final static int PRIVATEKEY_MAXAGE = 180*1000;
    private static RedisTemplate redisTemplate;

    @Autowired
    RedisUtil(RedisTemplate redisTemplate){
    	this.redisTemplate = redisTemplate;
    }
    
    public static Object getData(String key){
    	try {
    		if(key == null) {
    			return null;
    		}else {
    			return redisTemplate.opsForValue().get(key);
    		}
    	}catch(Exception e) {
    		return null;
    	}
    }

    public static void setData(String key, String value){
        redisTemplate.opsForValue().set(key,value);
    }

    public static void setData(String key,String value,long duration){
		redisTemplate.opsForValue().set(key, value, duration, TimeUnit.MILLISECONDS);
    }

    public static void deleteData(String key){
    	redisTemplate.delete(key);
    }
}