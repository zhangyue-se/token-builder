package lige;

import java.util.ArrayList;
import java.util.List;

/**
 * ZhangYue
 */
public class Token {

    private List<Integer> children;
    private List<String> value;
    private String type;

    public Token() {
        children = new ArrayList<>();
        value = new ArrayList<>();
    }

    public Token(List<Integer> children, List<String> value, String type) {
        this.children = children;
        this.value = value;
        this.type = type;
    }

    public List<Integer> getChildren() {
        return children;
    }

    public void setChildren(List<Integer> children) {
        this.children = children;
    }

    public List<String> getValue() {
        return value;
    }

    public void setValue(List<String> value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isChildrenEmpty(){
        if (this.children == null || this.children.size() == 0){
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "Token{" +
                "children=" + children +
                ", value=" + value +
                ", type='" + type + '\'' +
                '}';
    }
}
