package pe.farmaciasperuanas.digital.process.kpi.infrastructure.outbound.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import pe.farmaciasperuanas.digital.process.kpi.domain.entity.Metrics;
import pe.farmaciasperuanas.digital.process.kpi.domain.port.repository.MetricsRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Repository
@RequiredArgsConstructor
@Slf4j
public class MetricsRepositoryImpl implements MetricsRepository {

    private final ReactiveMongoTemplate reactiveMongoTemplate;

    @Override
    public Mono<Metrics> save(Metrics metrics) {
        if (metrics.getCreatedDate() == null) {
            metrics.setCreatedDate(LocalDateTime.now());
        }
        metrics.setUpdatedDate(LocalDateTime.now());
        return reactiveMongoTemplate.save(metrics);
    }

    @Override
    public Mono<Metrics> findByProviderId(String providerId) {
        Query query = new Query(Criteria.where("providerId").is(providerId));
        return reactiveMongoTemplate.findOne(query, Metrics.class);
    }

    @Override
    public Flux<Metrics> findAll() {
        return reactiveMongoTemplate.findAll(Metrics.class);
    }

    @Override
    public Mono<Metrics> updateMetrics(Metrics metrics) {
        Query query = new Query(Criteria.where("providerId").is(metrics.getProviderId()));
        metrics.setUpdatedDate(LocalDateTime.now());

        // Check if the document exists
        return reactiveMongoTemplate.exists(query, Metrics.class)
                .flatMap(exists -> {
                    if (exists) {
                        // Update existing document
                        Update update = new Update()
                                .set("totalActiveCampaigns", metrics.getTotalActiveCampaigns())
                                .set("totalInvestmentPeriod", metrics.getTotalInvestmentPeriod())
                                .set("totalInvestmentForBrand", metrics.getTotalInvestmentForBrand())
                                .set("ventaTotal", metrics.getVentaTotal())
                                .set("updatedDate", metrics.getUpdatedDate());

                        return reactiveMongoTemplate.findAndModify(query, update, Metrics.class);
                    } else {
                        // Insert new document
                        return reactiveMongoTemplate.save(metrics);
                    }
                });
    }
}