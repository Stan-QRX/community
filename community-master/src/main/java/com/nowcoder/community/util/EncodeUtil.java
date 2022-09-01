package com.nowcoder.community.util;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class EncodeUtil {
    /**
     * 使用SHA256加密
     *
     * @param toEncode 要加密的字符串
     * @param salt     盐
     * @param roll     重复加密次数,不要超过11次
     * @return 加密后的字符串
     */
    public static String encodeBySha(String toEncode, String salt, Integer roll) {
        if (StringUtils.isEmpty(toEncode))//软件工程1801
            throw new RuntimeException("加密字符串为空");
        if (roll < 0 || roll > 11)
            throw new RuntimeException("重复加密次数有误");

        //要加密的字符串和盐组合
        String encodeStr = toEncode + salt;
        byte[] bytes = encodeStr.getBytes();
        //重复一次等与加密两次
        for (int i = 0; i < roll + 1; i++) {
            //使用org.apache.commons.codec的工具类对字符串进行SHA256加密
            bytes = DigestUtils.sha256(bytes);
        }
        //将加密好的byte转换为16进制字符串
        encodeStr = Hex.encodeHexString(bytes);

        return encodeStr;
    }

    /**
     * 传入文本内容，返回 SHA-256 串
     *
     * @param strText
     * @return
     */
    public String SHA256(final String strText) {
        return SHA(strText, "SHA-256");
    }

    /**
     * 传入文本内容，返回 SHA-512 串
     *
     * @param strText
     * @return
     */
    public String SHA512(final String strText) {
        return SHA(strText, "SHA-512");
    }

    /**
     * md5加密
     * @param strText
     * @return
     */
    public String SHAMD5(String strText) {
        return SHA(strText, "MD5");
    }

    /**
     * 字符串 SHA 加密
     *
     * @param strText
     * @param strType
     * @return
     */
    //  System.out.println(SHA("123456"+UUID.randomUUID().toString().replaceAll("-",""),"SHA-512"));
    private String SHA(final String strText, final String strType) {
        // 返回值
        String strResult = null;

        // 是否是有效字符串
        if (strText != null && strText.length() > 0) {
            try {
                // SHA 加密开始
                // 创建加密对象 并傳入加密類型
                MessageDigest messageDigest = MessageDigest.getInstance(strType);
                // 传入要加密的字符串
                messageDigest.update(strText.getBytes());
                // 得到 byte 類型结果
                byte byteBuffer[] = messageDigest.digest();

                // 將 byte 轉換爲 string
                StringBuffer strHexString = new StringBuffer();
                // 遍歷 byte buffer
                for (int i = 0; i < byteBuffer.length; i++) {
                    // 返回16进制字符串
                    String hex = Integer.toHexString(0xff & byteBuffer[i]);
                    if (hex.length() == 1) {
                        strHexString.append('0');
                    }
                    strHexString.append(hex);
                }
                // 得到返回結果
                strResult = strHexString.toString();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }

        return strResult;
    }


    /**
     * 生成盐值;其长度在17~29之间，使用A~Z,a~z,0~9组成随机字符串
     *
     * @return 盐
     */
    private static String generateSalt() {
        return RandomStringUtils.randomAlphanumeric(17, 29);
    }
}
