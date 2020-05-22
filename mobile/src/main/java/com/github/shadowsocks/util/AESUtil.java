package com.github.shadowsocks.util;

import android.util.Base64;
import android.util.Log;

import com.google.android.gms.common.util.Hex;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;



public class AESUtil {
    public static final String KEY_ALGORITHM = "AES";
    // 加密模式为ECB，填充模式为NoPadding
    public static final String CIPHER_ALGORITHM = "AES/CBC/PKCS7Padding";
    // 字符集
    public static final String ENCODING = "UTF-8";
    // 向量
    public static final String IV_SEED = "1234567812345678";

    public static final String AES_KEY = "21ewqf6g8asdf324";

    /**
     * AES加密算法
     *
     * @param str 密文
     * @param key 密key
     * @return
     */
    public static String encrypt(String str, String key) {
        try {
            if (str == null) {
                Log.d("tag","AES加密出错:Key为空null");
                return null;
            }
            // 判断Key是否为16位
            if (key.length() != 16) {
                Log.d("test", "AES加密出错:Key长度不是16位");
                return null;
            }
            byte[] raw = key.getBytes(ENCODING);
            SecretKeySpec skeySpec = new SecretKeySpec(raw, KEY_ALGORITHM);
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            IvParameterSpec iv = new IvParameterSpec(IV_SEED.getBytes(ENCODING));
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
            byte[] srawt = str.getBytes(ENCODING);
            int len = srawt.length;
            /* 计算补空格后的长度 */
            while (len % 16 != 0)
                len++;
            byte[] sraw = new byte[len];
            /* 在最后空格 */
            for (int i = 0; i < len; ++i) {
                if (i < srawt.length) {
                    sraw[i] = srawt[i];
                } else {
                    sraw[i] = 32;
                }
            }
            byte[] encrypted = cipher.doFinal(sraw);
            String result = Hex.bytesToStringLowercase(encrypted);
//            String result = formatString(new String(Base64.encode(encrypted, Base64.DEFAULT), ENCODING));
            //Log.e("test", "AESUtil encrypt key:" + key + " from:" + str + " to:" + result);
            return result;
        } catch (Exception ex) {
            //Log.e("test","AES加密出错：" + ex.toString());
            return null;
        }
    }

    /**
     * AES解密算法
     *
     * @param str 密文
     * @param key 密key
     * @return
     */
    public static String decrypt(String str, String key) {
        try {
            // 判断Key是否正确
            if (key == null) {
                //Log.e("test","AES解密出错:Key为空null");
                return null;
            }
            // 判断Key是否为16位
            if (key.length() != 16) {
                //Log.e("test","AES解密出错：Key长度不是16位");
                return null;
            }
            byte[] raw = key.getBytes(ENCODING);
            SecretKeySpec skeySpec = new SecretKeySpec(raw, KEY_ALGORITHM);
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            IvParameterSpec iv = new IvParameterSpec(IV_SEED.getBytes(ENCODING));
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

            byte[] bytes = Hex.stringToBytes(str);
//            byte[] bytes = Base64.decode(str.getBytes(ENCODING), Base64.DEFAULT);
            bytes = cipher.doFinal(bytes);
            String result = new String(bytes, ENCODING);
            //Log.e("test", "AESUtil decrypt key:" + key + " from:" + str + " to:" + result);
            return result;
        } catch (Exception ex) {
            //Log.e("test","AES解密出错：" + ex.toString());
            return null;
        }
    }

    private static String formatString(String sourceStr) {
        if (sourceStr == null) {
            return null;
        }
        return sourceStr.replaceAll("\\r", "").replaceAll("\\n", "");
    }

    public static void main(String[] args) {
        String aa = encrypt("123", "21ewqf6g8asdf324");
        System.out.println("encrypt:" + aa);
        String bb = decrypt(aa, "21ewqf6g8asdf324");
        System.out.println("decrypt:" + bb);
        int a = 1;
        int b = 1;
    }
}