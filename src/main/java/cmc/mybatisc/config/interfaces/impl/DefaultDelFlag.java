package cmc.mybatisc.config.interfaces.impl;

import cmc.mybatisc.config.interfaces.DelFlag;
import cmc.mybatisc.core.util.TableStructure;

import java.io.Serializable;
import java.util.Map;
import java.util.function.Function;

public class DefaultDelFlag implements DelFlag {
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
     * @param tableStructure 实体解析器
     * @param prefix       前缀
     * @param suffix       后缀
     * @return {@link String}
     */
    @Override
    public String generateSelectSql(Map<String, Function<?,Serializable>> dy, TableStructure tableStructure, String prefix, String suffix) {
        return "";
    }

    /**
     * 生成删除sql(默认真删除)
     *
     * @param tableStructure 映射器解析器
     * @param suffix       后缀
     * @return {@link String}
     */
    @Override
    public String generateDeleteSql(Map<String, Function<?,Serializable>> dy, TableStructure tableStructure, String suffix) {
        return "delete from "+tableStructure.getName() + " " + suffix;
    }
}
