package cmc.mybatisc.config.interfaces;

import cmc.mybatisc.parser.EntityParser;
import cmc.mybatisc.parser.MapperParser;

import java.io.Serializable;
import java.util.Map;
import java.util.function.Function;

public class DefaultDelFlag implements DelFlag{
    @Override
    public String getFieldName() {
        return null;
    }

    @Override
    public Serializable getDelValue() {
        return null;
    }

    @Override
    public Serializable getUnDelValue() {
        return null;
    }

    /**
     * 生成查询sql
     *
     * @param dy           dy
     * @param entityParser 实体解析器
     * @param prefix       前缀
     * @param suffix       后缀
     * @return {@link String}
     */
    @Override
    public String generateSelectSql(Map<String, Function<?,Serializable>> dy, EntityParser entityParser, String prefix, String suffix) {
        return "";
    }

    /**
     * 生成删除sql(默认真删除)
     *
     * @param entityParser 映射器解析器
     * @param suffix       后缀
     * @return {@link String}
     */
    @Override
    public String generateDeleteSql(Map<String, Function<?,Serializable>> dy, EntityParser entityParser, String suffix) {
        return "delete from "+entityParser.getTableName() + " " + suffix;
    }
}
