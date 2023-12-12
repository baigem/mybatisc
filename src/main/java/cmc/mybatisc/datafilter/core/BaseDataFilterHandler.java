package cmc.mybatisc.datafilter.core;

import cmc.mybatisc.core.MybatisInterceptor;
import cmc.mybatisc.core.MybatisInterceptorRegister;
import org.aspectj.lang.annotation.After;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 基本数据过滤器处理程序
 *
 * @author 程梦城
 * @version 1.0.0
 * &#064;date  2023/07/07
 */
@SuppressWarnings(value = {"unchecked"})
public abstract class BaseDataFilterHandler<T extends Annotation> {
    /**
     * 注册器
     */
    private final MybatisInterceptorRegister register;
    /**
     * 注释
     */
    protected ThreadLocal<T> annotation = new ThreadLocal<>();

    public BaseDataFilterHandler(MybatisInterceptorRegister register) {
        this.register = register;
        for (Method method : this.getClass().getMethods()) {
            if (method.getName().equals("dataScopePointCut")) {
                return;
            }
        }
        throw new RuntimeException("数据拦截器的切入点方法名必须为dataScopePointCut，且入参必须为切入点指定的注解");
    }


    public void doBefore(T dataScope) {
        // 获得注解
        register(dataScope);
    }

    /**
     * 获取数据范围值
     *
     * @return {@link Supplier}<{@link String}>
     */
    protected abstract Supplier<String> getDataScopeValue();

    /**
     * 获取匹配模式
     *
     * @return {@link String}
     */
    protected String getMatchPattern() {
        return DataFilterOption.TO;
    }

    /**
     * 是启用预编译吗 默认开启
     *
     * @return boolean
     */
    protected boolean isEnablePrecompilation() {
        return true;
    }

    /**
     * 注册
     *
     * @param dataScope 数据范围
     */
    protected void register(Annotation dataScope) {
        MybatisInterceptor mybatisInterceptor;
        DataFilterOption dataFilterOption;
        this.annotation.set((T) dataScope);
        // 进行注册
        if (!register.hash(dataScope)) {
            DataFilterCoreProxy proxy = new DataFilterCoreProxy(register);
            proxy.setPrecompile(this.isEnablePrecompilation());
            dataFilterOption = new DataFilterOption(this.getDataScopeValue(), this.getMatchPattern(), DataScopeSourceData.create(dataScope));
            proxy.setDataFilterOption(dataFilterOption);
            // 进行注册,如果有缓存则不进行覆盖
            register.register(dataScope, proxy, false);
            mybatisInterceptor = proxy;
        } else {
            mybatisInterceptor = register.get(dataScope);
            dataFilterOption = ((DataFilterCoreCache) mybatisInterceptor).getDataFilterOption();
        }
        // 开始使用
        register.useRegister(Arrays.stream(dataFilterOption.getDataSource().getFrom()).collect(Collectors.toList()), mybatisInterceptor);
    }

    /**
     * 清空当前线程上次保存的权限信息
     */
    @After(value = "dataScopePointCut(dataScope)", argNames = "dataScope")
    public void clearThreadLocal(T ignore) {
        // 清理线程数据
        register.removeLocal();
        annotation.remove();
    }
}
