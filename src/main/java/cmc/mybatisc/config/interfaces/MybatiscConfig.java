package cmc.mybatisc.config.interfaces;

import cmc.mybatisc.core.util.AliasOperation;

import java.util.Collection;
import java.util.Collections;

/**
 * mybatisc配置
 *
 * @author cmc
 * &#064;date  2024/01/03
 */
public interface MybatiscConfig {
    /**
     * 获取del标志列表
     *
     * @return {@link Collection}<{@link DelFlag}>
     */
    default Collection<DelFlag> getDelFlags(){
        // 默认返回空
        return Default.delFlags;
    }

    /**
     * 获取名称转换 默认是转换为下划线
     *
     * @return {@link NameConversion}
     */
    default NameConversion getNameConversion(){
        return Default.nameConversion;
    }


    /**
     * 获取别名
     *
     * @return {@link AliasOperation}
     */
    default AliasOperation getAlias(){
        return Default.ALIAS_OPERATION;
    }

    /**
     * 默认配置
     *
     * @author cmc
     * &#064;date  2024/01/04
     */
    class Default{
        /**
         * 删除标志
         */
        public final static Collection<DelFlag> delFlags = Collections.emptyList();
        /**
         * 名称转换
         */
        public final static NameConversion nameConversion = new NameConversion() {};

        /**
         * 别名
         */
        public final static AliasOperation ALIAS_OPERATION = new AliasOperation();
    }
}
