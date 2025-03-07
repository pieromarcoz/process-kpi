package pe.farmaciasperuanas.digital.process.kpi.domain.port.repository;

import pe.farmaciasperuanas.digital.process.kpi.domain.entity.SalesforceOpens;
import reactor.core.publisher.Flux;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface SalesforceOpensRepository {
    Flux<SalesforceOpens> findByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    Flux<SalesforceOpens> findBySendId(Integer sendId);
    Flux<SalesforceOpens> findByCorporacion(String corporacion);
    Flux<SalesforceOpens> findByFechaProceso(LocalDate fechaProceso);
}