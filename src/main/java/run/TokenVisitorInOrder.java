package run;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BreakStmt;
import com.github.javaparser.ast.stmt.ContinueStmt;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.visitor.TreeVisitor;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * ZhangYue
 */
public class TokenVisitorInOrder extends TreeVisitor {
    StringBuilder result = new StringBuilder();
    @Override
    public void process(Node node) {
        String[] temp = node.getClass().getName().split("\\.");
        String className = temp[temp.length-1];
        int line = node.getBegin().get().line;
        int column = node.getBegin().get().column;
        if(node instanceof SimpleName){
            result.append(className).append("," + ((SimpleName) node).getIdentifier()).append("," + line).append("," + column).append("\r\n");
        }else if(node instanceof Name){
            result.append(className).append("," + ((Name) node).getIdentifier()).append("," + line).append("," + column).append("\r\n");
        }else if(node instanceof Modifier){
            result.append(className).append("," + ((Modifier) node).getKeyword().asString().toLowerCase()).append("," + line).append("," + column).append("\r\n");
        }else if (node instanceof PrimitiveType){
            result.append(className).append("," + ((PrimitiveType) node).getType().asString().toLowerCase()).append("," + line).append("," + column).append("\r\n");
        } else if (node instanceof CharLiteralExpr){
            result.append(className).append("," + "CharConstant").append("," + line).append("," + column).append("\r\n");
        }else if (node instanceof DoubleLiteralExpr){
            result.append(className).append("," + "DoubleNumberNumber").append("," + line).append("," + column).append("\r\n");
        }else if (node instanceof IntegerLiteralExpr){
            result.append(className).append("," + "IntegerNumber").append("," + line).append("," + column).append("\r\n");
        }else if (node instanceof LongLiteralExpr){
            result.append(className).append("," + "LongNumber").append("," + line).append("," + column).append("\r\n");
        }else if (node instanceof StringLiteralExpr){
            result.append(className).append("," + "StringConstant").append("," + line).append("," + column).append("\r\n");
        }else if (node instanceof TextBlockLiteralExpr){
            result.append(className).append("," + "TextBlock").append("," + line).append("," + column).append("\r\n");
        }else if (node instanceof BooleanLiteralExpr){
            result.append(className).append("," + "BooleanConstant").append("," + line).append("," + column).append("\r\n");
        } else if (node instanceof BreakStmt){
            result.append(className).append("," + "break").append("," + line).append("," + column).append("\r\n");
        } else if (node instanceof ContinueStmt){
            result.append(className).append("," + "continue").append("," + line).append("," + column).append("\r\n");
        } else {
            result.append(className).append(",null").append("," + line).append("," + column).append("\r\n");
        }
    }


    public static String tokenOfInOrder(MethodDeclaration method){
        TokenVisitorInOrder myTreeVisitor = new TokenVisitorInOrder();
        myTreeVisitor.visitBreadthFirst(method);
        return myTreeVisitor.result.toString();
    }

    @Test
    public void tokenOfInOrder1() throws FileNotFoundException {
        CompilationUnit cu = StaticJavaParser.parse(new File("/Users/zhangyue/Desktop/test1.java"));
        for (Node node:cu.getChildNodes()){
            if (node instanceof ClassOrInterfaceDeclaration){
                ((ClassOrInterfaceDeclaration) node).getMembers().forEach((member)->{
                    if (member instanceof MethodDeclaration){
                        TokenVisitorInOrder myTreeVisitor = new TokenVisitorInOrder();
                        myTreeVisitor.visitBreadthFirst(member);
                        System.out.println(myTreeVisitor.result.toString());
                    }
                });
            }
        }
    }
}
