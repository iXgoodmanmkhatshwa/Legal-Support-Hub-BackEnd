package za.co.ixengineers.legal.security;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import org.springframework.security.web.SecurityFilterChain;

import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.http.HttpStatus;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        return http
                .csrf(csrf -> csrf.disable())

                .cors(cors -> {
                })

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(authorize ->
                        authorize
                                .requestMatchers(
                                        "/actuator/health/**",
                                        "/actuator/info"
                                )
                                .permitAll()

                                .requestMatchers("/api/admin/**")
                                .hasAnyRole(
                                        "SYSTEM_ADMIN",
                                        "HEAD_OF_LEGAL"
                                )

                                .anyRequest()
                                .authenticated()
                )

                .oauth2ResourceServer(resourceServer ->
                        resourceServer.jwt(jwt ->
                                jwt.jwtAuthenticationConverter(
                                        jwtAuthenticationConverter()
                                )
                        )
                )
                .exceptionHandling(exceptions ->
                        exceptions
                                .authenticationEntryPoint(
                                        (request, response, exception) -> {
                                            response.setStatus(
                                                    HttpStatus.UNAUTHORIZED.value()
                                            );

                                            response.setContentType(
                                                    "application/json"
                                            );

                                            response.getWriter().write(
                                                    """
                                                    {
                                                      "message": "Authentication failed"
                                                    }
                                                    """
                                            );
                                        }
                                )
                                .accessDeniedHandler(
                                        (request, response, exception) -> {
                                            response.setStatus(
                                                    HttpStatus.FORBIDDEN.value()
                                            );

                                            response.setContentType(
                                                    "application/json"
                                            );

                                            response.getWriter().write(
                                                    """
                                                    {
                                                      "message": "Access denied"
                                                    }
                                                    """
                                            );
                                        }
                                )
                )

                .build();
    }
    @Bean
    JwtDecoder jwtDecoder(
            @Value(
                    "${spring.security.oauth2.resourceserver.jwt.issuer-uri}"
            )
            String issuer,

            @Value("${security.expected-audience}")
            String expectedAudience
    ) {
        NimbusJwtDecoder decoder =
                JwtDecoders.fromIssuerLocation(issuer);

        OAuth2TokenValidator<Jwt> issuerValidator =
                JwtValidators.createDefaultWithIssuer(issuer);

        OAuth2TokenValidator<Jwt> audienceValidator =
                jwt -> {
                    boolean valid =
                            jwt.getAudience()
                                    .contains(expectedAudience);

                    if (valid) {
                        return OAuth2TokenValidatorResult.success();
                    }

                    OAuth2Error error =
                            new OAuth2Error(
                                    "invalid_token",
                                    "Invalid audience. Expected: " +
                                            expectedAudience +
                                            ", received: " +
                                            jwt.getAudience(),
                                    null
                            );

                    return OAuth2TokenValidatorResult.failure(
                            error
                    );
                };

        decoder.setJwtValidator(
                new DelegatingOAuth2TokenValidator<>(
                        issuerValidator,
                        audienceValidator
                )
        );

        return decoder;
    }


    private Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter() {

        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();

        /*
         * Microsoft Entra application roles are delivered in
         * the access-token "roles" claim.
         */
        authoritiesConverter.setAuthoritiesClaimName("roles");
        authoritiesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter authenticationConverter =
                new JwtAuthenticationConverter();

        authenticationConverter.setJwtGrantedAuthoritiesConverter(
                authoritiesConverter
        );

        return authenticationConverter;
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration =
                new CorsConfiguration();

        configuration.setAllowedOrigins(
                List.of("http://localhost:4200")
        );

        configuration.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PATCH",
                        "PUT",
                        "DELETE",
                        "OPTIONS"
                )
        );

        configuration.setAllowedHeaders(
                List.of(
                        "Authorization",
                        "Content-Type",
                        "Accept",
                        "Origin",
                        "X-Requested-With"
                )
        );

        configuration.setExposedHeaders(
                List.of("Location")
        );

        configuration.setAllowCredentials(true);

        /*
         * Cache the browser's successful CORS preflight response
         * for one hour.
         */
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/api/**",
                configuration
        );

        return source;
    }
}