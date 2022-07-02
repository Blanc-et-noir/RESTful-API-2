package com.spring.api.util;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisUtil {
	@Value("${redis.privatekey_maxage}")
	public int PRIVATEKEY_MAXAGE;
	@Autowired
    private RedisTemplate redisTemplate = null;

    public Object getData(String key){
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

    public void setData(String key, String value){
        redisTemplate.opsForValue().set(key,value);
    }

    public void setData(String key,String value,long duration){
		redisTemplate.opsForValue().set(key, value, duration, TimeUnit.MILLISECONDS);
    }

    public void deleteData(String key){
    	redisTemplate.delete(key);
    }
}