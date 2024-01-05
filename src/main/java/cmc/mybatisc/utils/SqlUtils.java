package cmc.mybatisc.utils;

import cmc.mybatisc.annotation.Search;
import cmc.mybatisc.base.model.CharacterString;
import cmc.mybatisc.base.model.StringList;
import cmc.mybatisc.model.QueryFieldCriteria;
import cmc.mybatisc.parser.SearchParser;
import cmc.mybatisc.utils.map.MapUtil;
import cmc.mybatisc.utils.string.StringTools;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * sql实用程序
 *
 * @author 程梦城
 * @version 1.0.0
 * &#064;date  2023/12/09
 */
public class SqlUtils {

    public static String generateSql(List<QueryFieldCriteria> list) {
        StringBuilder sql = new StringBuilder();
        // 进行分组
        Map<String, List<QueryFieldCriteria>> group = list.stream().collect(Collectors.groupingBy(QueryFieldCriteria::getGrouping));
        list = group.get("");
        group.remove("");
        list.forEach(info -> sql.append(generateSql(info)));
        // 生成需要分组判断的
        group.forEach((k, v) -> {
            sql.append("\n<trim prefix=\"and (\"  suffix=\")\" prefixOverrides=\"AND |OR \">");
            v.forEach(info -> sql.append(generateSql(info)));
            sql.append("</trim>");
        });
        return sql.toString();
    }

    @Search
    public static String generateSql(QueryFieldCriteria info) {
        // 判断是否是基础数据类型
        if (info.getFieldClass().isEnum() || CharacterString.class.isAssignableFrom(info.getFieldClass()) || info.getFieldClass().getName().contains("java.") || !info.isCustomEntity()) {
            if (info.getFieldClass() == String.class || CharacterString.class.isAssignableFrom(info.getFieldClass())) {
                // 字符串查询
                return generateStringIfSql(info);
            } else if (info.getFieldClass() == Date.class) {
                // 时间查询
                return generateDateSql(info);
            } else if (info.getFieldClass() == List.class) {
                // 列表查询
                return generateForSql(info);
            }  else if (info.getFieldClass() == StringList.class) {
                // 字符串列表查询
                return generateStringForSql(info);
            } else {
                // 数字查询
                return generateNumberIfSql(info);
            }
        } else {
            // 用户自定义的结构体，重新构造List<FieldMapp>
            List<QueryFieldCriteria> list = QueryFieldCriteria.buildBySearch(SearchParser.parse(info));
            if (list.isEmpty()) {
                return "";
            }
            return String.format(" <if test=\"%s != null\">\n", info.getFullName()) + generateSql(list) + "</if>\n";
        }
    }


    /**
     * 生成排序sql
     *
     * @param list 列表
     * @return {@link String}
     */
    public static String generateSortSql(List<QueryFieldCriteria> list) {
        if (list.isEmpty()) {
            return "";
        }
        StringBuilder sql = new StringBuilder();
        sql.append("ORDER BY ");
        for (QueryFieldCriteria queryFieldCriteria : list) {
            sql.append(StringTools.template("${alias}.${fieldName} ${sortRule}", queryFieldCriteria)).append(", ");
        }
        return sql.toString().replaceAll(", $", "");
    }

    /**
     * 生成组sql
     *
     * @param groupList 组列表
     * @return {@link String}
     */
    public static String generateGroupSql(List<QueryFieldCriteria> groupList) {
        if (groupList.isEmpty()) {
            return "";
        }
        StringBuilder sql = new StringBuilder();
        sql.append("GROUP BY ");
        for (QueryFieldCriteria queryFieldCriteria : groupList) {
            sql.append(StringTools.template("${alias}.${fieldName} ${sortRule}", queryFieldCriteria)).append(", ");
        }
        return sql.toString().replaceAll(", $", "");
    }

    /**
     * 如果sql生成数字
     *
     * @param queryFieldCriteria 场映射
     * @return {@link String}
     */
    public static String generateNumberIfSql(QueryFieldCriteria queryFieldCriteria) {
        StringBuilder sql = new StringBuilder();
        sql.append(StringTools.template("<if test=\"${fullName} != null\">\n", queryFieldCriteria));
        sql.append("                ").append(queryFieldCriteria.isAnd() ? "and" : "or");
        if (queryFieldCriteria.isLike()) {
            sql.append(StringTools.template(" ${alias}.${fieldName} like concat('%',#{${fullName}},'%')\n", queryFieldCriteria));
        } else if (queryFieldCriteria.isRange()) {
            sql.append(StringTools.template(" ${alias}.${fieldName} between #{${fullName}} and #{${endName}}\n", queryFieldCriteria));
        } else {
            sql.append(StringTools.template(" ${alias}.${fieldName} ${compare} #{${fullName}}\n", queryFieldCriteria));
        }
        sql.append("            </if>");
        return sql.toString();
    }

    /**
     * @param queryFieldCriteria 场映射
     * @return {@link String}
     */
    public static String generateStringIfSql(QueryFieldCriteria queryFieldCriteria) {
        StringBuilder sql = new StringBuilder();
        sql.append(StringTools.template("<if test=\"${fullName} != null and ${fullName} != ''\">\n", queryFieldCriteria));
        sql.append("                ").append(queryFieldCriteria.isAnd() ? "and" : "or");
        if (queryFieldCriteria.isLike()) {
            sql.append(StringTools.template(" ${alias}.${fieldName} like concat('%',#{${fullName}},'%')\n", queryFieldCriteria));
        } else if (queryFieldCriteria.isRange()) {
            sql.append(StringTools.template(" ${alias}.${fieldName} between #{${fullName}} and #{${endName}}\n", queryFieldCriteria));
        } else {
            sql.append(StringTools.template(" ${alias}.${fieldName} ${compare} #{${fullName}}\n", queryFieldCriteria));
        }
        sql.append("            </if>");
        return sql.toString();
    }


    /**
     * 为sql生成for循环的sql
     *
     * @param queryFieldCriteria 场映射
     * @return {@link String}
     */
    public static String generateForSql(QueryFieldCriteria queryFieldCriteria) {
        String and = queryFieldCriteria.isAnd() ? "and" : "or";
        Map<String, Object> map = MapUtil.toMap(queryFieldCriteria);
        map.put("andStr", and);
        return StringTools.template("<if test=\"${fullName} != null and ${fullName}.size() > 0\">\n" +
                "                <foreach collection=\"${fullName}\" index=\"index\" item=\"item\"\n" +
                "                         open=\"${andStr} ${alias}.${fieldName} in (\"\n" +
                "                         separator=\",\" close=\")\">\n" +
                "                    #{item}\n" +
                "                </foreach>\n" +
                "            </if>", map);
    }

    public static String generateStringForSql(QueryFieldCriteria queryFieldCriteria) {
        String and = queryFieldCriteria.isAnd() ? "and" : "or";
        Map<String, Object> map = MapUtil.toMap(queryFieldCriteria);
        map.put("andStr", and);
        return StringTools.template("<if test=\"${fullName} != null and ${fullName}.size() > 0\">\n" +
                "                <foreach collection=\"${fullName}\" index=\"index\" item=\"item\"\n" +
                "                         open=\"${andStr} \"\n" +
                "                         separator=\"${andStr}\">\n" +
                "                    ${alias}.${fieldName} like concat('%',#{item},#{${fullName}.getDelimiter()},'%')\n" +
                "                </foreach>\n" +
                "            </if>", map);
    }


    public static String generateDateSql(QueryFieldCriteria queryFieldCriteria) {
        // 判断是否开启范围查询
        if (!queryFieldCriteria.isRange()) {
            return generateNumberIfSql(queryFieldCriteria);
        }
        String and = queryFieldCriteria.isAnd() ? "and" : "or";
        Map<String, Object> map = MapUtil.toMap(queryFieldCriteria);
        map.put("andStr", and);
        return StringTools.template("<choose>\n" +
                "                <!-- 范围查询 -->\n" +
                "                <when test=\"${endName} != null and ${fullName} != null\">\n" +
                "                    ${andStr} to_days(${alias}.${fieldName}) between to_days(#{${fullName}}) and to_days(#{${endName}})\n" +
                "                </when>\n" +
                "                <!-- 维度查询 -->\n" +
                "                <when test=\"${modeName} != null and ${modeName} != ''\">\n" +
                "                    <choose>\n" +
                "                        <!-- 天查询 -->\n" +
                "                        <when test=\"${modeName} == 'day'.toString()\">\n" +
                "                            ${andStr} to_days(${alias}.${fieldName}) = to_days(#{${fullName}})\n" +
                "                        </when>\n" +
                "                        <!-- 周查询 -->\n" +
                "                        <when test=\"${modeName} == 'week'.toString()\">\n" +
                "                            ${andStr} YEARWEEK(${alias}.${fieldName}) = YEARWEEK(#{${fullName}})\n" +
                "                        </when>\n" +
                "                        <!-- 月查询 -->\n" +
                "                        <when test=\"${modeName} == 'month'.toString()\">\n" +
                "                            ${andStr} DATE_FORMAT(${alias}.${fieldName},'%Y%m') = DATE_FORMAT(#{${fullName}},'%Y%m')\n" +
                "                        </when>\n" +
                "                        <!-- 年查询 -->\n" +
                "                        <when test=\"${modeName} == 'year'.toString()\">\n" +
                "                            ${andStr} DATE_FORMAT(${alias}.${fieldName},'%Y') = DATE_FORMAT(#{${fullName}},'%Y')\n" +
                "                        </when>\n" +
                "                        <when test=\"${modeName} == 'not'.toString()\">\n" +
                "                            ${andStr} ${alias}.${fieldName} not between #{${fullName}} and #{${endName}}\n" +
                "                        </when>\n" +
                "                        <!-- 年、月、日、时、分、秒查询 -->\n" +
                "                        <otherwise>\n" +
                "                            ${andStr} DATE_FORMAT(${alias}.${fieldName},#{${modeName}}) =\n" +
                "                            DATE_FORMAT(#{${fullName}},#{${modeName}})\n" +
                "                        </otherwise>\n" +
                "                    </choose>\n" +
                "                </when>\n" +
                "                <!-- 精确查询 -->\n" +
                "                <when test=\"${fullName} != null\">\n" +
                "                    ${andStr} ${alias}.${fieldName} = #{${fullName}}\n" +
                "                </when>\n" +
                "            </choose>", map);
    }

    /**
     * 包裹字段
     */
    public static String packageField(String str) {
        String[] split = str.split("\\.");
        for (int i = 0; i < split.length; i++) {
            if (!(split[i].startsWith("`") && split[i].endsWith("`"))) {
                split[i] = "`" + str + "`";
            }
        }
        return String.join(".",str);
    }

}
