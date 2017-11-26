package org.clever.devops;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Swagger2配置
 * <p>
 * 作者：lizw <br/>
 * 创建时间：2017/6/12 9:28 <br/>
 */
@Configuration
@EnableSwagger2
public class Swagger2Config {

    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("org.clever.devops.controller"))
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo apiInfo() {
        // Contact contact = new Contact("name", "url", "email");
        return new ApiInfoBuilder()
                .title("clever-devops服务")
                // .description("description")
                // .termsOfServiceUrl("termsOfServiceUrl")
                .version("1.0")
                // .license("license")
                // .licenseUrl("licenseUrl")
                // .termsOfServiceUrl("termsOfServiceUrl")
                // .contact(contact)
                // .extensions()
                .build();
    }
}
