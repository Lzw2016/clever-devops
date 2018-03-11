package org.clever.devops;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;
import org.fusesource.jansi.AnsiOutputStream;
import org.fusesource.jansi.AnsiRenderer;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * jansi
 *
 * 作者： lzw<br/>
 * 创建时间：2018-01-15 11:09 <br/>
 */
public class Test04 {

    @Test
    public void t01() {
        String str = Ansi.ansi().eraseScreen().fg(Ansi.Color.RED).a("Hello").fg(Ansi.Color.GREEN).a(" World").reset().toString();
        System.out.println(str);
    }

    @Test
    public void t02() {
        System.out.println(Ansi.ansi().eraseScreen().render("@|red Hello|@ @|green World|@"));
    }

    @Test
    public void t03() {
        AnsiConsole.systemInstall();
        AnsiConsole.out.println("Hello World");
        String str = Ansi.ansi().eraseScreen().fg(Ansi.Color.RED).a("Hello").fg(Ansi.Color.GREEN).a(" World").reset().toString();
        AnsiConsole.out.println(str);
    }

    @Test
    public void t04() throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        AnsiOutputStream ansiOutputStream = new AnsiOutputStream(byteArrayOutputStream);
        ansiOutputStream.write("123456\b\b\b789".getBytes());
        ansiOutputStream.flush();
        ansiOutputStream.close();
        String str = new String(byteArrayOutputStream.toByteArray());
        System.out.println(str);


        str = AnsiRenderer.render("123456\b\b\b789");

//        Ansi.ansi(new StringBuilder()).

        System.out.println(str);
    }
}
