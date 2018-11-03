package com.xxl.job.core.util;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.List;

/**
 * file tool
 *
 * @author xuxueli 2017-12-29 17:56:48
 */
@Slf4j
public class FileUtil {

    public static void deleteRecursively(File root) {
        if (null != root && root.exists()) {
            if (root.isDirectory()) {
                File[] children = root.listFiles();
                if (null != children) {
                    for (File child : children) {
                        deleteRecursively(child);
                    }
                }
            }
            root.delete();
        }
    }

    public static void deleteFile(String fileName) {
        File file = new File(fileName);
        if (file.exists()) {
            file.delete();
        }
    }

    public static void appendFileLine(String fileName, String content) {
        File file = new File(fileName);
        if (createFileIfNotExist(file)) {
            return;
        }

        content = Strings.nullToEmpty(content);
        content += "\r\n";

        appendContentToFile(content, file);
    }

    public static void appendContentToFile(String content, File file) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file, true);
            fos.write(content.getBytes("utf-8"));
            fos.flush();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }

    public static boolean createFileIfNotExist(File file) {
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                return true;
            }
        }
        return false;
    }

    public static List<String> loadFileLines(String fileName) {
        List<String> result = Lists.newArrayList();

        File file = new File(fileName);
        if (!file.exists()) {
            return result;
        }

        LineNumberReader reader = null;
        try {
            reader = new LineNumberReader(new InputStreamReader(new FileInputStream(file), "utf-8"));
            String line;
            while (null != (line = reader.readLine())) {
                if (line.trim().length() > 0) {
                    result.add(line);
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } finally {
            if (null != reader) {
                try {
                    reader.close();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }

        return result;
    }
}