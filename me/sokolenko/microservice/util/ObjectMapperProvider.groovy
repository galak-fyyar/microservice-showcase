package me.sokolenko.microservice.util

import org.codehaus.jackson.map.ObjectMapper
import org.codehaus.jackson.map.SerializationConfig

import javax.ws.rs.ext.ContextResolver
import javax.ws.rs.ext.Provider

/**
 * @author Anatoliy Sokolenko
 */
@Provider
class ObjectMapperProvider implements ContextResolver<ObjectMapper> {

    final ObjectMapper objectMapper;

    public ObjectMapperProvider() {
        this.objectMapper = createDefaultMapper();
    }

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return objectMapper;
    }

    private static ObjectMapper createDefaultMapper() {
        ObjectMapper result = new ObjectMapper();
        result.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);

        return result;
    }

}
