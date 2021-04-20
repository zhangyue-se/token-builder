package test;

import util.FileUtils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

/**
 * ZhangYue
 */
public class FindDiffience {

    public static void main(String[] args) throws IOException {
        String inorder = new String("/Users/zhangyue/IdeaProjects/juc/src/main/resources/test/inorder.txt");
        String seq = new String("/Users/zhangyue/IdeaProjects/juc/src/main/resources/test/seq.txt");
        List<String> list1 = FileUtils.readToFile(inorder);
        List<String> list2 = FileUtils.readToFile(seq);
        System.out.println(list1.size());
        System.out.println(list2.size());
        int count = 0;
        if (list1.size()> list2.size()){
            for (String s:list1){
                if (!list2.contains(s)){
                    System.out.println(s);
                }
            }
        }else {
            for (String s:list2){
                if (!list1.contains(s)){
                    System.out.println(s);
                }
            }
        }

        System.out.println("重复的token。。。。");
        Map<String,Integer> map = new HashMap<>();
        for (String ss : list1){
            if (map.containsKey(ss)){
                map.put(ss,map.get(ss)+1);
            }else {
                map.put(ss,1);
            }
        }
        for (Map.Entry entry: map.entrySet()){
            if ((Integer)entry.getValue()>1){
                System.out.println(entry.getKey());
            }
        }

    }
}
