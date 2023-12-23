package cmc.mybatisc.config;

import cmc.mybatisc.annotation.MapperStrong;
import cmc.mybatisc.core.MapperFactoryBeanClass;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;

/**
 * 自定义扫描仪,实现对mybatis的增强
 *
 * @author cmc
 * &#064;date  2023/05/23
 */
@Slf4j
@Configuration
public class MybatisScannerConfigurer implements BeanDefinitionRegistryPostProcessor, ApplicationContextAware {
    private MapperScannerConfigurer mapperScannerConfigurer;
    @Getter
    private static ConfigurableListableBeanFactory beanFactory;


    public MybatisScannerConfigurer() {
    }

    /**
     * {@inheritDoc}
     *
     * @param registry
     * @since 1.0.2
     */
    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) {
        this.mapperScannerConfigurer.setProcessPropertyPlaceHolders(false);
        this.mapperScannerConfigurer.setAnnotationClass(MapperStrong.class);
        this.mapperScannerConfigurer.setProcessPropertyPlaceHolders(true);
        this.mapperScannerConfigurer.postProcessBeanDefinitionRegistry(registry);

        this.mapperScannerConfigurer.setProcessPropertyPlaceHolders(false);
        this.mapperScannerConfigurer.setAnnotationClass(Mapper.class);
        this.mapperScannerConfigurer.setProcessPropertyPlaceHolders(true);
    }


    /**
     * {@inheritDoc}
     *
     * @param applicationContext
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.mapperScannerConfigurer = applicationContext.getBean(MapperScannerConfigurer.class);
        // 重新设置代理类
        this.mapperScannerConfigurer.setMapperFactoryBeanClass(MapperFactoryBeanClass.class);
    }

    /**
     * Modify the application context's internal bean factory after its standard
     * initialization. All bean definitions will have been loaded, but no beans
     * will have been instantiated yet. This allows for overriding or adding
     * properties even to eager-initializing beans.
     *
     * @param beanFactory the bean factory used by the application context
     * @throws BeansException in case of errors
     */
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        MybatisScannerConfigurer.beanFactory = beanFactory;
    }
}