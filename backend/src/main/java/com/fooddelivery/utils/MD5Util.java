package com.fooddelivery.utils;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * MD5加密工具类
 */
public class MD5Util {

    /**
     * MD5加密
     */
    public static String encrypt(String text) {
        return DigestUtils.md5Hex(text);
    }

    /**
     * MD5加密（加盐）
     */
    public static String encrypt(String text, String salt) {
        return DigestUtils.md5Hex(text + salt);
    }

    /**
     * 验证密码
     */
    public static boolean verify(String text, String md5) {
        return encrypt(text).equalsIgnoreCase(md5);
    }

    /**
     * 验证密码（加盐）
     */
    public static boolean verify(String text, String salt, String md5) {
        return encrypt(text, salt).equalsIgnoreCase(md5);
    }
}
