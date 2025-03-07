// src/main/java/pe/farmaciasperuanas/digital/process/kpi/application/service/MediosPushServiceImpl.java
package pe.farmaciasperuanas.digital.process.kpi.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import pe.farmaciasperuanas.digital.process.kpi.domain.entity.Kpi;
import pe.farmaciasperuanas.digital.process.kpi.domain.entity.SalesforcePush;
import pe.farmaciasperuanas.digital.process.kpi.domain.port.repository.KpiRepository;
import pe.farmaciasperuanas.digital.process.kpi.domain.port.service.MediosPushService;
import pe.farmaciasperuanas.digital.process.kpi.domain.port.service.UtmParserService;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class MediosPushServiceImpl implements MediosPushService {

    private final ReactiveMongoTemplate reactiveMongoTemplate;
    private final KpiRepository kpiRepository;
    private final UtmParserService utmParserService;

    @Override
    public Mono<Void> processPushAppKpis(LocalDate startDate, LocalDate endDate) {
        log.info("Procesando KPIs de Push App para el periodo: {} - {}", startDate, endDate);

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // Crear consulta para buscar datos de push app en el rango de fechas
        Query query = new Query(Criteria.where("FechaProceso")
                .gte(startDate.format(formatter))
                .lte(endDate.format(formatter))
                .and("AppName").exists(true));

        Map<String, Integer> sentCountByMessageName = new HashMap<>();
        Map<String, Integer> openedCountByMessageName = new HashMap<>();

        // 1. Obtener y agrupar datos de push
        return reactiveMongoTemplate.find(query, SalesforcePush.class, "salesforce_push")
                .doOnNext(push -> log.debug("Procesando push: {}", push.getMessageName()))
                .groupBy(SalesforcePush::getMessageName)
                .flatMap(group -> {
                    String messageName = group.key();

                    return group.collectList()
                            .flatMap(pushList -> {
                                int sentCount = pushList.size();
                                int openedCount = (int) pushList.stream()
                                        .filter(push -> push.getMessageOpened() != null && push.getMessageOpened())
                                        .count();

                                sentCountByMessageName.put(messageName, sentCount);
                                openedCountByMessageName.put(messageName, openedCount);

                                // Extraer campaignId del nombre del mensaje
                                return extractCampaignIdFromMessageName(messageName)
                                        .map(campaignId -> Map.entry(campaignId, messageName));
                            });
                })
                .flatMap(entry -> {
                    String campaignId = entry.getKey();
                    String messageName = entry.getValue();

                    // Obtener conteos para este mensaje
                    int sentCount = sentCountByMessageName.getOrDefault(messageName, 0);
                    int openedCount = openedCountByMessageName.getOrDefault(messageName, 0);

                    // Calcular tasa de apertura
                    double openRate = sentCount > 0 ? (double) openedCount / sentCount * 100 : 0;

                    // Crear KPIs
                    Kpi sentKpi = Kpi.builder()
                            .campaignId(campaignId)
                            .kpiId("PA-A")
                            .kpiDescription("Alcance (Envíos)")
                            .type("Cantidad")
                            .value((double) sentCount)
                            .status("A")
                            .build();

                    Kpi openedKpi = Kpi.builder()
                            .campaignId(campaignId)
                            .kpiId("PA-I")
                            .kpiDescription("Impresiones (Aperturas)")
                            .type("Cantidad")
                            .value((double) openedCount)
                            .status("A")
                            .build();

                    Kpi openRateKpi = Kpi.builder()
                            .campaignId(campaignId)
                            .kpiId("PA-OR")
                            .kpiDescription("Open Rate (OR)")
                            .type("Porcentaje")
                            .value(openRate)
                            .status("A")
                            .build();

                    // Guardar KPIs
                    return Mono.when(
                            kpiRepository.save(sentKpi),
                            kpiRepository.save(openedKpi),
                            kpiRepository.save(openRateKpi)
                    );
                })
                .then()
                .doOnSuccess(v -> log.info("Procesamiento de KPIs de Push App completado"))
                .doOnError(e -> log.error("Error al procesar KPIs de Push App", e));
    }

    @Override
    public Mono<Void> processPushWebKpis(LocalDate startDate, LocalDate endDate) {
        log.info("Procesando KPIs de Push Web para el periodo: {} - {}", startDate, endDate);

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // Crear consulta para buscar datos de push web en el rango de fechas
        Query query = new Query(Criteria.where("FechaProceso")
                .gte(startDate.format(formatter))
                .lte(endDate.format(formatter))
                .and("MessageName").regex(".*web.*", "i")); // Filtro básico para push web

        Map<String, Integer> sentCountByMessageName = new HashMap<>();
        Map<String, Integer> openedCountByMessageName = new HashMap<>();

        return reactiveMongoTemplate.find(query, SalesforcePush.class, "salesforce_push")
                .doOnNext(push -> log.debug("Procesando push web: {}", push.getMessageName()))
                .groupBy(SalesforcePush::getMessageName)
                .flatMap(group -> {
                    String messageName = group.key();

                    return group.collectList()
                            .flatMap(pushList -> {
                                int sentCount = pushList.size();
                                int openedCount = (int) pushList.stream()
                                        .filter(push -> push.getMessageOpened() != null && push.getMessageOpened())
                                        .count();

                                sentCountByMessageName.put(messageName, sentCount);
                                openedCountByMessageName.put(messageName, openedCount);

                                // Extraer campaignId del nombre del mensaje
                                return extractCampaignIdFromMessageName(messageName)
                                        .map(campaignId -> Map.entry(campaignId, messageName));
                            });
                })
                .flatMap(entry -> {
                    String campaignId = entry.getKey();
                    String messageName = entry.getValue();

                    // Obtener conteos para este mensaje
                    int sentCount = sentCountByMessageName.getOrDefault(messageName, 0);
                    int openedCount = openedCountByMessageName.getOrDefault(messageName, 0);

                    // Calcular tasa de apertura
                    double openRate = sentCount > 0 ? (double) openedCount / sentCount * 100 : 0;

                    // Crear KPIs
                    Kpi sentKpi = Kpi.builder()
                            .campaignId(campaignId)
                            .kpiId("PW-A")
                            .kpiDescription("Alcance (Envíos)")
                            .type("Cantidad")
                            .value((double) sentCount)
                            .status("A")
                            .build();

                    Kpi openedKpi = Kpi.builder()
                            .campaignId(campaignId)
                            .kpiId("PW-I")
                            .kpiDescription("Impresiones (Aperturas)")
                            .type("Cantidad")
                            .value((double) openedCount)
                            .status("A")
                            .build();

                    Kpi openRateKpi = Kpi.builder()
                            .campaignId(campaignId)
                            .kpiId("PW-OR")
                            .kpiDescription("Open Rate (OR)")
                            .type("Porcentaje")
                            .value(openRate)
                            .status("A")
                            .build();

                    // Guardar KPIs
                    return Mono.when(
                            kpiRepository.save(sentKpi),
                            kpiRepository.save(openedKpi),
                            kpiRepository.save(openRateKpi)
                    );
                })
                .then()
                .doOnSuccess(v -> log.info("Procesamiento de KPIs de Push Web completado"))
                .doOnError(e -> log.error("Error al procesar KPIs de Push Web", e));
    }

    private Mono<String> extractCampaignIdFromMessageName(String messageName) {
        // Ejemplo de formato:
        // 20250305_sfmc_cindi_do_mifarma_std_compra_pautareg_wellness_app_abierto_varios_omega3_push
        if (messageName == null || messageName.isEmpty()) {
            return Mono.empty();
        }

        String[] parts = messageName.split("_");
        if (parts.length > 0) {
            // El campaignId suele estar al inicio (fecha YYYYMMDD)
            return Mono.just(parts[0]);
        }

        return Mono.empty();
    }
}