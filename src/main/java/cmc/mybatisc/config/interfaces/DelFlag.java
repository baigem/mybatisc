package cmc.mybatisc.config.interfaces;

import cmc.mybatisc.core.util.TableStructure;
import cmc.mybatisc.utils.string.StringTools;
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
    default boolean isAccuracy(TableStructure ignore){
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
     * @param prefix       前缀
     * @param dy           动态数据
     * @param tableStructure 实体解析器
     * @param suffix        后缀
     * @return {@link String}
     */
    default String generateSelectSql(Map<String, Function<?,Serializable>> dy,TableStructure tableStructure, String prefix, String suffix){
        String alise = tableStructure.getAlias();
        String sql;
        if(this.hasDynamicUnDelValue()){
            sql = StringTools.concat(alise,".",this.getFieldName()," = #{",this.getOnlyKey(),"} ", suffix);
            dy.put(this.getOnlyKey(),ignore->this.getUnDelValue());
        }else{
            Serializable unDelValue = this.getUnDelValue();
            if(unDelValue == null){
                sql = StringTools.concat(alise,".",this.getFieldName()," is null ", suffix);
            }else {
                sql = StringTools.concat(alise,".",this.getFieldName()," = ",unDelValue.toString()," ",suffix);
            }
        }
        return prefix+" "+sql;
    }

    /**
     * 生成delete sql
     *
     * @param dy           dy
     * @param entityParser 实体解析器
     * @param suffix       后缀
     * @return {@link String}
     */
    default String generateDeleteSql(Map<String, Function<?,Serializable>> dy, TableStructure entityParser, String suffix){
        String tableName = entityParser.getName();
        String alise = entityParser.getAlias();
        if(this.hasDynamicDelValue()){
            dy.put(this.getOnlyKey(),ignore->this.getDelValue());
            return StringTools.concat("update ",tableName," as " ,alise ," set ",alise,".",getFieldName()," = #{",this.getOnlyKey(),"} ",suffix);
        }else{
            return StringTools.concat("update ",tableName," as ",alise," set ",alise,".",getFieldName()," = ",JSON.toJSONString(getDelValue())," ",suffix);
        }
    }


    /**
     * 获取唯一秘钥
     *
     * @return {@link String}
     */
    default String getOnlyKey(){
        return this.getFieldName()+"_value";
    }
}
