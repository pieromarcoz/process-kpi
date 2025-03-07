package pe.farmaciasperuanas.digital.process.kpi.domain.port.repository;

import pe.farmaciasperuanas.digital.process.kpi.domain.entity.Metrics;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface MetricsRepository {
    Mono<Metrics> save(Metrics metrics);
    Mono<Metrics> findByProviderId(String providerId);
    Flux<Metrics> findAll();
    Mono<Metrics> updateMetrics(Metrics metrics);
}