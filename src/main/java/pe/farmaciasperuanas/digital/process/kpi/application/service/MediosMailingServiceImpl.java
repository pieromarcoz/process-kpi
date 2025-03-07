package pe.farmaciasperuanas.digital.process.kpi.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.farmaciasperuanas.digital.process.kpi.domain.entity.Kpi;
import pe.farmaciasperuanas.digital.process.kpi.domain.port.repository.KpiRepository;
import pe.farmaciasperuanas.digital.process.kpi.domain.port.repository.SalesforceClicksRepository;
import pe.farmaciasperuanas.digital.process.kpi.domain.port.repository.SalesforceOpensRepository;
import pe.farmaciasperuanas.digital.process.kpi.domain.port.repository.SalesforceSentsRepository;
import pe.farmaciasperuanas.digital.process.kpi.domain.port.service.MediosMailingService;
import pe.farmaciasperuanas.digital.process.kpi.domain.port.service.UtmParserService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class MediosMailingServiceImpl implements MediosMailingService {

    private final SalesforceClicksRepository salesforceClicksRepository;
    private final SalesforceOpensRepository salesforceOpensRepository;
    private final SalesforceSentsRepository salesforceSentsRepository;
    private final KpiRepository kpiRepository;
    private final UtmParserService utmParserService;

    @Override
    public Mono<Void> processMailingPadreKpis(LocalDate startDate, LocalDate endDate) {
        log.info("Procesando KPIs de Mailing Padre para el periodo: {} - {}", startDate, endDate);

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        Map<String, Integer> opensCountBySendId = new ConcurrentHashMap<>();
        Map<String, Integer> clicksCountBySendId = new ConcurrentHashMap<>();
        Map<String, Integer> sentsCountBySendId = new ConcurrentHashMap<>();

        // 1. Obtener aperturas (MP-I: Impresiones/Aperturas)
        return salesforceOpensRepository.findByDateRange(startDateTime, endDateTime)
                .groupBy(open -> open.getSendID().toString())
                .flatMap(group -> {
                    String sendId = group.key();
                    return group.count()
                            .doOnNext(count -> opensCountBySendId.put(sendId, count.intValue()));
                })
                .then()

                // 2. Obtener clics (MP-C: Clics)
                .then(Mono.defer(() ->
                        salesforceClicksRepository.findByDateRange(startDateTime, endDateTime)
                                .groupBy(click -> click.getSendID().toString())
                                .flatMap(group -> {
                                    String sendId = group.key();
                                    return group.count()
                                            .doOnNext(count -> clicksCountBySendId.put(sendId, count.intValue()));
                                })
                                .then()
                ))

                // 3. Obtener envíos (MP-A: Alcance/Envíos)
                .then(Mono.defer(() ->
                        salesforceSentsRepository.findByDateRange(startDateTime, endDateTime)
                                .groupBy(sent -> sent.getSendID().toString())
                                .flatMap(group -> {
                                    String sendId = group.key();
                                    return group.count()
                                            .doOnNext(count -> sentsCountBySendId.put(sendId, count.intValue()));
                                })
                                .then()
                ))

                // 4. Procesar y guardar KPIs
                .then(Mono.defer(() ->
                        Flux.fromIterable(opensCountBySendId.keySet())
                                .flatMap(sendId ->
                                        utmParserService.extractCampaignIdFromSendId(Integer.parseInt(sendId))
                                                .flatMap(campaignId -> {
                                                    int opens = opensCountBySendId.getOrDefault(sendId, 0);
                                                    int clicks = clicksCountBySendId.getOrDefault(sendId, 0);
                                                    int sents = sentsCountBySendId.getOrDefault(sendId, 0);

                                                    // Calcular Open Rate (MP-OR)
                                                    double openRate = sents > 0 ? (double) opens / sents * 100 : 0;

                                                    // Calcular CTR (MP-CR)
                                                    double ctr = opens > 0 ? (double) clicks / opens * 100 : 0;

                                                    // Guardar KPI de aperturas (MP-I)
                                                    Kpi opensKpi = Kpi.builder()
                                                            .campaignId(campaignId)
                                                            .kpiId("MP-I")
                                                            .kpiDescription("Impresiones (Aperturas)")
                                                            .type("Cantidad")
                                                            .value((double) opens)
                                                            .status("A")
                                                            .build();

                                                    // Guardar KPI de envíos (MP-A)
                                                    Kpi sentsKpi = Kpi.builder()
                                                            .campaignId(campaignId)
                                                            .kpiId("MP-A")
                                                            .kpiDescription("Alcance (Envíos)")
                                                            .type("Cantidad")
                                                            .value((double) sents)
                                                            .status("A")
                                                            .build();

                                                    // Guardar KPI de clics (MP-C)
                                                    Kpi clicksKpi = Kpi.builder()
                                                            .campaignId(campaignId)
                                                            .kpiId("MP-C")
                                                            .kpiDescription("Clics")
                                                            .type("Cantidad")
                                                            .value((double) clicks)
                                                            .status("A")
                                                            .build();

                                                    // Guardar KPI de Open Rate (MP-OR)
                                                    Kpi openRateKpi = Kpi.builder()
                                                            .campaignId(campaignId)
                                                            .kpiId("MP-OR")
                                                            .kpiDescription("Open Rate (OR)")
                                                            .type("Porcentaje")
                                                            .value(openRate)
                                                            .status("A")
                                                            .build();

                                                    // Guardar KPI de CTR (MP-CR)
                                                    Kpi ctrKpi = Kpi.builder()
                                                            .campaignId(campaignId)
                                                            .kpiId("MP-CR")
                                                            .kpiDescription("CTR (CR)")
                                                            .type("Porcentaje")
                                                            .value(ctr)
                                                            .status("A")
                                                            .build();

                                                    return Mono.when(
                                                            kpiRepository.save(opensKpi),
                                                            kpiRepository.save(sentsKpi),
                                                            kpiRepository.save(clicksKpi),
                                                            kpiRepository.save(openRateKpi),
                                                            kpiRepository.save(ctrKpi)
                                                    );
                                                })
                                                .onErrorResume(e -> {
                                                    log.error("Error procesando KPIs para sendId: {}", sendId, e);
                                                    return Mono.empty();
                                                })
                                )
                                .then()
                ))
                .doOnSuccess(v -> log.info("Procesamiento de KPIs de Mailing Padre completado"))
                .doOnError(e -> log.error("Error al procesar KPIs de Mailing Padre", e));
    }

    @Override
    public Mono<Void> processMailingCabeceraKpis(LocalDate startDate, LocalDate endDate) {
        log.info("Procesando KPIs de Mailing Cabecera para el periodo: {} - {}", startDate, endDate);

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        // Buscamos los clics en mailing cabecera analizando los utm_campaign en las URLs
        return salesforceClicksRepository.findByDateRange(startDateTime, endDateTime)
                .filter(click -> click.getUrl() != null && click.getUrl().contains("utm_campaign"))
                .flatMap(click ->
                        utmParserService.extractMetadataFromUrl(click.getUrl())
                                .filter(metadata -> "MC".equals(metadata.getFormat()))
                                .map(metadata -> {
                                    // Agrupamos por campaignSubId
                                    return Map.entry(metadata.getCampaignSubId(), click);
                                })
                )
                .groupBy(Map.Entry::getKey)
                .flatMap(group -> {
                    String campaignSubId = group.key();
                    return group.count()
                            .flatMap(clickCount -> {
                                // Guardar KPI de clics (MCC)
                                Kpi clicksKpi = Kpi.builder()
                                        .campaignSubId(campaignSubId)
                                        .kpiId("MCC")
                                        .kpiDescription("Clics")
                                        .type("Cantidad")
                                        .value(clickCount.doubleValue())
                                        .status("A")
                                        .build();

                                return kpiRepository.save(clicksKpi);
                            });
                })
                .then()
                .doOnSuccess(v -> log.info("Procesamiento de KPIs de Mailing Cabecera completado"))
                .doOnError(e -> log.error("Error al procesar KPIs de Mailing Cabecera", e));
    }

    @Override
    public Mono<Void> processMailingFeedKpis(LocalDate startDate, LocalDate endDate) {
        log.info("Procesando KPIs de Mailing Feed para el periodo: {} - {}", startDate, endDate);

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        // Similar a Mailing Cabecera pero para formato MF
        return salesforceClicksRepository.findByDateRange(startDateTime, endDateTime)
                .filter(click -> click.getUrl() != null && click.getUrl().contains("utm_campaign"))
                .flatMap(click ->
                        utmParserService.extractMetadataFromUrl(click.getUrl())
                                .filter(metadata -> "MF".equals(metadata.getFormat()))
                                .map(metadata -> {
                                    // Agrupamos por campaignSubId
                                    return Map.entry(metadata.getCampaignSubId(), click);
                                })
                )
                .groupBy(Map.Entry::getKey)
                .flatMap(group -> {
                    String campaignSubId = group.key();
                    return group.count()
                            .flatMap(clickCount -> {
                                // Guardar KPI de clics (MFC)
                                Kpi clicksKpi = Kpi.builder()
                                        .campaignSubId(campaignSubId)
                                        .kpiId("MFC")
                                        .kpiDescription("Clics")
                                        .type("Cantidad")
                                        .value(clickCount.doubleValue())
                                        .status("A")
                                        .build();

                                return kpiRepository.save(clicksKpi);
                            });
                })
                .then()
                .doOnSuccess(v -> log.info("Procesamiento de KPIs de Mailing Feed completado"))
                .doOnError(e -> log.error("Error al procesar KPIs de Mailing Feed", e));
    }

    @Override
    public Mono<Void> processMailingBodyKpis(LocalDate startDate, LocalDate endDate) {
        log.info("Procesando KPIs de Mailing Body para el periodo: {} - {}", startDate, endDate);

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        // Similar a Mailing Cabecera pero para formato MB
        return salesforceClicksRepository.findByDateRange(startDateTime, endDateTime)
                .filter(click -> click.getUrl() != null && click.getUrl().contains("utm_campaign"))
                .flatMap(click ->
                        utmParserService.extractMetadataFromUrl(click.getUrl())
                                .filter(metadata -> "MB".equals(metadata.getFormat()))
                                .map(metadata -> {
                                    // Agrupamos por campaignSubId
                                    return Map.entry(metadata.getCampaignSubId(), click);
                                })
                )
                .groupBy(Map.Entry::getKey)
                .flatMap(group -> {
                    String campaignSubId = group.key();
                    return group.count()
                            .flatMap(clickCount -> {
                                // Guardar KPI de clics (MBC)
                                Kpi clicksKpi = Kpi.builder()
                                        .campaignSubId(campaignSubId)
                                        .kpiId("MBC")
                                        .kpiDescription("Clics")
                                        .type("Cantidad")
                                        .value(clickCount.doubleValue())
                                        .status("A")
                                        .build();

                                return kpiRepository.save(clicksKpi);
                            });
                })
                .then()
                .doOnSuccess(v -> log.info("Procesamiento de KPIs de Mailing Body completado"))
                .doOnError(e -> log.error("Error al procesar KPIs de Mailing Body", e));
    }
}