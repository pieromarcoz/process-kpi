package pe.farmaciasperuanas.digital.process.kpi.domain.port.repository;

import pe.farmaciasperuanas.digital.process.kpi.domain.entity.SalesforceSents;
import reactor.core.publisher.Flux;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface SalesforceSentsRepository {
    Flux<SalesforceSents> findByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    Flux<SalesforceSents> findBySendId(Integer sendId);
    Flux<SalesforceSents> findByCorporacion(String corporacion);
    Flux<SalesforceSents> findByFechaProceso(LocalDate fechaProceso);
}