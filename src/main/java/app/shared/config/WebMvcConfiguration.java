package app.shared.config;

import app.security.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.*;
import org.springframework.web.servlet.config.annotation.*;


@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

    @Autowired
    private SessionCheckInterceptor interceptor;


    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        //** - всичко след
        registry.addInterceptor(interceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/css/**", "/images/**");
    }
}