package pe.farmaciasperuanas.digital.process.kpi.domain.port.repository;

import pe.farmaciasperuanas.digital.process.kpi.domain.entity.SalesforceClicks;
import reactor.core.publisher.Flux;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface SalesforceClicksRepository {
    Flux<SalesforceClicks> findByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    Flux<SalesforceClicks> findBySendId(Integer sendId);
    Flux<SalesforceClicks> findByCorporacion(String corporacion);
    Flux<SalesforceClicks> findByFechaProceso(LocalDate fechaProceso);
}