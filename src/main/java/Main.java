import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import config.PathConfig;
import lombok.SneakyThrows;
import run.TokenInfo;
import run.TokenVisitorInOrder;
import run.TokenVisitorSeqOrder;
import run.VocabularyUtil;
import util.FileUtils;
import util.Utils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

/**
 * ZhangYue
 */
public class Main {

    static Map<String, Integer> vocabularyOfType = new HashMap<>();     //存放type词表
    static Map<String, Integer> vocabularyOfToken = new HashMap<>();    //存放token词表
    static int count1 = 0;//用来计数出问题的方法
    static boolean flag1 = false;    //用来记录三个生成结果的线程有没有执行结束
    static boolean flag2 = false;    //用来记录三个生成结果的线程有没有执行结束
    static boolean flag3 = false;    //用来记录三个生成结果的线程有没有执行结束

    public static void main(String[] args) throws IOException {

        //清空文件中的内容
        File file = new File(PathConfig.result);
        for (File file1 : file.listFiles()){
            if (file1.isFile()){
                FileWriter fileWriter =new FileWriter(file1);
                fileWriter.write("");
                fileWriter.flush();
                fileWriter.close();
            }
        }
        System.out.println("开始获取所有java文件路径.......");
        FileUtils.getJavaPaths(PathConfig.rootPath);
        List<String> javaPathList = FileUtils.javaPathList;
        System.out.println("获取所有java文件路径完成，一共获取了" + javaPathList.size() + "个java文件...");
        System.out.println();

        System.out.println("开始获取所有的MethodDeclaration.......");
        List<MethodDeclaration> methodDeclarationList = new ArrayList<>();
        int count = 0;
        for (String path:javaPathList){
            System.out.println("已经处理：" + (++count) + "个java文件");
            new VoidVisitorAdapter<Void>(){
                @Override
                public void visit(MethodDeclaration n, Void arg) {
                    super.visit(n, arg);
                    methodDeclarationList.add(n);
                }
            }.visit(StaticJavaParser.parse(new File(path)),null);
        }
        System.out.println("获取所有的MethodDeclaration完成，一共获取了" + methodDeclarationList.size() + "个方法体...");
        System.out.println();

        System.out.println("开始构建词表.......");
        //构建词表
        VocabularyUtil.buildVocabulary(methodDeclarationList);
        System.out.println("构建词表完成.......");
        System.out.println();
        // 读取词表
        vocabularyOfType = VocabularyUtil.getVocabularyAsMap(PathConfig.typeVocabularyPath);
        vocabularyOfToken = VocabularyUtil.getVocabularyAsMap(PathConfig.tokenVocabularyPath);

        System.out.println("开始生成训练数据.......");

        //生成训练所需要的数据格式
        threadRun(methodDeclarationList);

//        for (MethodDeclaration method:methodDeclarationList){
//            System.out.println("正在处理第" + (++count2) + "个方法体，已完成：" + count2 + "/" + all);
//            String inorder = TokenVisitorInOrder.tokenOfInOrder(method);
//            String seqOrder = TokenVisitorSeqOrder.tokenOfSeqOrder(method);
//            handleToken(inorder, seqOrder, method);
//        }

        while (!flag1 || !flag2 || !flag3){
            System.out.println(flag1+" "+flag2+" "+flag3);
        }
        System.out.println("开始合并文件.......");
        String[] mergerTruthPath = {"src/main/resources/result/result1/truth.txt","src/main/resources/result/result2/truth.txt","src/main/resources/result/result3/truth.txt"};
        FileUtils.mergeFiles(mergerTruthPath,PathConfig.truthPath);
        String[] mergerInorderPath = {"src/main/resources/result/result1/inorder.txt","src/main/resources/result/result2/inorder.txt","src/main/resources/result/result3/inorder.txt"};
        FileUtils.mergeFiles(mergerInorderPath,PathConfig.inorderPath);
        String[] mergerSeqPath = {"src/main/resources/result/result1/seq.txt","src/main/resources/result/result2/seq.txt","src/main/resources/result/result3/seq.txt"};
        FileUtils.mergeFiles(mergerSeqPath,PathConfig.seqPath);

    }

    /**
     * 处理序列生成特定的数据格式
     * @param inorder
     * @param seqOrder
     * @throws IOException
     */
    public static void handleToken(String inorder, String seqOrder, MethodDeclaration method, BufferedWriter writer1,
                                   BufferedWriter writer2, BufferedWriter writer3) throws IOException {
        //将中序序列处理为TokenInfo类型的list
        List<TokenInfo> tokenListOfInorder = new ArrayList<>();
        String[] inorderByLine = inorder.split("\n");
        for (String string : inorderByLine){
            String[] token = string.split(",");
            TokenInfo tokenInfo = new TokenInfo(token[0], token[1], Integer.parseInt(token[2]), Integer.parseInt(token[3].split("\r")[0]));
            tokenListOfInorder.add(tokenInfo);
        }

        //将顺序序列处理为list
        List<TokenInfo> tokenListOfSeqOrder = new ArrayList<>();
        String[] seqOrderByLine = seqOrder.split("\n");
        for (String string : seqOrderByLine){
            String[] token = string.split(",");
            TokenInfo tokenInfo = new TokenInfo(token[0], token[1], Integer.parseInt(token[2]), Integer.parseInt(token[3]));
            tokenListOfSeqOrder.add(tokenInfo);
        }

        //获取token和type词表
        if (tokenListOfInorder.size() == tokenListOfSeqOrder.size()){
            if (!Utils.isEqualOfTwoList(tokenListOfInorder,tokenListOfSeqOrder)){
                System.out.println("两个方法不一致,目前一共有" + (++count1) + "个方法有问题");
                return;
            }
            for (int i = tokenListOfSeqOrder.size()-2; i >= 0; i--){
                //经处理获取长度为i的序列
                StringBuilder stringBuilder1 = new StringBuilder();
                for (int j = 0;j <= i; j++){
                    Integer tokenId = vocabularyOfToken.get(tokenListOfSeqOrder.get(j).getToken());
                    if(tokenId == null){
                        continue;
                    }
                    stringBuilder1.append(vocabularyOfType.get(tokenListOfSeqOrder.get(j).getType()) + "," + vocabularyOfToken.get(tokenListOfSeqOrder.get(j).getToken()) + " ");
                }
                //获取当前顺序序列token对应的中序序列
                StringBuilder stringBuilder2 =getInorderResult(tokenListOfSeqOrder,i+1,tokenListOfInorder, vocabularyOfType, vocabularyOfToken);

                //将处理好的序列写入文件
                if (stringBuilder2.toString().contains("null")||vocabularyOfToken.get(tokenListOfSeqOrder.get(i+1).getToken())==null||
                        vocabularyOfType.get(tokenListOfSeqOrder.get(i+1).getType())==null){
                    continue;
                }
                writer1.write(stringBuilder1.toString() + "\n");
                writer2.write(stringBuilder2.toString() + "\n");
                //写入正确结果
                writer3.write(vocabularyOfType.get(tokenListOfSeqOrder.get(i+1).getType()) + "," + vocabularyOfToken.get(tokenListOfSeqOrder.get(i+1).getToken()) + "\n");

            }
        }else {
            System.out.println("该方法token个数不一致！！,目前一共有" + (++count1) + "个方法有问题");
        }
    }

    /**
     * 根据顺序序列处理中序序列，将顺序中删除的token在中序中也删除，并返回结果
     * @param list
     * @param currPos
     * @param list1
     * @return
     */
    public static StringBuilder getInorderResult(List<TokenInfo> list, int currPos, List<TokenInfo> list1, Map<String, Integer> vocabularyOfType, Map<String, Integer> vocabularyOfToken){
        List<TokenInfo> list2 = new ArrayList<>(list1);
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = currPos;i < list.size(); i++){
            for (TokenInfo tokenInfo:list2){
                if (list.get(i).equals(tokenInfo)){
                    list2.remove(tokenInfo);
                    break;
                }
            }
        }
        for (TokenInfo tokenInfo:list2){
            stringBuilder.append(vocabularyOfType.get(tokenInfo.getType()) + "," + vocabularyOfToken.get(tokenInfo.getToken()) + " ");
        }
        return stringBuilder;
    }

    /**
     * 将两种顺序的序列不做处理存入txt文件中
     * @param root
     * @throws FileNotFoundException
     */
    public static void storeResult(String root) throws FileNotFoundException {
        FileUtils.getJavaPaths(root);
        List<String> allJavaPath = FileUtils.javaPathList;
        List<MethodDeclaration> allMethod = new ArrayList<>();
        Random random = new Random();
        for (String path : allJavaPath){
            new VoidVisitorAdapter<Object>(){
                @Override
                public void visit(MethodDeclaration n, Object arg) {
                    super.visit(n, arg);
                    int begin = n.getBegin().get().line;
                    int end = n.getEnd().get().line;
                    if (end - begin >= 5){
                        allMethod.add(n);
                    }
                }
            }.visit(StaticJavaParser.parse(new File(path)),null);
        }
        allMethod.forEach(method->{
            String resultOfSeqOrder = TokenVisitorSeqOrder.tokenOfSeqOrder(method);
            String resultOfInOrder = TokenVisitorInOrder.tokenOfInOrder(method);
            int numberLineOfSeq = resultOfSeqOrder.split("\n").length;
            int numberLineOfIn = resultOfInOrder.split("\r\n").length;
            if (numberLineOfIn == numberLineOfSeq){
                String tokenTxtSeq = "seq" + random.nextInt(10000);
                Path path1 = Paths.get(tokenTxtSeq);
                try(BufferedWriter writer = Files.newBufferedWriter(path1, StandardCharsets.UTF_8)) {
                    writer.write(resultOfSeqOrder);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                String tokenTxtIn = tokenTxtSeq.replaceAll("seq","in");
                Path path2 = Paths.get(tokenTxtIn);
                try(BufferedWriter writer = Files.newBufferedWriter(path2, StandardCharsets.UTF_8)) {
                    writer.write(resultOfSeqOrder);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    /**
     * 使用线程加快数据的生成速度
     * 目前只是简单的创建了三个线程对不同的文件进行写。。。。。
     * @param methodDeclarationList
     */
    public static void threadRun(List<MethodDeclaration> methodDeclarationList){
        int border1 = methodDeclarationList.size() / 3;
        int border2 = methodDeclarationList.size() / 3 * 2;
        List<MethodDeclaration> list1 = new ArrayList<>(methodDeclarationList.subList(0,border1));
        List<MethodDeclaration> list2 = new ArrayList<>(methodDeclarationList.subList(border1,border2));
        List<MethodDeclaration> list3 = new ArrayList<>(methodDeclarationList.subList(border2,methodDeclarationList.size()-1));
        final int len1 = list1.size();
        final int len2 = list2.size();
        final int len3 = list3.size();

        new Thread(()->{
            Path path1 = Paths.get("src/main/resources/result/result1/seq.txt");
            Path path2 = Paths.get("src/main/resources/result/result1/inorder.txt");
            Path path3 = Paths.get("src/main/resources/result/result1/truth.txt");

            try (BufferedWriter writer1 = Files.newBufferedWriter(path1, StandardCharsets.UTF_8, StandardOpenOption.CREATE);
                 BufferedWriter writer2 = Files.newBufferedWriter(path2, StandardCharsets.UTF_8, StandardOpenOption.CREATE);
                 BufferedWriter writer3 = Files.newBufferedWriter(path3, StandardCharsets.UTF_8, StandardOpenOption.CREATE);) {
                int count1 = 0;
                for (MethodDeclaration method:list1){
                    System.out.println("Thread1正在处理第" + (++count1) + "个方法体，已完成：" + count1 + "/" + len1);
                    String inorder = TokenVisitorInOrder.tokenOfInOrder(method);
                    String seqOrder = TokenVisitorSeqOrder.tokenOfSeqOrder(method);
                    try {
                        handleToken(inorder, seqOrder, method, writer1, writer2, writer3);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            flag1 = true;
        },"Thread1").start();

        new Thread(()->{
            Path path1 = Paths.get("src/main/resources/result/result2/seq.txt");
            Path path2 = Paths.get("src/main/resources/result/result2/inorder.txt");
            Path path3 = Paths.get("src/main/resources/result/result2/truth.txt");
            try (BufferedWriter writer1 = Files.newBufferedWriter(path1, StandardCharsets.UTF_8, StandardOpenOption.CREATE);
                 BufferedWriter writer2 = Files.newBufferedWriter(path2, StandardCharsets.UTF_8, StandardOpenOption.CREATE);
                 BufferedWriter writer3 = Files.newBufferedWriter(path3, StandardCharsets.UTF_8, StandardOpenOption.CREATE);) {
                int count1 = 0;
                for (MethodDeclaration method:list2){
                    System.out.println("Thread2正在处理第" + (++count1) + "个方法体，已完成：" + count1 + "/" + len2);
                    String inorder = TokenVisitorInOrder.tokenOfInOrder(method);
                    String seqOrder = TokenVisitorSeqOrder.tokenOfSeqOrder(method);
                    try {
                        handleToken(inorder, seqOrder, method, writer1, writer2, writer3);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            flag2 = true;
        },"Thread2").start();

        new Thread(()->{
            Path path1 = Paths.get("src/main/resources/result/result3/seq.txt");
            Path path2 = Paths.get("src/main/resources/result/result3/inorder.txt");
            Path path3 = Paths.get("src/main/resources/result/result3/truth.txt");
            try (BufferedWriter writer1 = Files.newBufferedWriter(path1, StandardCharsets.UTF_8, StandardOpenOption.CREATE);
                 BufferedWriter writer2 = Files.newBufferedWriter(path2, StandardCharsets.UTF_8, StandardOpenOption.CREATE);
                 BufferedWriter writer3 = Files.newBufferedWriter(path3, StandardCharsets.UTF_8, StandardOpenOption.CREATE);) {
                int count1 = 0;
                for (MethodDeclaration method:list2){
                    System.out.println("Thread3正在处理第" + (++count1) + "个方法体，已完成：" + count1 + "/" + len3);
                    String inorder = TokenVisitorInOrder.tokenOfInOrder(method);
                    String seqOrder = TokenVisitorSeqOrder.tokenOfSeqOrder(method);
                    try {
                        handleToken(inorder, seqOrder, method, writer1, writer2, writer3);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            flag3 = true;
        },"Thread3").start();


    }


}
