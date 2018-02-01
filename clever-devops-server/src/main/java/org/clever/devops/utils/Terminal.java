package org.clever.devops.utils;

import com.google.common.collect.Maps;
import com.pty4j.PtyProcess;
import com.pty4j.WinSize;
import com.sun.jna.Platform;
import lombok.extern.slf4j.Slf4j;
import org.clever.common.model.exception.BusinessException;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 作者： lzw<br/>
 * 创建时间：2018-02-01 10:30 <br/>
 */
@Slf4j
public class Terminal implements Closeable {

    private PtyProcess process;
    private BufferedWriter outputWriter;
    private LinkedBlockingQueue<String> commandQueue = new LinkedBlockingQueue<>();
    private ConsoleOutput consoleOutput;
    private final Long startTime;
    private final String taskId = UUID.randomUUID().toString();

    /**
     * 初始化 Terminal
     */
    public Terminal(ConsoleOutput consoleOutput) {
        this(consoleOutput, null);
    }

    /**
     * 初始化 Terminal
     */
    public Terminal(ConsoleOutput consoleOutput, List<String> commands) {
        this.consoleOutput = consoleOutput;
        Map<String, String> envs = Maps.newHashMap(System.getenv());
        String[] termCommand;
        if (Platform.isWindows()) {
            termCommand = new String[]{"cmd.exe"};
        } else {
            termCommand = new String[]{"/bin/bash", "--login"};
            envs.put("TERM", "xterm");
        }
        String userHome = System.getProperty("user.home");
        try {
            process = PtyProcess.exec(termCommand, envs, userHome, false, false, null);
        } catch (IOException e) {
            throw new BusinessException("PtyProcess 初始化失败", e);
        }
        process.setWinSize(new WinSize(80, 10));
        if (this.consoleOutput != null) {
            new Thread(() -> printReader(process.getInputStream(), this.consoleOutput)).start(); // out
            new Thread(() -> printReader(process.getErrorStream(), this.consoleOutput)).start(); // err
        }
        outputWriter = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
        startTime = System.currentTimeMillis();
        if (commands != null && commands.size() > 0) {
            commands.forEach(this::onCommand);
        }
    }

    /**
     * 打印输出到终端
     */
    private static void printReader(InputStream inputStream, ConsoleOutput consoleOutput) {
        if (consoleOutput == null) {
            return;
        }
        try {
            int nRead;
            byte[] data = new byte[1024];
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                consoleOutput.output(new String(data, 0, nRead));
            }
        } catch (Exception e) {
            log.error("Terminal Print Error", e);
        }
    }

    /**
     * 处理输入命令
     */
    public void onCommand(String... commands) {
        if (commands == null) {
            return;
        }
        for (String str : commands) {
            commandQueue.offer(str);
        }
        new Thread(() -> {
            String cmd = null;
            try {
                while ((cmd = commandQueue.poll()) != null) {
                    outputWriter.write(cmd);
                }
                outputWriter.flush();
            } catch (IOException e) {
                log.error(String.format("执行命令[%1$s]失败", cmd), e);
            }
        }).start();
    }

    /**
     * 改变终端大小
     */
    public void onTerminalResize(int columns, int rows) {
        if (process != null) {
            process.setWinSize(new WinSize(columns, rows));
        }
    }

    @Override
    public void close() throws IOException {
        if (process != null) {
            if (process.getInputStream() != null) {
                process.getInputStream().close();
            }
            if (process.getErrorStream() != null) {
                process.getErrorStream().close();
            }
            if (process.getOutputStream() != null) {
                process.getOutputStream().close();
            }
            process.destroyForcibly();
        }
        if (outputWriter != null) {
            outputWriter.close();
        }
    }

    public void destroy() throws IOException {
        close();
    }

    /**
     * 获取任务ID
     */
    public String getTaskId() {
        return taskId;
    }

    /**
     * 得到启动时间戳
     */
    public Long getStartTime() {
        return startTime;
    }

    /**
     * 获取运行的时间 (毫秒)
     */
    public Long getRunningTime() {
        return System.currentTimeMillis() - startTime;
    }

    /**
     * 终端是否停止
     */
    public boolean isAlive() {
        return process != null && process.isAlive();
    }

    /**
     * 等待进程退出
     */
    public int waitFor() {
        Integer result = null;
        while (result == null) {
            try {
                result = process.waitFor();
            } catch (InterruptedException e) {
                log.warn("Process WaitFor 中断", e);
            }
        }
        if (consoleOutput != null) {
            consoleOutput.completed();
        }
        return result;
    }
}
