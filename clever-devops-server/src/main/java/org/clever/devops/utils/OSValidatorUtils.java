package org.clever.devops.utils;

/**
 * 判断操作系统类型
 * <p>
 * 作者： lzw<br/>
 * 创建时间：2017-12-13 20:14 <br/>
 */
public class OSValidatorUtils {

    private static String OS = System.getProperty("os.name").toLowerCase();

    /**
     * Windows
     */
    public static boolean isWindows() {
        return (OS.contains("win"));
    }

    /**
     * Mac
     */
    public static boolean isMac() {
        return (OS.contains("mac"));
    }

    /**
     * Linux
     */
    public static boolean isUnix() {
        return (OS.contains("nix") || OS.contains("nux") || OS.indexOf("aix") > 0);
    }

    /**
     * Solaris (Sun OS)
     */
    public static boolean isSolaris() {
        return (OS.contains("sunos"));
    }
}