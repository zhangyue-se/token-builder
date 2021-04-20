package run;

import com.github.javaparser.ast.body.MethodDeclaration;
import config.PathConfig;
import util.MapUtil;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

/**
 * ZhangYue
 */
public class VocabularyUtil {
    private static int count = 0;
    /**
     * 更新type、token词表
     * @throws IOException
     */
    public static void buildVocabulary(List<MethodDeclaration> methodDeclarations) throws IOException {
        Map<String, Integer> mapWithTokenFrequency = new HashMap<>();
        Map<String, Integer> mapWithTypeFrequency = new HashMap<>();
        methodDeclarations.forEach(method->{
            String inorder = TokenVisitorSeqOrder.tokenOfSeqOrder(method);
            String[] inorderByLine = inorder.split("\n");
            for (int i = 0;i < inorderByLine.length; i++){
                String[] temp = inorderByLine[i].split(",");
                if (mapWithTokenFrequency.containsKey(temp[1])){
                    mapWithTokenFrequency.put(temp[1], mapWithTokenFrequency.get(temp[1]) + 1);
                }else {
                    mapWithTokenFrequency.put(temp[1], 1);
                }
                if (mapWithTypeFrequency.containsKey(temp[0])){
                    continue;
                }else {
                    mapWithTypeFrequency.put(temp[0],count++);
                }
            }
        });

        //将待写入的词表按照编号排序
        Map<String, Integer> mapType = MapUtil.sortMapByValues(mapWithTypeFrequency);
        Map<String, Integer> mapToken = MapUtil.sortMapByValues(mapWithTokenFrequency);

        BufferedWriter writerType = Files.newBufferedWriter(Paths.get(PathConfig.typeVocabularyPath), StandardCharsets.UTF_8, StandardOpenOption.APPEND);
        BufferedWriter writerToken = Files.newBufferedWriter(Paths.get(PathConfig.tokenVocabularyPath), StandardCharsets.UTF_8, StandardOpenOption.APPEND);

        //将type词表写入文件
        for (Map.Entry entry: mapType.entrySet()){
            writerType.write(entry.getKey() + " " + entry.getValue() + "\n");
        }
        //将token词表写入文件，根据频度写入
        count = 0;
        for (Map.Entry entry:mapToken.entrySet()){
            if ((Integer)entry.getValue() <= 3){
                continue;
            }else {
                writerToken.write(entry.getKey() + " " + (++count) + "\n");
            }
        }
        writerType.close();
        writerToken.close();
    }

    /**
     * 获取词表中的所有内容
     */
    public static Map<String,Integer> getVocabularyAsMap(String vocabularyPath) throws IOException {
        HashMap<String, Integer> map = new HashMap<>();
        FileInputStream fileInputStream = new FileInputStream(vocabularyPath);
        InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream,"UTF-8");
        BufferedReader in = new BufferedReader(inputStreamReader);
        String str = null;

        while ((str = in.readLine()) != null){
            String[] s = str.split("[ .?\n](?![ .?\n])");
            map.put(s[0],Integer.parseInt(s[1]));
        }
        return map;
    }

    public static void main(String[] args) {
        String[] s = "MethodDeclaration 0".split("[ .?\n](?![ .?\n])");
        System.out.println(s.length);
        System.out.println(s[1]);
        System.out.println(s[0]);
    }

}
