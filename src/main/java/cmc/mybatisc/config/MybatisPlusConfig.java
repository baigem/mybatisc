package cmc.mybatisc.config;

import cmc.mybatisc.core.MybatisInterceptorRegister;
import cmc.mybatisc.core.PlusInterceptor;
import cmc.mybatisc.typehandler.StringListTypeHandler;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@ComponentScan(basePackages = "cmc.mybatisc")
public class MybatisPlusConfig {
    /**
     * 数据权限过滤
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor(MybatisInterceptorRegister mybatisInterceptorRegister) {
        MybatisPlusInterceptor interceptor = new PlusInterceptor();
        // mybatis的拦截处理器
        interceptor.addInnerInterceptor(mybatisInterceptorRegister);
        // 分页插件
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        // 乐观锁
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        // 防全表更新与删除
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());

        return interceptor;
    }

    @Bean
    public ConfigurationCustomizer configurationCustomizer() {
        return configuration -> {
            configuration.getTypeHandlerRegistry().register(StringListTypeHandler.class);
        };
    }

}
