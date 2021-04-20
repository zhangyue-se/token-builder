package run;

/**
 * 每个token信息
 * ZhangYue
 */
public class TokenInfo {

    private String type;
    private String token;
    private Integer line;   //所在行
    private Integer column; //所在列

    public TokenInfo(String type, String token, Integer line, Integer column) {
        this.type = type;
        this.token = token;
        this.line = line;
        this.column = column;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Integer getLine() {
        return line;
    }

    public void setLine(Integer line) {
        this.line = line;
    }

    public Integer getColumn() {
        return column;
    }

    public void setColumn(Integer column) {
        this.column = column;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this){
            return true;
        }
        if (!(obj instanceof TokenInfo)){
            return false;
        }
        TokenInfo tokenInfo = (TokenInfo)obj;
        if (this.line.equals(tokenInfo.line) && this.column.equals(tokenInfo.column)){
            return true;
        }else {
            return false;
        }
    }

    @Override
    public String toString() {
        return "TokenInfo{" +
                "type='" + type + '\'' +
                ", token='" + token + '\'' +
                ", line=" + line +
                ", column=" + column +
                '}';
    }
    //    @Override
//    public String toString() {
//        return "< " + this.type + "," + this.token + ">";
//    }
}
