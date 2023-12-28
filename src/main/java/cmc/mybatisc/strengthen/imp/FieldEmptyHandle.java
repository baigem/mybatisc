package cmc.mybatisc.strengthen.imp;

import cmc.mybatisc.annotation.FieldEmpty;
import cmc.mybatisc.base.CodeStandardEnum;
import cmc.mybatisc.config.DelFlagConfig;
import cmc.mybatisc.config.MybatisScannerConfigurer;
import cmc.mybatisc.config.interfaces.DelFlag;
import cmc.mybatisc.model.ParamAnnotation;
import cmc.mybatisc.parser.SqlParser;
import cmc.mybatisc.strengthen.BaseStrengthen;
import cmc.mybatisc.utils.MapperStrongUtils;
import cn.hutool.extra.spring.SpringUtil;
import org.apache.ibatis.session.SqlSession;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 字段清空句柄
 *
 * @author 程梦城
 * @version 1.0.0
 * &#064;date  2023/07/07
 */
public class FieldEmptyHandle extends BaseStrengthen {
    public FieldEmptyHandle(SqlSession sqlSession, Class<?> mapper) {
        super(sqlSession, mapper);
    }

    /**
     * 创建缓存
     *
     * @param method 方法
     * @return {@link Function}<{@link Object[]}, {@link Object}>
     */
    @Override
    public Function<Object[], Object> createdProxyMethod(Method method) {
        SqlParser sqlParser = this.createdSql(method);
        String id = MapperStrongUtils.create(this.sqlSession, this.mapper, method, sqlParser.getSql());
        if (method.getReturnType() == boolean.class || method.getReturnType() == Boolean.class) {
            return (p) -> this.sqlSession.update(id, this.createMap(method.getParameters(), p, sqlParser)) > 0;
        }
        return (p) -> this.sqlSession.update(id, this.createMap(method.getParameters(), p, sqlParser));
    }

    /**
     * 创建sql
     *
     * @param method 方法
     * @return {@link String}
     */
    @Override
    public SqlParser createdSql(Method method) {
        SqlParser sqlParser = new SqlParser();
        Map<String, Function<?, Serializable>> dy = sqlParser.getParameters();
        DelFlagConfig delFlagConfig = MybatisScannerConfigurer.getBeanFactory().getBean(DelFlagConfig.class);
        FieldEmpty fieldEmpty = method.getAnnotation(FieldEmpty.class);
        Parameter[] parameters = method.getParameters();
        String table = MapperStrongUtils.getTableName(this.mapperParser.getEntityParser().getTableName(), "");
        String tableAlias = this.mapperParser.getEntityParser().getTableAlias();
        if (!StringUtils.hasText(table)) {
            throw new IllegalArgumentException("table name is not empty");
        }
        CodeStandardEnum codeStandardEnum = this.getCodeStandardEnum(fieldEmpty.nameMode());

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<script> ");

        if (fieldEmpty.value().length == 0) {
            throw new RuntimeException("清空的字段不可为空");
        }
        stringBuilder.append("update ").append(table).append(" as ").append(tableAlias).append(" set ");
        String collect = Arrays.stream(fieldEmpty.value()).map(s -> {
            String fieldName = getFieldName(codeStandardEnum, s.getValue(), fieldEmpty.removeSuffix());
            return fieldName + " = NULL";
        }).collect(Collectors.joining(", "));
        stringBuilder.append(collect).append(" where ");
        // 添加逻辑删除条件
        stringBuilder.append(delFlagConfig.generateSelectSql(dy,super.mapperParser.getEntityParser(),"","and"));
        if (parameters.length == 0) {
            throw new RuntimeException("清空的字段的条件不可为空");
        }
        for (Parameter parameter : parameters) {
            ParamAnnotation generate = ParamAnnotation.generate(parameter);
            String fieldName = tableAlias + "." + getFieldName(codeStandardEnum, generate.value, fieldEmpty.removeSuffix());
            // 判断是否是列表
            if (MapperStrongUtils.isListTypeClass(parameter.getType())) {
                stringBuilder.append(fieldName).append(" in <foreach item='item' collection='").append(generate.value).append("' open='(' separator=',' close=')'>#{item}</foreach> ").append(generate.oan.getValue()).append(" ");
            } else {
                stringBuilder.append(fieldName).append(" = #{").append(generate.value).append("} ").append(generate.oan.getValue()).append(" ");
            }
        }
        sqlParser.setSql(stringBuilder.toString().replaceAll(" not $| or $| and $|where $", "") + "</script>");
        return sqlParser;
    }
}
