package com.book_store.api_gateway;

import static com.book_store.api_gateway.filter.CorrelationLoggingWebFilter.CORRELATION_ID_HEADER;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApiGatewayApplicationTests {

	private static final String TEST_ORIGIN = "http://localhost:3000";
	private static final DisposableServer USER_SERVICE = HttpServer.create()
		.port(0)
		.route(routes -> routes.post("/api/user/login",
			(request, response) -> response.header(HttpHeaders.CONTENT_TYPE, "application/json")
				.sendString(Mono.just("{\"token\":\"ok\"}"))))
		.route(routes -> routes.get("/api/user/ping",
			(request, response) -> response.header(HttpHeaders.CONTENT_TYPE, "text/plain")
				.sendString(Mono.just("pong"))))
		.bindNow();

	@LocalServerPort
	private int port;

	@DynamicPropertySource
	static void registerProperties(DynamicPropertyRegistry registry) {
		registry.add("USER_SERVICE_URL", () -> "http://localhost:" + USER_SERVICE.port());
	}

	@AfterAll
	static void tearDown() {
		USER_SERVICE.disposeNow();
	}

	private WebTestClient webTestClient() {
		return WebTestClient.bindToServer()
			.baseUrl("http://localhost:" + port)
			.build();
	}

	@Test
	void shouldAllowPreflightRequestsToGatewayRoutes() {
		webTestClient().options()
			.uri("/book-store/api/user/login")
			.header(HttpHeaders.ORIGIN, TEST_ORIGIN)
			.header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
			.header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Authorization,Content-Type")
			.exchange()
			.expectStatus().is2xxSuccessful()
			.expectHeader().valueEquals(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, TEST_ORIGIN)
			.expectHeader().valueMatches(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, ".*POST.*")
			.expectHeader().valueMatches(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, ".*Authorization.*")
			.expectHeader().exists(CORRELATION_ID_HEADER);
	}

	@Test
	void shouldProxyBrowserRequestsAndAppendCorsHeaders() {
		webTestClient().post()
			.uri("/book-store/api/user/login")
			.header(HttpHeaders.ORIGIN, TEST_ORIGIN)
			.header(HttpHeaders.CONTENT_TYPE, "application/json")
			.exchange()
			.expectStatus().isOk()
			.expectHeader().valueEquals(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, TEST_ORIGIN)
			.expectHeader().valueMatches(CORRELATION_ID_HEADER, ".+")
			.expectBody(String.class).isEqualTo("{\"token\":\"ok\"}");
	}

	@Test
	void shouldPreserveIncomingCorrelationId() {
		String correlationId = "corr-12345";

		webTestClient().post()
			.uri("/book-store/api/user/login")
			.header(HttpHeaders.ORIGIN, TEST_ORIGIN)
			.header(CORRELATION_ID_HEADER, correlationId)
			.exchange()
			.expectStatus().isOk()
			.expectHeader().valueEquals(CORRELATION_ID_HEADER, correlationId);
	}
}
