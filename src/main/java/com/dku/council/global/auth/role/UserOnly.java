package com.dku.council.global.auth.role;

import com.dku.council.global.config.swagger.SwaggerConfig;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.security.access.annotation.Secured;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION)
@Secured(UserAuthNames.ROLE_USER)
public @interface UserOnly {
}
