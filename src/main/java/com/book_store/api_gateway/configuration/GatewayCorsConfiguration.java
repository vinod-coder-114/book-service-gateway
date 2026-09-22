package com.book_store.api_gateway.configuration;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
@EnableConfigurationProperties(GatewayCorsConfiguration.GatewayCorsProperties.class)
class GatewayCorsConfiguration {

	@Bean
	CorsWebFilter corsWebFilter(GatewayCorsProperties properties) {
		CorsConfiguration corsConfiguration = new CorsConfiguration();
		corsConfiguration.setAllowCredentials(properties.isAllowCredentials());
		corsConfiguration.setAllowedOriginPatterns(properties.getAllowedOriginPatterns());
		corsConfiguration.setAllowedMethods(properties.getAllowedMethods());
		corsConfiguration.setAllowedHeaders(properties.getAllowedHeaders());
		corsConfiguration.setExposedHeaders(properties.getExposedHeaders());
		corsConfiguration.setMaxAge(properties.getMaxAge());

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", corsConfiguration);
		return new CorsWebFilter(source);
	}

	@ConfigurationProperties(prefix = "app.cors")
	public static class GatewayCorsProperties {

		private List<String> allowedOriginPatterns = new ArrayList<>(List.of("*"));
		private List<String> allowedMethods = new ArrayList<>(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		private List<String> allowedHeaders = new ArrayList<>(List.of("*"));
		private List<String> exposedHeaders = new ArrayList<>(List.of("Location", "X-Correlation-Id"));
		private boolean allowCredentials;
		private Duration maxAge = Duration.ofMinutes(30);

		public List<String> getAllowedOriginPatterns() {
			return allowedOriginPatterns;
		}

		public void setAllowedOriginPatterns(List<String> allowedOriginPatterns) {
			this.allowedOriginPatterns = allowedOriginPatterns;
		}

		public List<String> getAllowedMethods() {
			return allowedMethods;
		}

		public void setAllowedMethods(List<String> allowedMethods) {
			this.allowedMethods = allowedMethods;
		}

		public List<String> getAllowedHeaders() {
			return allowedHeaders;
		}

		public void setAllowedHeaders(List<String> allowedHeaders) {
			this.allowedHeaders = allowedHeaders;
		}

		public List<String> getExposedHeaders() {
			return exposedHeaders;
		}

		public void setExposedHeaders(List<String> exposedHeaders) {
			this.exposedHeaders = exposedHeaders;
		}

		public boolean isAllowCredentials() {
			return allowCredentials;
		}

		public void setAllowCredentials(boolean allowCredentials) {
			this.allowCredentials = allowCredentials;
		}

		public Duration getMaxAge() {
			return maxAge;
		}

		public void setMaxAge(Duration maxAge) {
			this.maxAge = maxAge;
		}
	}
}
