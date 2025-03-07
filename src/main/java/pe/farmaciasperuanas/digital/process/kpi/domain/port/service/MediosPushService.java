package pe.farmaciasperuanas.digital.process.kpi.domain.port.service;

import reactor.core.publisher.Mono;

import java.time.LocalDate;

public interface MediosPushService {
    /**
     * Procesa los KPIs para push app
     * @param startDate Fecha de inicio
     * @param endDate Fecha de fin
     * @return Mono que completa cuando el proceso termina
     */
    Mono<Void> processPushAppKpis(LocalDate startDate, LocalDate endDate);

    /**
     * Procesa los KPIs para push web
     * @param startDate Fecha de inicio
     * @param endDate Fecha de fin
     * @return Mono que completa cuando el proceso termina
     */
    Mono<Void> processPushWebKpis(LocalDate startDate, LocalDate endDate);
}