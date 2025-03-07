package pe.farmaciasperuanas.digital.process.kpi.infrastructure.outbound.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import pe.farmaciasperuanas.digital.process.kpi.domain.entity.SalesforceClicks;
import pe.farmaciasperuanas.digital.process.kpi.domain.port.repository.SalesforceClicksRepository;
import reactor.core.publisher.Flux;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Repository
@RequiredArgsConstructor
@Slf4j
public class SalesforceClicksRepositoryImpl implements SalesforceClicksRepository {

    private final ReactiveMongoTemplate reactiveMongoTemplate;

    @Override
    public Flux<SalesforceClicks> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        Query query = new Query(Criteria.where("eventDate")
                .gte(startDate)
                .lte(endDate));
        return reactiveMongoTemplate.find(query, SalesforceClicks.class);
    }

    @Override
    public Flux<SalesforceClicks> findBySendId(Integer sendId) {
        Query query = new Query(Criteria.where("sendID").is(sendId));
        return reactiveMongoTemplate.find(query, SalesforceClicks.class);
    }

    @Override
    public Flux<SalesforceClicks> findByCorporacion(String corporacion) {
        Query query = new Query(Criteria.where("corporacion").is(corporacion));
        return reactiveMongoTemplate.find(query, SalesforceClicks.class);
    }

    @Override
    public Flux<SalesforceClicks> findByFechaProceso(LocalDate fechaProceso) {
        Query query = new Query(Criteria.where("fechaProceso").is(fechaProceso));
        return reactiveMongoTemplate.find(query, SalesforceClicks.class);
    }
}