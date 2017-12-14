package org.clever.devops.utils;

/**
 * 控制台输出回写接口
 * 作者： lzw<br/>
 * 创建时间：2017-12-14 10:47 <br/>
 */
public interface IConsoleOutput {

    /**
     * 控制台输出数据
     *
     * @param line 输出行
     */
    void output(String line);

    /**
     * 调用完成回调
     */
    void completed();
}
