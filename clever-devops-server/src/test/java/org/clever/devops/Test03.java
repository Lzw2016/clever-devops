package org.clever.devops;

import lombok.extern.slf4j.Slf4j;
import org.clever.devops.utils.CodeCompileUtils;
import org.clever.devops.utils.ConsoleOutput;
import org.clever.devops.utils.ExecShellUtils;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.AnsiWriter;
import org.jline.utils.AttributedStringBuilder;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;

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
//                "java -cp clever-devops-model-1.0.0-SNAPSHOT.jar org.clever.devops.Example",
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


    @Test
    public void test05() throws IOException {
//        AnsiString ansiString = new AnsiString("1234567890\b\b\n123\n456\r\033[k\n789\033[1Daa");
//        CharSequence s1 = ansiString.getPlain();
//        System.out.println(s1);
    }

    @Test
    public void test06() throws IOException {
//        AnsiConsole.systemInstall();
//        String str = "1234567890\b\b\n123\n456\r\033[k\n789\033[1Daa\n\033[31m红色字\033[0m";
        String str = "1234567890\b\b\n123\n456\r\033[K\n789\033[1Daa";
        System.out.println("1111111111111\r\033[k");
        System.out.println();
        StringWriter stringWriter = new StringWriter();
        AnsiWriter out = new AnsiWriter(stringWriter);

        try {
            out.write(str.toCharArray(), 0, str.length());
            out.flush();
            out.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String s1 = stringWriter.getBuffer().toString();
        System.out.println(s1);
//        AnsiConsole.systemUninstall();
    }

    @Test
    public void test07() throws IOException {
        AttributedStringBuilder sb = new AttributedStringBuilder();
        sb.ansiAppend("1234567890\b\b\n");

        String str = sb.toString();
        System.out.println(str);
        str = sb.toAnsi();
        System.out.println(str);
        str = sb.toAttributedString().toString();
        System.out.println(str);

    }

    @Test
    public void test08() throws IOException {
        TerminalBuilder builder = TerminalBuilder.builder();

//        log.info();
    }
}
