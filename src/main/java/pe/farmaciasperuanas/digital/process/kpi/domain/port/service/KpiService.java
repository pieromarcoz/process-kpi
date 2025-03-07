package pe.farmaciasperuanas.digital.process.kpi.domain.port.service;

import reactor.core.publisher.Mono;

import java.time.LocalDate;

public interface KpiService {
    /**
     * Procesa los KPIs para un rango de fechas específico
     * @param startDate Fecha de inicio
     * @param endDate Fecha de fin
     * @return Mono que completa cuando el proceso termina
     */
    Mono<Void> processKpis(LocalDate startDate, LocalDate endDate);

    /**
     * Procesa los KPIs para un proveedor específico
     * @param providerId ID del proveedor
     * @param startDate Fecha de inicio
     * @param endDate Fecha de fin
     * @return Mono que completa cuando el proceso termina
     */
    Mono<Void> processKpisByProvider(String providerId, LocalDate startDate, LocalDate endDate);

    /**
     * Procesa los KPIs para un medio específico (propios, pagados, etc.)
     * @param medium Tipo de medio
     * @param startDate Fecha de inicio
     * @param endDate Fecha de fin
     * @return Mono que completa cuando el proceso termina
     */
    Mono<Void> processKpisByMedium(String medium, LocalDate startDate, LocalDate endDate);

    /**
     * Procesa todos los KPIs pendientes (ejecución programada)
     * @return Mono que completa cuando el proceso termina
     */
    Mono<Void> processAllPendingKpis();
}