package org.clever.devops;

import lombok.extern.slf4j.Slf4j;
import org.clever.devops.utils.CodeCompileUtils;
import org.clever.devops.utils.ConsoleOutput;
import org.clever.devops.utils.ExecShellUtils;
import org.junit.Test;

/**
 * 作者： lzw<br/>
 * 创建时间：2017-12-13 20:52 <br/>
 */
@Slf4j
public class Test03 {

    private ConsoleOutput consoleOutput = new ConsoleOutput() {
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

    @Test
    public void test03() {
        log.info(System.getProperty("java.home"));
        // compile
        ExecShellUtils.exec(consoleOutput, new String[]{
                "G:",
                "java -cp clever-devops-model-1.0.0-SNAPSHOT.jar org.clever.devops.App",
        });
    }

    @Test
    public void test04() {
        StringBuilder sb = new StringBuilder();
        sb.append("0123456789\n9876543210\n");
        int start = sb.lastIndexOf("\n") + 1;
        sb.delete(start, sb.length());
        log.info(sb.toString());
    }
}
