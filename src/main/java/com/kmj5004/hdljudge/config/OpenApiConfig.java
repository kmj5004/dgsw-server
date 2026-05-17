package com.kmj5004.hdljudge.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME = "bearer-jwt";

    @Bean
    public OpenAPI hdljudgeOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("HDL Online Judge API")
                .version("0.0.1")
                .description("브라우저에서 Verilog 코드를 작성·제출하면 격리 컨테이너로 시뮬레이션·합성·시각화하는 학습용 온라인 저지의 REST API. 자세한 설명은 docs/FUNCTIONAL-SPEC.md 참고.")
                .license(new License().name("학습용 프로젝트")))
            .components(new Components()
                .addSecuritySchemes(BEARER_SCHEME, new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description("로그인 응답의 accessToken 을 'Bearer <token>' 형태로 전달")))
            .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME));
    }
}
