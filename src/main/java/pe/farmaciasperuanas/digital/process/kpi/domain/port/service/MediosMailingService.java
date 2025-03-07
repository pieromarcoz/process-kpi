package pe.farmaciasperuanas.digital.process.kpi.domain.port.service;

import reactor.core.publisher.Mono;

import java.time.LocalDate;

public interface MediosMailingService {
    /**
     * Procesa los KPIs para mailing padre
     * @param startDate Fecha de inicio
     * @param endDate Fecha de fin
     * @return Mono que completa cuando el proceso termina
     */
    Mono<Void> processMailingPadreKpis(LocalDate startDate, LocalDate endDate);

    /**
     * Procesa los KPIs para mailing cabecera
     * @param startDate Fecha de inicio
     * @param endDate Fecha de fin
     * @return Mono que completa cuando el proceso termina
     */
    Mono<Void> processMailingCabeceraKpis(LocalDate startDate, LocalDate endDate);

    /**
     * Procesa los KPIs para mailing feed
     * @param startDate Fecha de inicio
     * @param endDate Fecha de fin
     * @return Mono que completa cuando el proceso termina
     */
    Mono<Void> processMailingFeedKpis(LocalDate startDate, LocalDate endDate);

    /**
     * Procesa los KPIs para mailing body
     * @param startDate Fecha de inicio
     * @param endDate Fecha de fin
     * @return Mono que completa cuando el proceso termina
     */
    Mono<Void> processMailingBodyKpis(LocalDate startDate, LocalDate endDate);
}