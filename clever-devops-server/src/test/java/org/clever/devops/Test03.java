package org.clever.devops;

import lombok.extern.slf4j.Slf4j;
import org.clever.devops.utils.CodeCompileUtils;
import org.clever.devops.utils.ExecShellUtils;
import org.clever.devops.utils.IConsoleOutput;
import org.junit.Test;

/**
 * 作者： lzw<br/>
 * 创建时间：2017-12-13 20:52 <br/>
 */
@Slf4j
public class Test03 {

    private IConsoleOutput consoleOutput = new IConsoleOutput() {
        @Override
        public void output(String line) {
            log.info(line);
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
    }

    @Test
    public void test02() {
        CodeCompileUtils.mvn(
                consoleOutput,
                "G:\\CodeDownloadPath\\loan-mall",
                new String[]{"clean", "package", "-U", "--global-settings=D:\\ToolsSoftware\\Maven\\settings.xml", "-Dmaven.test.skip=true"});
    }
}
