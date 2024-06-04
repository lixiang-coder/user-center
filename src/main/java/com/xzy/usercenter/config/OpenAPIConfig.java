package com.xzy.usercenter.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;


@Configuration
@Slf4j
public class OpenAPIConfig {
    @Bean
    public OpenAPI openAPI() {
        log.info("Swagger 接口文档开始生成");
        return new OpenAPI()
                .info(new Info()
                        .title("用户中心")
                        .description("用户中心接口文档")
                        .version("版本v1.0"))
                .externalDocs(new ExternalDocumentation()
                        .description("描述信息")
                        .url("/"));
    }
}
