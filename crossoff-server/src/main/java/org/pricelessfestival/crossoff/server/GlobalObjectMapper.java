package org.pricelessfestival.crossoff.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

/**
 * Created by ivan on 4/27/18.
 */
@Provider
public class GlobalObjectMapper implements ContextResolver<ObjectMapper> {

    // customized for JSR310 timestamp support
    public static final ObjectMapper JACKSON = new ObjectMapper().registerModule(new JavaTimeModule());

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return JACKSON;
    }
}
