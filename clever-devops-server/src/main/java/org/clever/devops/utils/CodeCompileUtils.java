package org.clever.devops.utils;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.clever.common.model.exception.BusinessException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 项目编译打包工具类
 * <p>
 * 作者： lzw<br/>
 * 创建时间：2017-12-14 10:46 <br/>
 */
public class CodeCompileUtils {

    /**
     * 使用 mvn 命令编译打包项目
     *
     * @param consoleOutput 控制台回调
     * @param projectPath   项目路径
     * @param args          mvn 参数
     * @return 编译成功返回true
     */
    public static boolean mvn(ConsoleOutput consoleOutput, String projectPath, String[] args) {
        File file = new File(projectPath);
        // projectPath 不是文件夹或不存在
        if (!file.exists() || !file.isDirectory()) {
            throw new BusinessException(String.format("路径[%1$s]不是文件夹或不存在", projectPath));
        }
        if (args == null || args.length <= 0) {
            throw new BusinessException("需要执行的Maven命令不能为空");
        }
        // 组织需要执行的命令
        List<String> commands = new ArrayList<>();
        if (FilenameUtils.getPrefix(projectPath).contains(":")) {
            // Windows 下先进入对应的盘符下
            commands.add(String.format("%1$s:", FilenameUtils.getPrefix(projectPath).split(":")[0]));
        }
        commands.add(String.format("cd %1$s", projectPath));
        StringBuilder sb = new StringBuilder();
        sb.append("mvn ");
        for (String arg : args) {
            if (StringUtils.isNotBlank(arg)) {
                sb.append(StringUtils.trim(arg)).append(" ");
            }
        }
        commands.add(sb.toString());
        // 执行命令
        return ExecShellUtils.exec(consoleOutput, commands.toArray(new String[commands.size()])) == 0;
    }
}
