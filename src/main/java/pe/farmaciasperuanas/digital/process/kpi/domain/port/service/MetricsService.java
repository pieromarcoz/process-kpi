package pe.farmaciasperuanas.digital.process.kpi.domain.port.service;

import pe.farmaciasperuanas.digital.process.kpi.domain.entity.Metrics;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

public interface MetricsService {
    /**
     * Calcula y actualiza las métricas generales para todos los proveedores
     * @return Mono que completa cuando el proceso termina
     */
    Mono<Void> calculateGeneralMetrics();

    /**
     * Calcula y actualiza las métricas para un proveedor específico
     * @param providerId ID del proveedor
     * @return Mono que completa cuando el proceso termina
     */
    Mono<Void> calculateProviderMetrics(String providerId);

    /**
     * Calcula y actualiza las métricas para un período específico
     * @param startDate Fecha de inicio
     * @param endDate Fecha de fin
     * @return Mono que completa cuando el proceso termina
     */
    Mono<Void> calculateMetricsForPeriod(LocalDate startDate, LocalDate endDate);

    /**
     * Obtiene las métricas para un proveedor específico
     * @param providerId ID del proveedor
     * @return Mono con las métricas del proveedor
     */
    Mono<Metrics> getProviderMetrics(String providerId);

    /**
     * Obtiene las métricas para todos los proveedores
     * @return Flux con las métricas de todos los proveedores
     */
    Flux<Metrics> getAllMetrics();
}