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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
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

    public static void main(String[] args) throws IOException {
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
        int count2 = 0;
        int all = methodDeclarationList.size();
        for (MethodDeclaration method:methodDeclarationList){
            System.out.println("正在处理第" + (++count2) + "个方法体，已完成：" + count2 + "/" + all);
            String inorder = TokenVisitorInOrder.tokenOfInOrder(method);
            String seqOrder = TokenVisitorSeqOrder.tokenOfSeqOrder(method);
            handleToken(inorder, seqOrder, method);
        }
        System.out.println("生成训练数据结束.......");
    }

    /**
     * 处理序列生成特定的数据格式
     * @param inorder
     * @param seqOrder
     * @throws IOException
     */
    public static void handleToken(String inorder, String seqOrder, MethodDeclaration method) throws IOException {
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
                Path path1 = Paths.get(PathConfig.seqPath);
                Path path2 = Paths.get(PathConfig.inorderPath);
                Path path3 = Paths.get(PathConfig.truthPath);
                try(BufferedWriter writer1 = Files.newBufferedWriter(path1, StandardCharsets.UTF_8, StandardOpenOption.APPEND);
                    BufferedWriter writer2 = Files.newBufferedWriter(path2, StandardCharsets.UTF_8, StandardOpenOption.APPEND);
                    BufferedWriter writer3 = Files.newBufferedWriter(path3, StandardCharsets.UTF_8, StandardOpenOption.APPEND)) {

                    if (stringBuilder2.toString().contains("null")||vocabularyOfToken.get(tokenListOfSeqOrder.get(i+1).getToken())==null||
                            vocabularyOfType.get(tokenListOfSeqOrder.get(i+1).getType())==null){
                        continue;
                    }
                    writer1.write(stringBuilder1.toString() + "\n");
                    writer2.write(stringBuilder2.toString() + "\n");
                    //写入正确结果
                    writer3.write(vocabularyOfType.get(tokenListOfSeqOrder.get(i+1).getType()) + "," + vocabularyOfToken.get(tokenListOfSeqOrder.get(i+1).getToken()) + "\n");

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }else {
            System.out.println("该方法token个数不一致！！,目前一共有" + (++count1) + "个方法有问题");
            try(BufferedWriter writer = Files.newBufferedWriter(Paths.get(PathConfig.errorMethodPath), StandardCharsets.UTF_8, StandardOpenOption.APPEND)) {
                writer.write(method.toString() + "\n" + "\n");
            }catch (IOException e){
                e.printStackTrace();
            }
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


}
