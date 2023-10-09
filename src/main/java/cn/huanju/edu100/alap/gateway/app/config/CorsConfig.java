package cn.huanju.edu100.alap.gateway.app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.util.pattern.PathPatternParser;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @Description: 跨域
 * @Author: xushengbin@hqwx.com
 * @Date: 2021-08-25
 */
@Configuration
public class CorsConfig {

    @Value("${hqgateway.cors-config.allowed-origins:}")
    private String allowedOrigins;

    @Value("${hqgateway.cors-config.allowed-headers:}")
    private String allowedHeaders;

    @Value("${hqgateway.cors-config.allowed-methods:}")
    private String allowedMethods;

    @Value("${hqgateway.cors-config.allowed-Credentials:true}")
    private boolean allowedCredentials;

    @Value("${hqgateway.cors-config.max-age:}")
    private Long maxAge;

    @Bean
    public CorsWebFilter corsWebFilter() {
        UrlBasedCorsConfigurationSource configurationSource = new UrlBasedCorsConfigurationSource(new PathPatternParser());
        CorsConfiguration configuration = new CorsConfiguration();

        //允许携带 cookie认证信息
        configuration.setAllowCredentials(this.allowedCredentials);

        final String Split_Separator = ",";
        //源
        List<String> allowedOriginPatternsList = null;
        if (StringUtils.hasText(this.allowedOrigins)) {
            allowedOriginPatternsList = Arrays.asList(this.allowedOrigins.split(Split_Separator));
        }
        configuration.setAllowedOriginPatterns(allowedOriginPatternsList);

        //Header
        List<String> allowedHeadersList = null;
        if (StringUtils.hasText(this.allowedHeaders)) {
            allowedHeadersList = Arrays.asList(this.allowedHeaders.split(Split_Separator));
        }
        configuration.setAllowedHeaders(allowedHeadersList);

        //Method
        List<String> allowedMethodsList = null;
        if (StringUtils.hasText(this.allowedMethods)) {
            allowedMethodsList = Arrays.asList(this.allowedMethods.split(Split_Separator));
        }
        configuration.setAllowedMethods(allowedMethodsList);

        //options有效期，单位: 秒(s)
        if (Objects.nonNull(this.maxAge) && this.maxAge > 0) {
            configuration.setMaxAge(this.maxAge);
        }

        configurationSource.registerCorsConfiguration("/**", configuration);
        return new CorsWebFilter(configurationSource);
    }
}
