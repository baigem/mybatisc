package cmc.mybatisc.strengthen.imp;

import cmc.mybatisc.annotation.FieldDelete;
import cmc.mybatisc.base.CodeStandardEnum;
import cmc.mybatisc.model.DelFlag;
import cmc.mybatisc.model.ParamAnnotation;
import cmc.mybatisc.strengthen.BaseStrengthen;
import cmc.mybatisc.utils.MapperStrongUtils;
import cn.hutool.core.date.DateUtil;
import org.apache.ibatis.session.SqlSession;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Date;
import java.util.function.Function;

/**
 * 字段查询句柄
 *
 * @author cmc
 * &#064;date  2023/05/25
 */
public class FieldDeleteHandle extends BaseStrengthen {
    public FieldDeleteHandle(SqlSession sqlSession, Class<?> mapper) {
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
        FieldDelete softDelect = method.getAnnotation(FieldDelete.class);

        Parameter[] parameters = method.getParameters();
        String table = MapperStrongUtils.getTableName(this.mapperParser.getTableName(), "");
        if (!StringUtils.hasText(table)) {
            throw new IllegalArgumentException("table name is not empty");
        }
        DelFlag delFlag = super.getDelFlag(softDelect.delFlag());
        CodeStandardEnum codeStandardEnum = this.getCodeStandardEnum(softDelect.nameMode());

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<script> ");
        if (delFlag != null) {
            // 软删除
            if (delFlag.isDeleteTime) {
                // 判断是时间戳还是其他
                if (delFlag.notDeleteValue.getClass() == Long.class) {
                    stringBuilder.append("update ").append(table).append(" set ").append(delFlag.fieldName).append(" = ").append(System.currentTimeMillis()).append(" where ");
                } else {
                    stringBuilder.append("update ").append(table).append(" set ").append(delFlag.fieldName).append(" = ").append(String.format("\"%s\"", DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"))).append(" where ");
                }
            } else {
                stringBuilder.append("update ").append(table).append(" set ").append(delFlag.fieldName).append(" = ").append(delFlag.deleteValue).append(" where ");
            }
            stringBuilder.append(this.generateDeleteFlag("", "and ", delFlag.fieldName, delFlag.notDeleteValue.toString(), delFlag.isDeleteTime));
        } else {
            // 真删除
            stringBuilder.append("delete from ").append(table).append(" where ");
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
        return stringBuilder.toString().replaceAll(" not $| or $| and $", "") + "</script>";
    }


}
