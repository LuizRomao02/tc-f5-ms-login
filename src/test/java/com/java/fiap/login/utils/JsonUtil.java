package com.java.fiap.login.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.commons.lang3.ObjectUtils;

public final class JsonUtil {

    private static volatile ObjectMapper objectMapper;

    private JsonUtil() {
        throw new UnsupportedOperationException("This class cannot be instantiated.");
    }

    private static ObjectMapper getObjectMapper() {
        if (ObjectUtils.isEmpty(objectMapper)) {
            synchronized (JsonUtil.class) {
                if (ObjectUtils.isEmpty(objectMapper)) {
                    objectMapper = new ObjectMapper();
                    objectMapper.registerModule(new JavaTimeModule());
                    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
                }
            }
        }
        return objectMapper;
    }

    public static String toJson(final Object object) {
        try {
            return getObjectMapper().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T toObject(final String json, Class<T> tClass) {
        try {
            return getObjectMapper().readValue(json, tClass);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
