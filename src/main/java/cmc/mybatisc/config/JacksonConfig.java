package cmc.mybatisc.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * 杰克逊配置
 *
 * @author 程梦城
 * @version 1.0.0
 * &#064;date  2023/12/14
 */
@SuppressWarnings("rowtypes")
@Configuration
public class JacksonConfig implements WebMvcConfigurer {

    private final StdSerializer<Long> stdSerializer = new StdSerializer<Long>(Long.class) {
        private static final long serialVersionUID = 9066520500999029610L;

        @Override
        public void serialize(Long aLong, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            if (aLong > Integer.MAX_VALUE) {
                jsonGenerator.writeString(aLong.toString());
            } else {
                jsonGenerator.writeNumber(aLong);
            }
        }
    };

    @Bean
    @Primary
    public Jackson2ObjectMapperBuilder getJackson2ObjectMapperBuilder() {
        return new Jackson2ObjectMapperBuilder();
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverterFactory(new StringToBaseDictConverterFactory());
        registry.addConverterFactory(new StringToStringListConverterFactory());

    }


    @Bean
    @Primary
    @ConditionalOnMissingBean(ObjectMapper.class)
    public ObjectMapper jacksonObjectMapper(Jackson2ObjectMapperBuilder builder) {
        ObjectMapper build = builder
                .serializerByType(Long.class, this.stdSerializer)
                .serializerByType(Long.TYPE, this.stdSerializer)
                .createXmlMapper(false).build();
        build.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        build.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        return build;
    }

    /**
     * Jackson全局转化long类型为String,解决jackson序列化时long类型缺失精度问题
     *
     * @return Jackson2ObjectMapperBuilderCustomizer 注入的对象
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return jacksonObjectMapperBuilder -> jacksonObjectMapperBuilder
                .serializerByType(Long.class, this.stdSerializer)
                .serializerByType(Long.TYPE, this.stdSerializer)
                .timeZone(TimeZone.getDefault());
    }
}
