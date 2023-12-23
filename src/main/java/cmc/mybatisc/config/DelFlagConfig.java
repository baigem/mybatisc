package cmc.mybatisc.config;

import cmc.mybatisc.config.interfaces.DefaultDelFlag;
import cmc.mybatisc.config.interfaces.DelFlag;
import cmc.mybatisc.parser.EntityParser;
import cmc.mybatisc.parser.MapperParser;
import cn.hutool.extra.spring.SpringUtil;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * del标志配置
 *
 * @author 程梦城
 * @version 1.0.0
 * &#064;date  2023/12/24
 */
@Component
public class DelFlagConfig {
    /**
     * 旗帜
     */
    private final Map<String, DelFlag> flags = new HashMap<>();
    /**
     * 默认del标志
     */
    private final DelFlag defaultDelFlag = new DefaultDelFlag();

    public DelFlagConfig() {
        // 初始化
        for (DelFlag value : SpringUtil.getBeansOfType(DelFlag.class).values()) {
            String fieldName = value.getFieldName();
            if(flags.containsKey(fieldName)){
                throw new RuntimeException("同一项目不允许出现重复逻辑删除字段，请检查实现了DelFlag的实现类是否重复");
            }
            flags.put(fieldName, value);
        }
    }

    /**
     * 获取del标志
     *
     * @param entityParser 映射器解析器
     * @return {@link DelFlag}
     */
    public DelFlag getDelFlag(EntityParser entityParser){
        for (String s : entityParser.getFieldList()) {
            DelFlag delFlag = flags.get(s);
            if(delFlag != null && delFlag.isAccuracy(entityParser)){
                return delFlag;
            }
        }
        return defaultDelFlag;
    }

    /**
     * 生成查询sql
     *
     * @param entityParser 映射器解析器
     * @param prefix       前缀
     * @param suffix       后缀
     * @return {@link String}
     */
    public String generateSelectSql(Map<String, Function<?,Serializable>> dy, EntityParser entityParser, String prefix, String suffix){
        return this.getDelFlag(entityParser).generateSelectSql(dy,entityParser,prefix,suffix);
    }

    /**
     * 生成删除sql
     *
     * @param entityParser 映射器解析器
     * @param suffix       后缀
     * @return {@link String}
     */
    public String generateDeleteSql(Map<String, Function<?,Serializable>> dy, EntityParser entityParser,String suffix){
        return this.getDelFlag(entityParser).generateDeleteSql(dy,entityParser,suffix);
    }
}
