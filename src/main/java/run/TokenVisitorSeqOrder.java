package run;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.printer.*;
import javaparser.PrettyPrinter;
import lombok.SneakyThrows;
import org.junit.Test;

import java.io.*;

/**
 * Created by zhangyue
 */
public class TokenVisitorSeqOrder {

    private static String codePath = "/Users/zhangyue/Desktop/test1.java";

    /**
     * 根据源码顺序打印
     * @throws FileNotFoundException
     */
    public static String tokenOfSeqOrder(MethodDeclaration method) {
        javaparser.PrettyPrinter printer = new PrettyPrinter();
        String res = printer.print(method);
        return res;
    }

    //++++++++++++++++++++++++++++++++++++++++++++++
    //以下都是测试的方法
    /**
     * 根据源码顺序打印，type-token
     * @throws FileNotFoundException
     */
    @Test
    public void test_my() throws FileNotFoundException {
        CompilationUnit cu = StaticJavaParser.parse(new File(codePath));
        javaparser.PrettyPrinter printer = new PrettyPrinter();
        for (Node node:cu.getChildNodes()){
            if (node instanceof ClassOrInterfaceDeclaration){
                ((ClassOrInterfaceDeclaration) node).getMembers().forEach((member)->{
                    if (member instanceof MethodDeclaration){
//                        System.out.println(member.toString());
                        String res = printer.print(member);
                        System.out.println(res);
                    }
                });
            }
        }
    }

    /**
     * 把代码生成dot文件
     * @throws IOException
     */
    @Test
    public void toDot() throws IOException {
        new VoidVisitorAdapter<Void>(){
            @SneakyThrows
            @Override
            public void visit(MethodDeclaration n, Void arg) {

                DotPrinter dotPrinter = new DotPrinter(true);
                String filePath = "/Users/zhangyue/IdeaProjects/juc/src/main/resources/test.dot";
                BufferedWriter bw = new BufferedWriter(new FileWriter(filePath));
                bw.flush();
                bw.write(dotPrinter.output(n));
                bw.close();
                Runtime rt = Runtime.getRuntime();
                rt.exec("dot -Tpng " + filePath + " -o " + "/Users/zhangyue/IdeaProjects/juc/src/main/resources/test" + ".png");
                super.visit(n, arg);
            }
        }.visit(StaticJavaParser.parse(new File(codePath)),null);
    }

    /**
     * 打印节点位置Position
     * @throws FileNotFoundException
     */
    @Test
    public void testPosition() throws FileNotFoundException {
        new VoidVisitorAdapter<Void>(){
            @Override
            public void visit(SimpleName n, Void arg) {
                super.visit(n, arg);
                System.out.println(n.asString());
                System.out.println(n.getBegin().get().line);
                System.out.println(n.getBegin().get().column);
                System.out.println(n.getEnd().get().line);
                System.out.println(n.getEnd().get().column);
                System.out.println();
            }
        }.visit(StaticJavaParser.parse(new File(codePath)),null);
    }

}
