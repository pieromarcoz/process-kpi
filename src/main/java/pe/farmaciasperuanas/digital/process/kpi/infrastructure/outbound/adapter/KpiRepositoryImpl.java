package pe.farmaciasperuanas.digital.process.kpi.infrastructure.outbound.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import pe.farmaciasperuanas.digital.process.kpi.domain.entity.Kpi;
import pe.farmaciasperuanas.digital.process.kpi.domain.port.repository.KpiRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Repository
@RequiredArgsConstructor
@Slf4j
public class KpiRepositoryImpl implements KpiRepository {

    private final ReactiveMongoTemplate reactiveMongoTemplate;

    @Override
    public Mono<Kpi> save(Kpi kpi) {
        if (kpi.getCreatedDate() == null) {
            kpi.setCreatedDate(LocalDateTime.now());
        }
        kpi.setUpdatedDate(LocalDateTime.now());
        return reactiveMongoTemplate.save(kpi);
    }

    @Override
    public Mono<Kpi> findById(String id) {
        return reactiveMongoTemplate.findById(id, Kpi.class);
    }

    @Override
    public Flux<Kpi> findByCampaignId(String campaignId) {
        Query query = new Query(Criteria.where("campaignId").is(campaignId));
        return reactiveMongoTemplate.find(query, Kpi.class);
    }

    @Override
    public Flux<Kpi> findByCampaignIdAndKpiId(String campaignId, String kpiId) {
        Query query = new Query(Criteria.where("campaignId").is(campaignId)
                .and("kpiId").is(kpiId));
        return reactiveMongoTemplate.find(query, Kpi.class);
    }

    @Override
    public Mono<Kpi> updateValue(String id, Double value) {
        Query query = new Query(Criteria.where("id").is(id));
        Update update = new Update()
                .set("value", value)
                .set("updatedDate", LocalDateTime.now());
        return reactiveMongoTemplate.findAndModify(query, update, Kpi.class);
    }

    @Override
    public Flux<Kpi> findByProviderId(String providerId) {
        Query query = new Query(Criteria.where("providerId").is(providerId));
        return reactiveMongoTemplate.find(query, Kpi.class);
    }

    @Override
    public Flux<Kpi> findByDateRange(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        Query query = new Query(Criteria.where("createdDate")
                .gte(startDateTime)
                .lte(endDateTime));

        return reactiveMongoTemplate.find(query, Kpi.class);
    }
    @Override
    public Flux<Kpi> findAll() {
        return reactiveMongoTemplate.findAll(Kpi.class);
    }
}