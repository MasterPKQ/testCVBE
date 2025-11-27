package com.example.identity.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.FileTemplateResolver;

@Configuration
public class ThymeleafConfig {

    @Value("${app.templates.path}")
    private String templatesPath;

    @Bean
    public FileTemplateResolver fileTemplateResolver() {
        FileTemplateResolver resolver = new FileTemplateResolver();
        resolver.setPrefix(templatesPath);
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCacheable(true);
        resolver.setCacheTTLMs(-1L); // Cache vĩnh viễn, invalidate thủ công
        resolver.setOrder(1);
        resolver.setCheckExistence(true);
        return resolver;
    }

    @Bean(name = "cvTemplateEngine")
    public SpringTemplateEngine templateEngine(FileTemplateResolver fileTemplateResolver) {
        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(fileTemplateResolver);
        engine.setEnableSpringELCompiler(true);
        return engine;
    }
}
