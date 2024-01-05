package cmc.mybatisc.model;

import cmc.mybatisc.annotation.DataScope;
import cmc.mybatisc.utils.reflect.ReflectUtils;
import cn.hutool.core.util.ReflectUtil;
import lombok.NoArgsConstructor;
import org.apache.ibatis.annotations.Param;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;

/**
 * 参数注释
 *
 * @author cmc
 * &#064;date  2023/06/10
 */
@NoArgsConstructor
public class ParamAnnotation {
    /**
     * 字段参数名称
     */
    public String value;
    /**
     * 是否模糊查询
     */
    public boolean like = false;
    /**
     * 为null
     */
    public boolean isNull = false;

    public boolean sort = false;

    public String sortRule;

    public DataScope.Oan oan = DataScope.Oan.AND;

    public cmc.mybatisc.annotation.Param.Over left = cmc.mybatisc.annotation.Param.Over.EMPTY;

    /**
     * 右
     */
    public cmc.mybatisc.annotation.Param.Over right = cmc.mybatisc.annotation.Param.Over.EMPTY;


    public ParamAnnotation(String value) {
        this.value = value;
    }

    /**
     * 创建
     *
     * @param param 参数
     * @return {@link ParamAnnotation}
     */
    public static ParamAnnotation generate(Annotation param) {
        ParamAnnotation paramAnnotation = new ParamAnnotation();
        paramAnnotation.value = ReflectUtil.invoke(param, "value");
        paramAnnotation.oan = ReflectUtils.invokeGet(param, "oan", DataScope.Oan.AND);
        paramAnnotation.left = ReflectUtils.invokeGet(param, "left", cmc.mybatisc.annotation.Param.Over.EMPTY);
        paramAnnotation.right = ReflectUtils.invokeGet(param, "right", cmc.mybatisc.annotation.Param.Over.EMPTY);
        paramAnnotation.like = ReflectUtils.invokeGet(param, "like", false);
        paramAnnotation.isNull = ReflectUtils.invokeGet(param, "isNull", false);
        paramAnnotation.sort = ReflectUtils.invokeGet(param, "sort", false);
        paramAnnotation.sortRule = ReflectUtils.invokeGet(param, "sortRule", "desc");
        return paramAnnotation;
    }

    public static ParamAnnotation generate(Parameter parameter) {
        ParamAnnotation paramAnnotation;
        if (parameter.isAnnotationPresent(Param.class)) {
            paramAnnotation = generate(parameter.getAnnotation(Param.class));
        } else if (parameter.isAnnotationPresent(cmc.mybatisc.annotation.Param.class)) {
            paramAnnotation = generate(parameter.getAnnotation(cmc.mybatisc.annotation.Param.class));
        } else {
            paramAnnotation = new ParamAnnotation(parameter.getName());
        }
        if (paramAnnotation.value.isEmpty()) {
            paramAnnotation.value = parameter.getName();
        }
        return paramAnnotation;
    }

}
