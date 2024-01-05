package cmc.mybatisc.config.interfaces.impl;

import cmc.mybatisc.config.interfaces.MybatiscConfig;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

/**
 * 默认mybatisc配置
 *
 * @author cmc
 * &#064;date  2024/01/03
 */
@Component
public class DefaultMybatiscConfig implements MybatiscConfig, Ordered {
    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}