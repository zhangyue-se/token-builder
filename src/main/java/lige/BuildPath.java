package lige;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ZhangYue
 */
public class BuildPath {

    public static final String path = "src/main/java/lige/json/test.json";

    public static void main(String[] args) {
        List<List<Token>> sequenceList = getSequenceList(path);
        sequenceList.forEach(temp->{
            String preorderPath = getPreorderPath(temp);
            System.out.println(preorderPath);
        });
    }

    /**
     * 将json文件中的序列转化为用Token类表示的序列
     * @param path  json文件的路径
     * @return  List<List<Token>>，其中List<Token>代表一个序列
     */
    public static List<List<Token>> getSequenceList(String path){
        FileReader fr;
        BufferedReader bf;
        List<List<Token>> sequences = new ArrayList<>();
        try {
            fr = new FileReader(path);
            bf = new BufferedReader(fr);
            String str;
            while ((str = bf.readLine()) != null){
                System.out.println(str);
                List<Token> tokens = new ArrayList<>(); //存放一个token序列
                //匹配{}之间的内容，包括{},方便构建token对象
                String regex = "\\{([^}]*)\\}";
                Pattern pattern = Pattern.compile (regex);
                Matcher matcher = pattern.matcher (str);
                while (matcher.find()){
                    Token token = new Token();
                    JSONObject jsonObject = JSON.parseObject(matcher.group());
                    //如果children有内容，将它加入token
                    if (jsonObject.get("children")!=null){
                        JSONArray jsonArray = jsonObject.getJSONArray("children");
                        List<Integer> children = new ArrayList<>();
                        for (int i = 0; i<jsonArray.size(); i++){
                            children.add((Integer) jsonArray.get(i));
                        }
                        token.setChildren(children);
                    }
                    //如果value有内容，将它加入token
                    if (jsonObject.get("value") != null){
                        JSONArray jsonArray = jsonObject.getJSONArray("value");
                        List<String> values = new ArrayList<>();
                        for (int i = 0; i<jsonArray.size(); i++){
                            values.add((String) jsonArray.get(i));
                        }
                        token.setValue(values);
                    }
                    //将类型加入token
                    token.setType(jsonObject.getString("type"));
                    tokens.add(token);
                }
                sequences.add(tokens);
            }
            fr.close();
            bf.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sequences;
    }

    /**
     * 获得层次结构的路径
     * @param sequence
     * @return
     */
    public String getHierarchicalPath(List<Token> sequence){
        return null;
    }


    /**
     * 获取序列先序遍历结果
     */
    public static String getPreorderPath(List<Token> sequence){
        List<String> result = new ArrayList<>();
        //第一个token从0开始
        dfs(sequence,result,0);
        StringBuilder stringBuilder = new StringBuilder();
        for (String ss : result){
            stringBuilder.append(ss);
            stringBuilder.append(",");
        }
        return stringBuilder.substring(0,stringBuilder.length()-1);
    }

    /**
     * dfs实现先序遍历
     * @param child 当前token的孩子坐标
     */
    public static void dfs(List<Token> sequence,List<String> result,Integer child){

        if (!sequence.get(child).isChildrenEmpty()){
            result.add(sequence.get(child).getType());
            for (Integer integer:sequence.get(child).getChildren()){
                dfs(sequence,result,integer);
            }
        }else {
            for (String name:sequence.get(child).getValue()){
                result.add(sequence.get(child).getType());
                result.add(name);
            }
        }
    }


}
