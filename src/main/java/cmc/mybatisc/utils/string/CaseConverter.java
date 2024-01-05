package cmc.mybatisc.utils.string;

/**
 * 命名风格转换器
 *
 * @author cmc
 * &#064;date  2024/01/04
 */
public class CaseConverter {

    /**
     * 转换为小驼峰
     *
     * @param str str
     * @return {@link String}
     */
    public static String convertToSmallHump(String str){
        StringBuilder lowerCase = new StringBuilder(str.substring(0, 1).toLowerCase());
        char[] charArray = str.substring(1).toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            if (charArray[i] == '_') {
                lowerCase.append(String.valueOf(charArray[++i]).toUpperCase());
            } else {
                lowerCase.append(charArray[i]);
            }
        }
        return lowerCase.toString();
    }

    /**
     * 转换为大驼峰
     *
     * @param str str
     * @return {@link String}
     */
    public static String convertToGreatHump(String str){
        str = convertToSmallHump(str);
        return String.valueOf(str.charAt(0)).toUpperCase() + str.substring(1);
    }

    /**
     * 转换为下划线
     *
     * @return {@link String}
     */
    public static String convertToUnderline(String str){
        StringBuilder lowerCase = new StringBuilder(str.substring(0, 1).toLowerCase());
        char[] charArray = str.substring(1).toCharArray();
        for (char c : charArray) {
            // 判断大写字母
            if ((c >= 65 && c <= 90) || (c >= 48 && c <= 57)) {
                lowerCase.append("_").append(String.valueOf(c).toLowerCase());
            } else {
                lowerCase.append(c);
            }
        }
        return lowerCase.toString();
    }
}
