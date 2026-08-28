package com.smartordering.framework.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.io.IOException;
import java.util.List;

/**
 * 雪花 ID 字符串化配置。
 *
 * <p>MyBatis-Plus ASSIGN_ID 生成的雪花 ID 超过 JavaScript 安全整数上限
 * （2^53-1），前端 JSON.parse 后精度不可逆丢失（如 2093163236156264450 会变成
 * 2093163236156264400），把错误的 ID 回传后端会导致 "not found" 类错误。</p>
 *
 * <p>本配置显式构建 Web 使用的 {@link ObjectMapper}（继承 Spring Boot 自动配置的全部
 * 定制），并通过 BeanSerializerModifier 把响应对象里所有以 {@code Id}/{@code Ids}
 * 结尾的数值字段序列化为字符串（{@code Ids} 列表逐元素转字符串），保证前端原样回传。
 * 非 ID 字段（如分页 total、金额、状态码）不受影响。</p>
 *
 * @author smartordering
 */
@Configuration
public class JacksonIdStringConfig {

    /** List<Long> 类型的 *Ids 字段：逐元素转字符串 */
    private static final JsonSerializer<Object> ID_LIST_SERIALIZER = new JsonSerializer<>() {
        @Override
        public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeStartArray();
            for (Object item : (Iterable<?>) value) {
                gen.writeString(item == null ? null : String.valueOf(item));
            }
            gen.writeEndArray();
        }
    };

    @Bean
    @Primary
    public ObjectMapper smartOrderingObjectMapper(Jackson2ObjectMapperBuilder builder) {
        return builder.postConfigurer(objectMapper -> objectMapper.setSerializerFactory(
                        objectMapper.getSerializerFactory()
                                .withSerializerModifier(new BeanSerializerModifier() {
                                    @Override
                                    public List<BeanPropertyWriter> changeProperties(SerializationConfig config,
                                                                                     BeanDescription beanDesc,
                                                                                     List<BeanPropertyWriter> beanProperties) {
                                        for (BeanPropertyWriter writer : beanProperties) {
                                            String name = writer.getName().toLowerCase();
                                            if (name.endsWith("ids") && writer.getType().isContainerType()) {
                                                // *Ids 集合：如 specGroupIds / optionIds / menuIds
                                                writer.assignSerializer(ID_LIST_SERIALIZER);
                                            } else if (name.endsWith("id")
                                                    && Number.class.isAssignableFrom(writer.getType().getRawClass())) {
                                                // *Id 标量：如 id / categoryId / specGroupId / orderId
                                                writer.assignSerializer(ToStringSerializer.instance);
                                            }
                                        }
                                        return beanProperties;
                                    }
                                })))
                .build();
    }
}