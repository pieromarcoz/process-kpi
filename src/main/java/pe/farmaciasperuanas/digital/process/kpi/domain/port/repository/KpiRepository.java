package pe.farmaciasperuanas.digital.process.kpi.domain.port.repository;

import pe.farmaciasperuanas.digital.process.kpi.domain.entity.Kpi;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.LocalDate;
/**
 * Interface for running Spring Boot framework.<br/>
 * <b>Class</b>: Application<br/>
 * <b>Copyright</b>: &copy; 2025 Digital.<br/>
 * <b>Company</b>: Digital.<br/>
 *

 * <u>Developed by</u>: <br/>
 * <ul>
 * <li>Piero Marcos</li>
 * </ul>
 * <u>Changes</u>:<br/>
 * <ul>
 * <li>Feb 28, 2025 KpiRepository Interface.</li>
 * </ul>
 * @version 1.0
 */

public interface KpiRepository {
    Mono<Kpi> save(Kpi kpi);
    Mono<Kpi> findById(String id);
    Flux<Kpi> findByCampaignId(String campaignId);
    Flux<Kpi> findByCampaignIdAndKpiId(String campaignId, String kpiId);
    Mono<Kpi> updateValue(String id, Double value);
    Flux<Kpi> findByProviderId(String providerId);
    Flux<Kpi> findByDateRange(LocalDate startDate, LocalDate endDate);
    Flux<Kpi> findAll();
}
