package cmc.mybatisc;

import cmc.mybatisc.core.GlobalContextHolder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ConfigurableBootstrapContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * 核心应用程序运行监听器
 *
 * @author cmc
 * @date 2023/05/23
 */
@Slf4j
public class CoreApplicationRunListener implements SpringApplicationRunListener {
    /**
     * 环境
     */
    @Getter
    private static ConfigurableEnvironment environment;

    public CoreApplicationRunListener(SpringApplication ignore, String[] ignoreArgs) {
    }

    /**
     * 配置解析好的时候执行
     *
     * @param bootstrapContext the bootstrap context
     * @param environment      the environment
     */
    @Override
    public void environmentPrepared(ConfigurableBootstrapContext bootstrapContext, ConfigurableEnvironment environment) {
        CoreApplicationRunListener.environment = environment;
    }

    /**
     * 项目启动后执行的任务
     *
     * @param context 上下文
     */
    @Override
    public void running(ConfigurableApplicationContext context) {
        GlobalContextHolder.waitingProjectForInitializationTaskToComplete();
    }
}
