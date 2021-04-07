package bootiful.gateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.time.Instant;
import java.util.Map;
import java.util.function.Supplier;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@Log4j2
@SpringBootApplication
public class GatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(GatewayApplication.class, args);
	}

	private final Sinks.Many<String> updates = Sinks
		.many()
		.multicast()
		.onBackpressureBuffer();

	@Bean
	Supplier<Flux<String>> updatesSupplier() {
		return updates::asFlux;
	}

	@SneakyThrows
	private String buildJsonString(ObjectMapper om, Object object) {
		return om.writeValueAsString(object);
	}

	@Bean
	RouterFunction<ServerResponse> routes(ObjectMapper om) {
		return route()
			.GET("/update", request -> {
				var message = Map.of("message", "Hello, world @ " + Instant.now() + "!");
				var success = this.updates.tryEmitNext(buildJsonString(om, message));
				log.info("success: " + success);
				return ok().bodyValue(message);
			})
			.build();
	}


}
