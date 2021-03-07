package hu.otp.ticketing.core.config;

import hu.otp.ticketing.core.security.HeaderValidatorInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
@RequiredArgsConstructor
public class MvcConfig implements WebMvcConfigurer {
    private final HeaderValidatorInterceptor headerValidatorInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(headerValidatorInterceptor).addPathPatterns("/**");
    }
}
