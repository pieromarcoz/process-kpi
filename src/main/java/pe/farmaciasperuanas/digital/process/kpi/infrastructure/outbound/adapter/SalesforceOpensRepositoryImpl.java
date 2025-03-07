package pe.farmaciasperuanas.digital.process.kpi.infrastructure.outbound.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import pe.farmaciasperuanas.digital.process.kpi.domain.entity.SalesforceOpens;
import pe.farmaciasperuanas.digital.process.kpi.domain.port.repository.SalesforceOpensRepository;
import reactor.core.publisher.Flux;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Repository
@RequiredArgsConstructor
@Slf4j
public class SalesforceOpensRepositoryImpl implements SalesforceOpensRepository {

    private final ReactiveMongoTemplate reactiveMongoTemplate;

    @Override
    public Flux<SalesforceOpens> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        Query query = new Query(Criteria.where("eventDate")
                .gte(startDate)
                .lte(endDate));
        return reactiveMongoTemplate.find(query, SalesforceOpens.class);
    }

    @Override
    public Flux<SalesforceOpens> findBySendId(Integer sendId) {
        Query query = new Query(Criteria.where("sendID").is(sendId));
        return reactiveMongoTemplate.find(query, SalesforceOpens.class);
    }

    @Override
    public Flux<SalesforceOpens> findByCorporacion(String corporacion) {
        Query query = new Query(Criteria.where("corporacion").is(corporacion));
        return reactiveMongoTemplate.find(query, SalesforceOpens.class);
    }

    @Override
    public Flux<SalesforceOpens> findByFechaProceso(LocalDate fechaProceso) {
        Query query = new Query(Criteria.where("fechaProceso").is(fechaProceso));
        return reactiveMongoTemplate.find(query, SalesforceOpens.class);
    }
}