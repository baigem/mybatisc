package cmc.mybatisc.config.interfaces;

import cmc.mybatisc.parser.EntityParser;
import cmc.mybatisc.parser.MapperParser;
import com.alibaba.fastjson2.JSON;

import java.io.Serializable;
import java.util.Map;
import java.util.function.Function;

/**
 * 删除标志
 *
 * @author 程梦城
 * @version 1.0.0
 * &#064;date  2023/12/24
 */
public interface DelFlag{
    /**
     * 匹配字段名
     */
    String getFieldName();

    /**
     * 是准确性
     * 是准确性 如果为false将不匹配此逻辑删除
     *
     * @param ignore 映射器解析器
     * @return boolean
     */
    default boolean isAccuracy(EntityParser ignore){
        return true;
    }

    /**
     * 具有动态del值
     *
     * @return boolean
     */
    default boolean hasDynamicDelValue(){
        return false;
    }

    /**
     * 具有动态未 del值
     *
     * @return boolean
     */
    default boolean hasDynamicUnDelValue(){
        return false;
    }

    /**
     * 获取del值
     *
     * @return {@link Serializable}
     */
    Serializable getDelValue();

    /**
     * 获取未删除值
     *
     * @return {@link Serializable}
     */
    Serializable getUnDelValue();

    /**
     * 生成查询sql
     *
     * @param mapperParser 映射器解析器
     * @param prefix       前缀
     * @return {@link String}
     */
    default String generateSelectSql(Map<String, Function<?,Serializable>> dy,EntityParser entityParser, String prefix, String suffix){
        String alise = entityParser.getTableAlias();
        String sql;
        if(this.hasDynamicUnDelValue()){
            sql = alise + "." + this.getFieldName() + " = #{" + this.getOnlyKey() + "} " + suffix;
            dy.put(this.getOnlyKey(),ignore->this.getUnDelValue());
        }else{
            Serializable unDelValue = this.getUnDelValue();
            if(unDelValue == null){
                sql = alise + "." + this.getFieldName() + " is null " + suffix;
            }else {
                sql = alise + "." + this.getFieldName() + " = " + unDelValue + " " + suffix;
            }
        }

        return prefix+" "+sql;
    }

    default String generateDeleteSql(Map<String, Function<?,Serializable>> dy, EntityParser entityParser, String suffix){
        String tableName = entityParser.getTableName();
        String alise = entityParser.getTableAlias();
        if(this.hasDynamicDelValue()){
            dy.put(this.getOnlyKey(),ignore->this.getDelValue());
            return "update " + tableName +" as " + alise + " set " + alise + "." + getFieldName() + " = #{"+ this.getOnlyKey() +"} " + suffix;
        }else{
            return "update " + tableName +" as " + alise + " set " + alise + "." + getFieldName() + " = " + JSON.toJSONString(getDelValue()) + " " + suffix;
        }
    }


    default String getOnlyKey(){
        return this.getFieldName()+"_value";
    }
}
