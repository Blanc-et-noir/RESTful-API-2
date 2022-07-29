package com.spring.api.util;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisUtil {
	//RSA2048 비밀키의 유효시간은 기본 3분
	@Value("${redis.privatekey_maxage}")
	public int PRIVATEKEY_MAXAGE;
	@Autowired
    private RedisTemplate redisTemplate = null;

	//Redis에서 데이터를 얻는 메소드
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

    //Redis에 데이터를 저장하는 메소드
    public void setData(String key, String value){
        redisTemplate.opsForValue().set(key,value);
    }

    //Redis에 유효시간동안 데이터를 저장하는 메소드
    public void setData(String key,String value,long duration){
		redisTemplate.opsForValue().set(key, value, duration, TimeUnit.MILLISECONDS);
    }

    //Redis에 데이터를 제거하는 메소드
    public void deleteData(String key){
    	redisTemplate.delete(key);
    }
}