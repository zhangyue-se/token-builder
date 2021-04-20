package util;

import run.TokenInfo;

import java.util.List;

/**
 * ZhangYue
 */
public class Utils {

    public static boolean isEqualOfTwoList(List<TokenInfo> tokenListOfInorder,List<TokenInfo> tokenListOfSeq){
        for (TokenInfo tokenInfo:tokenListOfInorder){
            if (!tokenListOfSeq.contains(tokenInfo)){
                return false;
            }
        }
        for (TokenInfo tokenInfo:tokenListOfSeq){
            if (!tokenListOfInorder.contains(tokenInfo)){
                return false;
            }
        }
        return true;
    }

}
