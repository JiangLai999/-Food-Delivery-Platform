package com.fooddelivery.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.servlet.context-path:/api}")
    private String contextPath;

    @Value("${server.port:8080}")
    private String port;

    @Bean
    public OpenAPI customOpenAPI() {
        // 构建服务器信息
        Server localServer = new Server()
                .url("http://localhost:" + port + contextPath)
                .description("本地开发环境");

        Server devServer = new Server()
                .url("http://dev.fooddelivery.com" + contextPath)
                .description("开发测试环境");

        // 创建 JWT 安全方案
        SecurityScheme jwtScheme = new SecurityScheme()
                .name("JWT")
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("请在请求头中添加 Authorization: Bearer {token}");

        // 创建 API 信息
        Info info = new Info()
                .title("外卖订餐系统 API 文档")
                .description("外卖订餐系统后端 RESTful API 接口文档\n\n" +
                        "### 接口说明\n" +
                        "1. 所有接口均返回统一格式的 JSON 数据\n" +
                        "2. 需要认证的接口需在 Header 中添加 Authorization: Bearer {token}\n" +
                        "3. 错误码说明：200-成功，400-请求错误，401-未认证，403-无权限，500-服务器错误\n\n" +
                        "### 用户类型说明\n" +
                        "- **admin**: 管理员\n" +
                        "- **merchant**: 商家\n" +
                        "- **user**: 普通用户\n" +
                        "- **rider**: 骑手")
                .version("1.0.0")
                .contact(new Contact()
                        .name("开发团队")
                        .email("support@fooddelivery.com")
                        .url("http://www.fooddelivery.com"))
                .license(new License()
                        .name("Apache 2.0")
                        .url("http://www.apache.org/licenses/LICENSE-2.0.html"));

        return new OpenAPI()
                .info(info)
                .servers(Arrays.asList(localServer, devServer))
                .addSecurityItem(new SecurityRequirement().addList("JWT"))
                .components(new Components()
                        .addSecuritySchemes("JWT", jwtScheme));
    }
}