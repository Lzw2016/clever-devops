package org.clever.devops;

import lombok.extern.slf4j.Slf4j;
import org.clever.devops.utils.CodeCompileUtils;
import org.clever.devops.utils.ConsoleOutput;
import org.clever.devops.utils.ExecShellUtils;
import org.junit.Test;

import java.io.IOException;

/**
 * 作者： lzw<br/>
 * 创建时间：2017-12-13 20:52 <br/>
 */
@Slf4j
public class Test03 {

    private StringBuilder stringBuilder = new StringBuilder();

    private ConsoleOutput consoleOutput = new ConsoleOutput() {
        @Override
        public void output(String line) {
            stringBuilder.append(line);
        }

        @Override
        public void completed() {
            log.info("完成");
        }
    };

    @Test
    public void test01() {
        log.info(System.getProperty("java.home"));
        // compile
        ExecShellUtils.exec(consoleOutput, new String[]{
                "G:",
                "cd G:\\CodeDownloadPath\\loan-mall",
                "mvn clean package -Dmaven.test.skip=true -U --global-settings=D:\\ToolsSoftware\\Maven\\settings.xml"
        });
        System.out.println(stringBuilder.toString());
    }

    @Test
    public void test02() {
        System.out.println("\033[0;31mhello \033[0;32mworld");
    }
}
