package hu.otp.ticketing.core.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import hu.otp.ticketing.core.security.RequestLoggingInterceptor;
import hu.otp.ticketing.ticket.endpoint.rest.ApiClient;
import hu.otp.ticketing.ticket.endpoint.rest.EventsApi;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Configuration
public class GeneralConfig {
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(OffsetDateTime.class, new JsonSerializer<OffsetDateTime>() {
            @Override
            public void serialize(OffsetDateTime offsetDateTime, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
                jsonGenerator.writeString(DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(offsetDateTime));
            }
        });
        javaTimeModule.addDeserializer(LocalDateTime.class, new JsonDeserializer<LocalDateTime>() {
            @Override
            public LocalDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
                return LocalDateTime.ofInstant(Instant.ofEpochSecond(jsonParser.getValueAsLong()), ZoneId.systemDefault());
            }
        });
        objectMapper.registerModule(javaTimeModule);
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        return objectMapper;
    }


    @Bean
    @Primary
    public EventsApi eventsApi(@Value("${ticket.url}") String url,
                               @Value("${ticket.user}") String user,
                               @Value("${ticket.pass}") String pass,
                               MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter) {
        RestTemplate rt = getRestTemplate(user, pass, mappingJackson2HttpMessageConverter);
        ApiClient apiClient = new ApiClient(rt);
        apiClient.setBasePath(url);
        return new EventsApi(apiClient);
    }

    private RestTemplate getRestTemplate(String user, String pass, MappingJackson2HttpMessageConverter conv) {
        CredentialsProvider provider = new BasicCredentialsProvider();
        provider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(user, pass));
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(HttpClientBuilder
                .create()
                .setDefaultCredentialsProvider(provider)
                .disableContentCompression()
                .build());
        RestTemplate rt = new RestTemplate(new BufferingClientHttpRequestFactory(factory));
        List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
        messageConverters.add(new StringHttpMessageConverter());
        messageConverters.add(conv);
        rt.setMessageConverters(messageConverters);
        rt.setInterceptors(Collections.singletonList(new RequestLoggingInterceptor()));
        return rt;
    }
}
