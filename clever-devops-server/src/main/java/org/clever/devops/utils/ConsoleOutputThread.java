package org.clever.devops.utils;

import lombok.extern.slf4j.Slf4j;

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
            final InputStreamReader reader = new InputStreamReader(inputStream, Charset.forName("GBK"));
            final int bufferSize = 1024;
            final char[] buffer = new char[bufferSize];
            int readSize;
            while ((readSize = reader.read(buffer, 0, buffer.length)) >= 0) {
                if (readSize > 0) {
                    consoleOutput.output(new String(buffer, 0, readSize));
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }
}
