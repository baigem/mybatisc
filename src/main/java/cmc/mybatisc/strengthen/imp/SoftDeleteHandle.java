package cmc.mybatisc.strengthen.imp;

import cmc.mybatisc.annotation.SoftDelete;
import cmc.mybatisc.config.DelFlagConfig;
import cmc.mybatisc.config.MybatisScannerConfigurer;
import cmc.mybatisc.core.util.TableStructure;
import cmc.mybatisc.model.ParamAnnotation;
import cmc.mybatisc.parser.SqlParser;
import cmc.mybatisc.strengthen.BaseStrengthen;
import cmc.mybatisc.utils.MapperStrongUtils;
import org.apache.ibatis.session.SqlSession;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.function.Function;

/**
 * 字段查询句柄
 *
 * @author cmc
 * &#064;date  2023/05/25
 */
public class SoftDeleteHandle extends BaseStrengthen {
    public SoftDeleteHandle(SqlSession sqlSession, Class<?> mapper) {
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
            return (p) -> this.sqlSession.update(id, this.createMap(method.getParameters(), p,sqlParser)) > 0;
        }
        return (p) -> this.sqlSession.update(id, this.createMap(method.getParameters(), p,sqlParser));
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
        SoftDelete softDelete = method.getAnnotation(SoftDelete.class);
        // 获取表结构
        TableStructure table = TableStructure.getTableStructure(softDelete.table(),mapperParser.getTableStructure());
        if (table == null || !StringUtils.hasText(table.getName())) {
            throw new IllegalArgumentException("table name is not empty");
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<script> ");
        // 进行删除
        stringBuilder.append(delFlagConfig.generateDeleteSql(dy,super.mapperParser.getTableStructure(),"where "));
        // 添加逻辑删除
        stringBuilder.append(delFlagConfig.generateSelectSql(dy,super.mapperParser.getTableStructure(),"","and "));
        for (Parameter parameter : method.getParameters()) {
            ParamAnnotation generate = ParamAnnotation.generate(parameter);
            String fieldName = table.getCompleteFieldName(generate.value, softDelete.removeSuffix());
            // 判断是否是列表
            if (MapperStrongUtils.isListTypeClass(parameter.getType())) {
                stringBuilder.append(fieldName).append(" in <foreach item='item' collection='").append(generate.value).append("' open='(' separator=',' close=')'>#{item}</foreach> ").append(generate.oan.getValue()).append(" ");
            } else {
                stringBuilder.append(fieldName).append(" = #{").append(generate.value).append("} ").append(generate.oan.getValue()).append(" ");
            }
        }
        sqlParser.setSql(stringBuilder.toString().replaceAll(" not $| or $| and $", "") + "</script>");

        return sqlParser;
    }


}
