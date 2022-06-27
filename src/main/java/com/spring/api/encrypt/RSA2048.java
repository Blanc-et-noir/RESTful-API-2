package com.spring.api.encrypt;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;

import javax.crypto.Cipher;

public class RSA2048{
	
	private static final int KEY_SIZE = 2048;
	
	public static String encrypt(String plainData, String stringPublicKey) {
        String encryptedData = null;
        try {
            //평문으로 전달받은 공개키를 공개키객체로 만드는 과정
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            byte[] bytePublicKey = Base64.getDecoder().decode(stringPublicKey.getBytes());
            X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(bytePublicKey);
            PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);

            //만들어진 공개키객체를 기반으로 암호화모드로 설정하는 과정
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);

            //평문을 암호화하는 과정
            byte[] byteEncryptedData = cipher.doFinal(plainData.getBytes());
            encryptedData = Base64.getEncoder().encodeToString(byteEncryptedData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return encryptedData;
    }

	public static String decrypt(String encryptedData, String stringPrivateKey) {
		String decryptedData = null;
		try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            byte[] bytePrivateKey = Base64.getDecoder().decode(stringPrivateKey.getBytes());
            PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(bytePrivateKey);
            PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);

            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);

            byte[] byteEncryptedData = Base64.getDecoder().decode(encryptedData.getBytes());
            byte[] byteDecryptedData = cipher.doFinal(byteEncryptedData);
            decryptedData = new String(byteDecryptedData);
        }catch (Exception e) {
            e.printStackTrace();
        }
		return decryptedData;
	}

	public static HashMap<String,String> createKeys() {
		KeyPairGenerator gen;
		KeyPair keypair = null;
		try {
			HashMap<String,String> keyPair = new HashMap<String,String>();
			SecureRandom secureRandom = new SecureRandom();
			KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
			keyPairGenerator.initialize(KEY_SIZE, secureRandom);
			KeyPair kp = keyPairGenerator.genKeyPair();
	         
	        String user_publickey = Base64.getEncoder().encodeToString(((KeyPair) kp).getPublic().getEncoded());
	        String user_privatekey = Base64.getEncoder().encodeToString(((KeyPair) kp).getPrivate().getEncoded());
	         
	        keyPair.put("user_publickey", user_publickey);
	        keyPair.put("user_privatekey", user_privatekey);
	        return keyPair;
		}catch (Exception e) {
			e.printStackTrace();
			return null;
		}		
	}
}