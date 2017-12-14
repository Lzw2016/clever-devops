package org.clever.devops.utils;

import lombok.extern.slf4j.Slf4j;
import org.clever.common.model.exception.BusinessException;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * 执行 shell 命令工具类<br/>
 * 用于编译代码<br/>
 * <p>
 * 作者： lzw<br/>
 * 创建时间：2017-12-13 14:08 <br/>
 */
@Slf4j
public class ExecShellUtils {

    /**
     * 在操作系统控制台上执行命令
     *
     * @param consoleOutput 回调接口
     * @param commands      需要执行的命令
     */
    public static void exec(IConsoleOutput consoleOutput, String[] commands) {
        String cmd;
        if (OSValidatorUtils.isWindows()) {
            // Windows
            cmd = "cmd";
        } else if (OSValidatorUtils.isUnix()) {
            // Linux
            cmd = "/bin/sh";
        } else if (OSValidatorUtils.isMac()) {
            // Mac
            cmd = "/bin/sh";
        } else {
            throw new BusinessException("不支持的操作系统，目前仅支持“Windows”和“Linux”、“Mac”");
        }
        Process process;
        try {
            process = Runtime.getRuntime().exec(cmd);
        } catch (IOException e) {
            throw new BusinessException(String.format("调用系统控制台异常，命令[%1$s]", cmd));
        }
        if (consoleOutput != null) {
            new ConsoleOutputThread(consoleOutput, process.getInputStream()).start();
            new ConsoleOutputThread(consoleOutput, process.getErrorStream()).start();
        }
        PrintWriter stdin = new PrintWriter(process.getOutputStream());
        for (String command : commands) {
            stdin.println(command);
        }
        stdin.close();
        while (process.isAlive()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                log.error("线程中断请求，已忽略", e);
            }
        }
        process.destroy();
        if (consoleOutput != null) {
            consoleOutput.completed();
        }
    }
}
