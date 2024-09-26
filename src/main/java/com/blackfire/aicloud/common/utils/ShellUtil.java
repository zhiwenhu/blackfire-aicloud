package com.blackfire.aicloud.common.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author : zhaominglei
 * @description: 通过java执行命令
 */
@Slf4j
public class ShellUtil {

    /**
     * 执行Shell
     */
    public static boolean execShell(String command) {
        String[] commands = new String[]{"/bin/sh", "-c", command};
        return exec(commands);
    }

    /**
     * 执行Dos
     */
    public static boolean execDos(String command) {
        String[] commands = new String[]{"cmd", "/c", command};
        return exec(commands);
    }

    /**
     * 执行命令
     */
    public static boolean exec(String[] commands) {
        try {
            Process process = Runtime.getRuntime().exec(commands);
            //读取标准输出流
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder outLine = new StringBuilder();
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                outLine.append(line);
            }
            log.info(outLine.toString());
            //读取标准错误流
            BufferedReader brError = new BufferedReader(new InputStreamReader(process.getErrorStream(), "gb2312"));
            StringBuilder errorLine = new StringBuilder();
            String errline = null;
            while ((errline = brError.readLine()) != null) {
                errorLine.append(errline);
            }
            log.info(errorLine.toString());
            return process.waitFor() == 0 ? true : false;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }
}