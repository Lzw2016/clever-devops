package org.clever.devops.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
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
     * 所有 PtyProcess，taskId -> PtyProcess
     */
    private static final ConcurrentHashMap<String, Terminal> PROCESS_MAP = new ConcurrentHashMap<>();

    /**
     * PtyProcess 超时时间 30分钟
     */
    private static final long PROCESS_TIME_OUT = 1000 * 60 * 30;

    static {
        // 守护线程 TODO 使用线程池 ThreadPoolTaskExecutor
        Thread thread = new Thread(() -> {
            //noinspection InfiniteLoopStatement
            while (true) {
                List<String> rmList = new ArrayList<>();
                int allCount = PROCESS_MAP.size();
                for (ConcurrentHashMap.Entry<String, Terminal> entry : PROCESS_MAP.entrySet()) {
                    String taskId = entry.getKey();
                    Terminal terminal = entry.getValue();
                    if (!terminal.isAlive() || terminal.getRunningTime() > PROCESS_TIME_OUT) {
                        if (terminal.isAlive()) {
                            try {
                                terminal.destroy();
                                rmList.add(taskId);
                            } catch (IOException e) {
                                log.error(String.format("释放%1$s任务失败", terminal.getClass().getSimpleName()), e);
                            }
                        } else {
                            rmList.add(taskId);
                        }
                    }
                }
                for (String taskId : rmList) {
                    PROCESS_MAP.remove(taskId);
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
    public static Terminal newTerminal(ConsoleOutput consoleOutput, String[] commands) {
        Terminal terminal = new Terminal(consoleOutput, commands);
        PROCESS_MAP.put(terminal.getTaskId(), terminal);
        return terminal;
    }


}
