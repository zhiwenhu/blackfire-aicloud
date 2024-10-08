package com.blackfire.aicloud.common.utils;

import cn.hutool.core.codec.Base64;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.util.UUID;

/**
 * PasswordUtil
 * Created by zhaominglei on 2023-09-13.
 */
@Slf4j
public class PasswordUtil {
    private PasswordUtil() {
        throw new IllegalStateException("PasswordUtil class");
    }
    /**
     * JAVA6支持以下任意一种算法 PBEWITHMD5ANDDES PBEWITHMD5ANDTRIPLEDES
     * PBEWITHSHAANDDESEDE PBEWITHSHA1ANDRC2_40 PBKDF2WITHHMACSHA1
     * */

    /**
     * 定义使用的算法为:PBEWITHMD5andDES算法
     * 加密算法
     */
    public static final String ALGORITHM = "PBEWithMD5AndDES";
    /**
     * 密钥
     */
    public static final String SALT = "63293188";

    /**
     * 定义迭代次数为1000次
     */
    private static final int ITERATIONCOUNT = 1000;

    /**
     * 获取加密算法中使用的盐值,解密中使用的盐值必须与加密中使用的相同才能完成操作. 盐长度必须为8字节
     *
     * @return byte[] 盐值
     */
    public static byte[] getSalt() {
        // 实例化安全随机数
        SecureRandom random = new SecureRandom();
        // 产出盐
        return random.generateSeed(8);
    }

    public static byte[] getStaticSalt() {
        // 产出盐
        return SALT.getBytes();
    }

    /**
     * 根据PBE密码生成一把密钥
     *
     * @param password 生成密钥时所使用的密码
     * @return Key PBE算法密钥
     */
    private static Key getPbeKey(String password) {
        // 实例化使用的算法
        SecretKeyFactory keyFactory;
        SecretKey secretKey = null;
        try {
            keyFactory = SecretKeyFactory.getInstance(ALGORITHM);
            // 设置PBE密钥参数
            PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray(), new byte[8], 65536, 256);
            // 生成密钥
            secretKey = keyFactory.generateSecret(keySpec);
        } catch (Exception e) {
            log.error("getPbeKey", e);
        }

        return secretKey;
    }

    /**
     * 加密明文字符串
     *
     * @param plaintext 待加密的明文字符串
     * @param password  生成密钥时所使用的密码
     * @param salt      盐值
     * @return 加密后的密文字符串
     * @throws Exception
     */
    public static String encrypt(String plaintext, String password, String salt) {
        Key key = getPbeKey(password);
        byte[] encipheredData = null;
        PBEParameterSpec parameterSpec = new PBEParameterSpec(salt.getBytes(), ITERATIONCOUNT, new IvParameterSpec(new byte[16]));
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);

            cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec);

            encipheredData = cipher.doFinal(plaintext.getBytes());
        } catch (Exception e) {
        }
        return bytesToHexString(encipheredData);
    }

    /**
     * 解密密文字符串
     *
     * @param ciphertext 待解密的密文字符串
     * @param password   生成密钥时所使用的密码(如需解密,该参数需要与加密时使用的一致)
     * @param salt       盐值(如需解密,该参数需要与加密时使用的一致)
     * @return 解密后的明文字符串
     * @throws Exception
     */
    public static String decrypt(String ciphertext, String password, String salt) {

        Key key = getPbeKey(password);
        byte[] passDec = null;
        PBEParameterSpec parameterSpec = new PBEParameterSpec(salt.getBytes(), ITERATIONCOUNT, new IvParameterSpec(new byte[16]));
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);

            cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec);

            passDec = cipher.doFinal(hexStringToBytes(ciphertext));
        } catch (Exception e) {
            // TODO: handle exception
        }
        return new String(passDec);
    }

    /**
     * 将字节数组转换为十六进制字符串
     *
     * @param src 字节数组
     * @return
     */
    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    /**
     * 将十六进制字符串转换为字节数组
     *
     * @param hexString 十六进制字符串
     * @return
     */
    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || "".equals(hexString)) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | (charToByte(hexChars[pos + 1])) & 0xff);
        }
        return d;
    }

    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    /**
     * 生成随机密码
     * 改下密码规则 与前端一致
     */
    public static String getRandomPassword(int len) {
        // 1、定义基本字符串baseStr和出参password
        String strMima = null;
        String baseStr = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ~!@#$%^&*()_+{}|<>?";
        boolean flag = false;
        // 2、使用循环来判断是否是正确的密码
        while (!flag) {
            // 密码重置
            strMima = "";
            // 个数计数
            int a = 0, b = 0, c = 0;
            try {
                for (int i = 0; i < len; i++) {
                    SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
                    int rand = (int) (random.nextInt(1000) / 1000.0 * baseStr.length());
                    strMima += baseStr.charAt(rand);
                    if (0 <= rand && rand < 10) { //数字
                        a++;
                    }
                    if (10 <= rand && rand < 36) { //字母
                        b++;
                    }
                    if (36 <= rand && rand < baseStr.length()) { //特殊字符
                        c++;
                    }
                    if (a * b * c != 0) { //同时都有
                        break;
                    }
                }
                // 是否是正确的密码（3类中不为0）
                flag = (a * b * c != 0 && strMima.length() == len);
            } catch (NoSuchAlgorithmException e) {
                String base = UUID.randomUUID().toString();
                if (len > base.length()) {
                    strMima = base;
                } else {
                    strMima = base.substring(0, len);
                }
            }
        }
        return strMima;
    }

    public static String getPhoneNumber(String content, String sessionKey,String ivStr) {
        try {
            if (StringUtils.isBlank(sessionKey)) {
                log.error("用户信息异常，sessionKey为空");
                return null;
            }
            // 开始解密
            byte[] encData = Base64.decode(content);
            byte[] iv = Base64.decode(ivStr);
            byte[] key = Base64.decode(sessionKey);
            AlgorithmParameterSpec ivSpec = new IvParameterSpec(iv);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            // 将解密结果 return
            return new String(cipher.doFinal(encData), StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("手机号解密失败", e);
            return null;
        }
    }

    public static void main(String[] args) {
        System.out.println(getRandomPassword(8));
    }
}