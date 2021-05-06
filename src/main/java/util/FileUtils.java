package util;

import java.io.*;
import java.nio.channels.FileChannel;
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
        String str;
        while ((str = in.readLine()) != null){
            list.add(str);
        }
        return list;
    }

    /**
     * 将三个文件的的内容拼接为一个文件
     */
    public static void mergeFiles(String[] paths, String FileOut) throws IOException {
        BufferedWriter bw=new BufferedWriter(new FileWriter(FileOut));
        //读取目录下的每个文件或者文件夹，并读取文件的内容写到目标文字中去
        List<File> list = new ArrayList<>();
        for (String ss:paths){
            list.add(new File(ss));
        }
        for(File file : list)
        {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while((line=br.readLine())!=null) {
                bw.write(line);
                bw.newLine();
            }
            br.close();
            file.delete();
        }
        bw.close();
    }
}
