package com.logistics.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 文档配置
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("综合物流管理系统 API")
                        .description("仓储管理 / 订单管理 / 运输管理 API 文档")
                        .version("1.0.0")
                        .contact(new Contact().name("Logistics Team")));
    }
}
