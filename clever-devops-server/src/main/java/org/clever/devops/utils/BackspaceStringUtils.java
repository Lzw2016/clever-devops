package org.clever.devops.utils;

/**
 * 作者： lzw<br/>
 * 创建时间：2017-12-16 19:33 <br/>
 */
public class BackspaceStringUtils {

    /**
     * 返回退格字符
     *
     * @param count 退格字符数量
     */
    public static String getBackspaceStr(int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append('\b');
        }
        return sb.toString();
    }
}
