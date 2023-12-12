package cmc.mybatisc.datafilter.handler;

import cmc.mybatisc.annotation.DataScope;
import cmc.mybatisc.core.MybatisInterceptorRegister;
import cmc.mybatisc.datafilter.core.BaseDataFilterHandler;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

/**
 * 我企业数据处理程序
 *
 * @author root
 * &#064;date  2023/07/05
 */
@Aspect
@Slf4j
@Component
public class DataScopeHandler extends BaseDataFilterHandler<DataScope> {

    @Autowired
    public DataScopeHandler(MybatisInterceptorRegister mybatisInterceptorRegister) {
        super(mybatisInterceptorRegister);
    }

    /**
     * 配置织入点
     */
    @Pointcut("@annotation(dataScope)")
    public void dataScopePointCut(DataScope dataScope) {
    }

    @Before(value = "dataScopePointCut(dataScope)", argNames = "dataScope")
    public void doBefore(DataScope dataScope) {
        super.doBefore(dataScope);
    }

    /**
     * 获取数据范围值
     *
     * @return {@link Supplier}<{@link String}>
     */
    @Override
    protected Supplier<String> getDataScopeValue() {
        // 只能查看未审核或已审核的数据
        return () -> super.annotation.get().dataScopeValue();
    }

    /**
     * 获取匹配模式
     *
     * @return {@link String}
     */
    @Override
    protected String getMatchPattern() {
        return super.annotation.get().matchPattern();
    }
}
