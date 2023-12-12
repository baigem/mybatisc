package cmc.mybatisc.strengthen.imp;

import cmc.mybatisc.annotation.FieldEmpty;
import cmc.mybatisc.base.CodeStandardEnum;
import cmc.mybatisc.model.DelFlag;
import cmc.mybatisc.model.ParamAnnotation;
import cmc.mybatisc.strengthen.BaseStrengthen;
import cmc.mybatisc.utils.MapperStrongUtils;
import org.apache.ibatis.session.SqlSession;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
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
        String id = MapperStrongUtils.create(this.sqlSession, this.mapper, method, this.createdSql(method));
        if (method.getReturnType() == boolean.class || method.getReturnType() == Boolean.class) {
            return (p) -> this.sqlSession.update(id, this.createMap(method.getParameters(), p)) > 0;
        }
        return (p) -> this.sqlSession.update(id, this.createMap(method.getParameters(), p));
    }

    /**
     * 创建sql
     *
     * @param method 方法
     * @return {@link String}
     */
    @Override
    public String createdSql(Method method) {
        FieldEmpty softDelect = method.getAnnotation(FieldEmpty.class);
        Parameter[] parameters = method.getParameters();
        String table = MapperStrongUtils.getTableName(this.mapperParser.getTableName(), softDelect.table());
        if (!StringUtils.hasText(table)) {
            throw new IllegalArgumentException("table name is not empty");
        }
        DelFlag delFlag = super.getDelFlag(softDelect.delFlag());
        CodeStandardEnum codeStandardEnum = this.getCodeStandardEnum(softDelect.nameMode());

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<script> ");

        if (softDelect.value().length == 0) {
            throw new RuntimeException("清空的字段不可为空");
        }
        stringBuilder.append("update ").append(table).append(" set ");
        String collect = Arrays.stream(softDelect.value()).map(s -> {
            String fieldName = getFieldName(codeStandardEnum, s.getValue(), softDelect.removeSuffix());
            return fieldName + " = NULL";
        }).collect(Collectors.joining(", "));
        stringBuilder.append(collect).append(" where ");

        if (delFlag != null) {
            stringBuilder.append(this.generateDeleteFlag("", "and ", delFlag.fieldName, delFlag.notDeleteValue.toString(), delFlag.isDeleteTime));
        }
        if (parameters.length == 0) {
            throw new RuntimeException("清空的字段的条件不可为空");
        }
        for (Parameter parameter : parameters) {
            ParamAnnotation generate = ParamAnnotation.generate(parameter);
            String fieldName = getFieldName(codeStandardEnum, generate.value, softDelect.removeSuffix());
            // 判断是否是列表
            if (MapperStrongUtils.isListTypeClass(parameter.getType())) {
                stringBuilder.append(fieldName).append(" in <foreach item='item' collection='").append(generate.value).append("' open='(' separator=',' close=')'>#{item}</foreach> ").append(generate.oan.getValue()).append(" ");
            } else {
                stringBuilder.append(fieldName).append(" = #{").append(generate.value).append("} ").append(generate.oan.getValue()).append(" ");
            }
        }
        return stringBuilder.toString().replaceAll(" not $| or $| and $|where $", "") + "</script>";
    }
}
