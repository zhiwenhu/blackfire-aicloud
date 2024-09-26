package com.blackfire.aicloud.common.utils;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.spec.AlgorithmParameterSpec;

public class AesEncryptUtils {
    //参数分别代表 算法名称/加密模式/数据填充方式
    private static final String ALGORITHMSTR = "AES/CBC/PKCS5Padding";

    /**
     * 加密
     * @param content 加密的字符串
     * @param encryptKey key值
     * @return
     * @throws Exception
     */
    public static String encrypt(String content, byte[] encryptKey, String iv) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHMSTR);
        //偏移量
        IvParameterSpec ivSpec = new IvParameterSpec(iv.getBytes(StandardCharsets.UTF_8));
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(encryptKey, "AES"), ivSpec);
        byte[] b = cipher.doFinal(content.getBytes(StandardCharsets.UTF_8));
        // 采用base64算法进行转码,避免出现中文乱码
        return Base64.encodeBase64String(b);
    }

    /**
     * 解密
     * @param encryptStr 解密的字符串
     * @param decryptKey 解密的key值
     * @param iv 解密的加盐偏移值
     * @return
     * @throws Exception
     */
    public static String decrypt(String encryptStr, byte[] decryptKey, String iv) throws Exception {
        AlgorithmParameterSpec ivSpec = new IvParameterSpec(iv.getBytes(StandardCharsets.UTF_8));
        Cipher cipher = Cipher.getInstance(ALGORITHMSTR);
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(decryptKey, "AES"), ivSpec);
        // 采用base64算法进行转码,避免出现中文乱码
        byte[] encryptBytes = Base64.decodeBase64(encryptStr);
        byte[] decryptBytes = cipher.doFinal(encryptBytes);
        return new String(decryptBytes, StandardCharsets.UTF_8);
    }

    public static void main(String[] args) throws Exception {
        String contnet = "[{\"source\":\"ga.shanghai.dhk\",\"dataType\":\"qq\",\"value\":\"12345678\",\"md5\":\"25d55ad283aa400af464c76d713c07ad\",\"infoType\":\"dhk\",\"riskType\":3,\"riskLevel\":2,\"lastFoundTime\":\"2024-01-20 20:07:36\",\"extInfo\":[{\"key\":\"cardCount\",\"value\":\"1\"},{\"key\":\"address\",\"value\":\"上海市金山区蒙源路110号\"},{\"key\":\"lat\",\"value\":\"30.741291046142578\"},{\"key\":\"lon\",\"value\":\"121.3380355834961\"},{\"key\":\"coordtype\",\"value\":\"4\"},{\"key\":\"imgurl\",\"value\":\"https://dhk-1314638917.cos.ap-guangzhou.myqcloud.com/shanghai/20240120/1705752402tmp_b37d778df56acc6f2f3d5516cc95d13f.jpg\"},{\"key\":\"uploader\",\"value\":\"金山分局-松隐派出所\"},{\"key\":\"uploaderUin\",\"value\":\"o2bLE6325MxWXAFfXvjGbqWiFZVg\"}]}]";
        String aesKey = "FLXvGqZDfxVXkho0bcFKMzUkfnnq5whzh3S2qvm9Unk=";
        String ivStr = "eZPhm0mdtVtSUDkf";
        System.out.println("加密前：" + contnet);

        String encrypt = encrypt(contnet, Base64.decodeBase64(aesKey), ivStr);
        System.out.println("加密后：" + encrypt);

        String decrypt = decrypt(encrypt, Base64.decodeBase64(aesKey), ivStr);
        System.out.println("解密后：" + decrypt);
    }
}
