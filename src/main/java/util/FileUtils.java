package util;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ZhangYue
 */
public class FileUtils {
    public static List<String> javaPathList = new ArrayList<>();

    /**
     * 获取path下的所有Java文件路径
     * @param path
     */
    public static void getJavaPaths(String path){
        File file = new File(path);
        if (file.isDirectory()) {
            for(File file1:file.listFiles()){
                getJavaPaths(file1.getAbsolutePath());
            }
        }else {
            if (file.getName().endsWith(".java")&&file.isFile()){
                // 过滤掉超过200KB的文件
                if (file.length()/1024 <= 200) {
                    javaPathList.add(file.getAbsolutePath());
                }
            }
        }
    }

    public static List<String> readToFile(String path) throws IOException {
        List<String> list = new ArrayList<>();
        FileInputStream fileInputStream = new FileInputStream(path);
        InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream,"UTF-8");
        BufferedReader in = new BufferedReader(inputStreamReader);
        String str = null;

        while ((str = in.readLine()) != null){
            list.add(str);
        }
        return list;
    }

}
