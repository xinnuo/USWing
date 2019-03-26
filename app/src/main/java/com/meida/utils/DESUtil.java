package com.meida.utils;

import android.util.Base64;

import java.security.Key;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;

public class DESUtil {

    public static final String ALGORITHM_DES = "DES/CBC/PKCS5Padding";

    public static String encode(String key, String data) {
        try {
            String str = encode(key, data.getBytes());
            return str;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String encodeIV(String key, String data, String iv) throws Exception {
        return encodeIV(key, data.getBytes(), iv);
    }

    public static String encode(String key, byte[] data) throws Exception {
        try {
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            Key secretKey = keyFactory.generateSecret(new DESKeySpec(key.getBytes()));
            Cipher cipher = Cipher.getInstance(ALGORITHM_DES);
            AlgorithmParameterSpec paramSpec = new IvParameterSpec(EncryptUtil.DESIV.getBytes());
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, paramSpec);

            return Base64.encodeToString(cipher.doFinal(data), Base64.DEFAULT);
        } catch (Exception e) {
            throw new Exception(e);
        }
    }

    public static String encodeIV(String key, byte[] data, String sic) throws Exception {
        try {
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            Key secretKey = keyFactory.generateSecret(new DESKeySpec(key.getBytes()));
            Cipher cipher = Cipher.getInstance(ALGORITHM_DES);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(sic.getBytes()));

            return Base64.encodeToString(cipher.doFinal(data), Base64.DEFAULT);
        } catch (Exception e) {
            throw new Exception(e);
        }
    }

    public static byte[] decode(String key, byte[] data) throws Exception {
        try {
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            Key secretKey = keyFactory.generateSecret(new DESKeySpec(key.getBytes()));
            Cipher cipher = Cipher.getInstance(ALGORITHM_DES);
            AlgorithmParameterSpec paramSpec = new IvParameterSpec(EncryptUtil.DESIV.getBytes());
            cipher.init(Cipher.DECRYPT_MODE, secretKey, paramSpec);

            return cipher.doFinal(data);
        } catch (Exception e) {
            throw new Exception(e);
        }
    }

    public static String decodeValue(String key, String data) throws Exception {
        byte[] datas = decode(key, Base64.decode(data, Base64.DEFAULT));
        if (datas.length == 0) return "";
        return new String(datas);
    }
}
