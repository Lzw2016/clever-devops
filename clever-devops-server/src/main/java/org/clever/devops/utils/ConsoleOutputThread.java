package org.clever.devops.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

/**
 * 处理控制台数据流输出
 * <p>
 * 作者： lzw<br/>
 * 创建时间：2017-12-14 10:48 <br/>
 */
@Slf4j
public class ConsoleOutputThread extends Thread {
    private IConsoleOutput consoleOutput;
    private InputStream inputStream;

    public ConsoleOutputThread(IConsoleOutput consoleOutput, InputStream inputStream) {
        this.consoleOutput = consoleOutput;
        this.inputStream = inputStream;
    }

    @Override
    public void run() {
        if (consoleOutput == null) {
            return;
        }
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("GBK")));
            String line;
            while ((line = br.readLine()) != null) {
                consoleOutput.output(line);
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }
}
