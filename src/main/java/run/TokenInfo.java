package run;

import lombok.*;

/**
 * 每个token信息
 * ZhangYue
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class TokenInfo {

    private String type;
    private String token;
    private Integer line;   //所在行
    private Integer column; //所在列

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
}
