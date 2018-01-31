package org.clever.devops.utils;

import com.pty4j.PtyProcess;
import lombok.extern.slf4j.Slf4j;
import org.clever.common.model.exception.BusinessException;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

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
     * 所有 PtyProcess，启动时间戳 -> PtyProcess
     */
    private static final ConcurrentHashMap<Long, PtyProcess> PROCESS_MAP = new ConcurrentHashMap<>();

    /**
     * PtyProcess 超时时间
     */
    private static final long PROCESS_TIME_OUT = 1000 * 60 * 30;

    static {
        // 守护线程 TODO 使用线程池 ThreadPoolTaskExecutor
        Thread thread = new Thread(() -> {
            //noinspection InfiniteLoopStatement
            while (true) {
                List<Long> rmList = new ArrayList<>();
                int allCount = PROCESS_MAP.size();
                for (ConcurrentHashMap.Entry<Long, PtyProcess> entry : PROCESS_MAP.entrySet()) {
                    Long startTime = entry.getKey();
                    PtyProcess ptyProcess = entry.getValue();
                    if (!ptyProcess.isAlive() || (System.currentTimeMillis() - startTime) > PROCESS_TIME_OUT) {
                        rmList.add(startTime);
                        if (ptyProcess.isAlive()) {
                            ptyProcess.destroyForcibly();
                        }
                    }
                }
                for (Long startTime : rmList) {
                    PROCESS_MAP.remove(startTime);
                }
                log.info(String.format("Process总数[%1$s] 移除Process数[%2$s] 当前Process数[%3$s]", allCount, rmList.size(), PROCESS_MAP.size()));
                try {
                    Thread.sleep(1000 * 3);
                } catch (Throwable e) {
                    log.error("休眠失败", e);
                }
            }
        });
        thread.start();
    }

    /**
     * 在操作系统控制台上执行命令
     *
     * @param consoleOutput 回调接口
     * @param commands      需要执行的命令
     * @return 返回命令执行最后的返回值(一般成功返回 0)
     */
    public static int exec(ConsoleOutput consoleOutput, String[] commands) {
        int result = -1;
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
        try {
            result = process.waitFor();
        } catch (InterruptedException e) {
            log.error("进程被中断", e);
        }
        process.destroy();
        if (consoleOutput != null) {
            consoleOutput.completed();
        }
        return result;
    }
}
