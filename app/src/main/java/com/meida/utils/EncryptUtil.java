package com.meida.utils;

/**
 * 通过 MD5 对 key 值进行两次加密
 */
public class EncryptUtil {
    public static String appkey = "c306e6eb-fdba-11e7-9bb0-00163e0004bf";
    public static String sercret = "jZ0F9RTa5Y4NDZ95C4n38SuddBgtSw05";
    public static String DESIV = "";

    /**
     * MD5 进行两次加密
     */
    public static String getkey(String time) {
        String m1 = MD5Util.md5Password(time).substring(0, 8);
        String m2 = MD5Util.md5Password(m1.toUpperCase() + appkey);
        return m2.substring(12, 20).toLowerCase();
    }

    public static String getiv(String time) {
        String m1 = MD5Util.md5Password(time).substring(12, 20);
        String m2 = MD5Util.md5Password(m1.toLowerCase() + sercret);
        return m2.substring(24, 32).toUpperCase();
    }
}
