/** 以下代码由代码生成器自动生成，如无必要，请勿修改！2022-3-31 14:00:42 **/
package com.blackfire.aicloud.provider.api.config;

import com.blackfire.aicloud.common.constant.DateTimeConstant;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * @author code-generator
 * @date 2022-3-31 14:00:42
 */

@Configuration
public class JacksonConfig {
    @Bean
    public ObjectMapper objectMapper(){
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(LocalDateTime.class,new LocalDateTimeSerializer(DateTimeConstant.DEFAULT_DATE_TIME_FORMAT));
        javaTimeModule.addSerializer(LocalDate.class,new LocalDateSerializer(DateTimeConstant.DEFAULT_DATE_FORMAT));
        javaTimeModule.addSerializer(LocalTime.class,new LocalTimeSerializer(DateTimeConstant.DEFAULT_TIME_FORMAT));
        javaTimeModule.addDeserializer(LocalDateTime.class,new LocalDateTimeDeserializer(DateTimeConstant.DEFAULT_DATE_TIME_FORMAT));
        javaTimeModule.addDeserializer(LocalDate.class,new LocalDateDeserializer(DateTimeConstant.DEFAULT_DATE_FORMAT));
        javaTimeModule.addDeserializer(LocalTime.class,new LocalTimeDeserializer(DateTimeConstant.DEFAULT_TIME_FORMAT));
        objectMapper.registerModule(javaTimeModule).registerModule(new ParameterNamesModule());
        return objectMapper;
    }

}