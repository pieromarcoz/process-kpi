// src/main/java/pe/farmaciasperuanas/digital/process/kpi/application/service/KpiServiceImpl.java
package pe.farmaciasperuanas.digital.process.kpi.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pe.farmaciasperuanas.digital.process.kpi.domain.port.service.KpiService;
import pe.farmaciasperuanas.digital.process.kpi.domain.port.service.MediosMailingService;
import pe.farmaciasperuanas.digital.process.kpi.domain.port.service.MediosPushService;
import pe.farmaciasperuanas.digital.process.kpi.domain.port.service.MetricsService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class KpiServiceImpl implements KpiService {

    private final MediosMailingService mediosMailingService;
    private final MediosPushService mediosPushService;
    private final MetricsService metricsService;

    @Value("${kpi.batch.size:3}")
    private int batchSize; // Número de días a procesar por lote

    @Value("${kpi.batch.delay:500}")
    private int batchDelayMillis; // Delay entre lotes para evitar sobrecarga

    @Override
    public Mono<Void> processKpis(LocalDate startDate, LocalDate endDate) {
        log.info("Iniciando procesamiento de KPIs para el periodo: {} - {}", startDate, endDate);

        // Validar fechas
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            log.error("Fechas no válidas para procesamiento: start={}, end={}", startDate, endDate);
            return Mono.error(new IllegalArgumentException("Fechas no válidas"));
        }

        // Dividir el rango en días individuales
        List<LocalDate> dates = getDatesInRange(startDate, endDate);
        int totalDays = dates.size();

        log.info("Procesando {} días en lotes de {}", totalDays, batchSize);

        // Agrupar días en lotes
        List<List<LocalDate>> batches = new ArrayList<>();
        for (int i = 0; i < dates.size(); i += batchSize) {
            int end = Math.min(i + batchSize, dates.size());
            batches.add(dates.subList(i, end));
        }

        AtomicInteger processedBatches = new AtomicInteger(0);
        int totalBatches = batches.size();

        // Procesar cada lote secuencialmente
        return Flux.fromIterable(batches)
                .concatMap(batch -> {
                    LocalDate batchStart = batch.get(0);
                    LocalDate batchEnd = batch.get(batch.size() - 1);
                    int currentBatch = processedBatches.incrementAndGet();

                    log.info("Procesando lote {}/{}: {} - {}",
                            currentBatch, totalBatches, batchStart, batchEnd);

                    return processBatch(batchStart, batchEnd)
                            .doOnSuccess(v -> log.info("Lote {}/{} completado", currentBatch, totalBatches))
                            .onErrorResume(e -> {
                                log.error("Error procesando lote {}/{}: {}",
                                        currentBatch, totalBatches, e.getMessage());
                                return Mono.empty();
                            })
                            // Añadir delay entre lotes para reducir carga
                            .delayElement(Duration.ofMillis(batchDelayMillis));
                })
                .then(metricsService.calculateGeneralMetrics())
                .doOnSuccess(v -> log.info("Procesamiento de todos los lotes completado"));
    }

    private Mono<Void> processBatch(LocalDate startDate, LocalDate endDate) {
        return Mono.when(
                processMediosPropiosKpis(startDate, endDate),
                // Aquí se añadirían los procesamientos para otros tipos de medios
                metricsService.calculateMetricsForPeriod(startDate, endDate)
        );
    }

    @Override
    public Mono<Void> processKpisByProvider(String providerId, LocalDate startDate, LocalDate endDate) {
        log.info("Iniciando procesamiento de KPIs para el proveedor: {} y periodo: {} - {}",
                providerId, startDate, endDate);

        // Dividir el rango en días individuales
        List<LocalDate> dates = getDatesInRange(startDate, endDate);
        int totalDays = dates.size();

        log.info("Procesando {} días en lotes de {} para proveedor {}",
                totalDays, batchSize, providerId);

        // Agrupar días en lotes
        List<List<LocalDate>> batches = new ArrayList<>();
        for (int i = 0; i < dates.size(); i += batchSize) {
            int end = Math.min(i + batchSize, dates.size());
            batches.add(dates.subList(i, end));
        }

        AtomicInteger processedBatches = new AtomicInteger(0);
        int totalBatches = batches.size();

        return Flux.fromIterable(batches)
                .concatMap(batch -> {
                    LocalDate batchStart = batch.get(0);
                    LocalDate batchEnd = batch.get(batch.size() - 1);
                    int currentBatch = processedBatches.incrementAndGet();

                    log.info("Procesando lote {}/{} para proveedor {}: {} - {}",
                            currentBatch, totalBatches, providerId, batchStart, batchEnd);

                    return processProviderBatch(providerId, batchStart, batchEnd)
                            .doOnSuccess(v -> log.info("Lote {}/{} para proveedor {} completado",
                                    currentBatch, totalBatches, providerId))
                            .onErrorResume(e -> {
                                log.error("Error procesando lote {}/{} para proveedor {}: {}",
                                        currentBatch, totalBatches, providerId, e.getMessage());
                                return Mono.empty();
                            })
                            .delayElement(Duration.ofMillis(batchDelayMillis));
                })
                .then(metricsService.calculateProviderMetrics(providerId))
                .doOnSuccess(v -> log.info("Procesamiento para proveedor {} completado", providerId));
    }

    private Mono<Void> processProviderBatch(String providerId, LocalDate startDate, LocalDate endDate) {
        return processMediosPropiosKpis(startDate, endDate)
                .then(Mono.defer(() -> metricsService.calculateMetricsForPeriod(startDate, endDate)));
    }

    @Override
    public Mono<Void> processKpisByMedium(String medium, LocalDate startDate, LocalDate endDate) {
        log.info("Iniciando procesamiento de KPIs para el medio: {} y periodo: {} - {}",
                medium, startDate, endDate);

        // Similar implementación por lotes
        List<LocalDate> dates = getDatesInRange(startDate, endDate);

        // Agrupar días en lotes
        List<List<LocalDate>> batches = createBatches(dates);

        AtomicInteger processedBatches = new AtomicInteger(0);
        int totalBatches = batches.size();

        return Flux.fromIterable(batches)
                .concatMap(batch -> {
                    LocalDate batchStart = batch.get(0);
                    LocalDate batchEnd = batch.get(batch.size() - 1);
                    int currentBatch = processedBatches.incrementAndGet();

                    log.info("Procesando lote {}/{} para medio {}: {} - {}",
                            currentBatch, totalBatches, medium, batchStart, batchEnd);

                    return processMediumBatch(medium, batchStart, batchEnd)
                            .doOnSuccess(v -> log.info("Lote {}/{} para medio {} completado",
                                    currentBatch, totalBatches, medium))
                            .onErrorResume(e -> {
                                log.error("Error procesando lote {}/{} para medio {}: {}",
                                        currentBatch, totalBatches, medium, e.getMessage());
                                return Mono.empty();
                            })
                            .delayElement(Duration.ofMillis(batchDelayMillis));
                })
                .then()
                .doOnSuccess(v -> log.info("Procesamiento para medio {} completado", medium));
    }

    private Mono<Void> processMediumBatch(String medium, LocalDate startDate, LocalDate endDate) {
        if ("medio propios".equals(medium)) {
            return processMediosPropiosKpis(startDate, endDate);
        } else if ("medios pagados".equals(medium)) {
            // Implementación para medios pagados
            log.warn("Procesamiento de KPIs para medios pagados aún no implementado");
            return Mono.empty();
        } else if ("on site".equals(medium)) {
            // Implementación para medios on site
            log.warn("Procesamiento de KPIs para medios on site aún no implementado");
            return Mono.empty();
        } else {
            log.warn("Medio no reconocido: {}", medium);
            return Mono.empty();
        }
    }

    @Override
    public Mono<Void> processAllPendingKpis() {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(7); // Por defecto procesamos la última semana

        log.info("Iniciando procesamiento automático de KPIs para el periodo: {} - {}",
                startDate, endDate);

        return processKpis(startDate, endDate);
    }

    private Mono<Void> processMediosPropiosKpis(LocalDate startDate, LocalDate endDate) {
        log.info("Procesando KPIs de medios propios para periodo: {} - {}", startDate, endDate);

        return Mono.when(
                        // Procesamiento paralelo para mejorar rendimiento
                        Flux.merge(
                                mediosMailingService.processMailingPadreKpis(startDate, endDate)
                                        .subscribeOn(Schedulers.parallel()),
                                mediosMailingService.processMailingCabeceraKpis(startDate, endDate)
                                        .subscribeOn(Schedulers.parallel()),
                                mediosMailingService.processMailingFeedKpis(startDate, endDate)
                                        .subscribeOn(Schedulers.parallel()),
                                mediosMailingService.processMailingBodyKpis(startDate, endDate)
                                        .subscribeOn(Schedulers.parallel()),
                                mediosPushService.processPushAppKpis(startDate, endDate)
                                        .subscribeOn(Schedulers.parallel()),
                                mediosPushService.processPushWebKpis(startDate, endDate)
                                        .subscribeOn(Schedulers.parallel())
                        ).then()
                )
                .doOnSuccess(v -> log.info("Procesamiento de KPIs de medios propios completado para periodo: {} - {}",
                        startDate, endDate));
    }

    // Método auxiliar para obtener la lista de fechas en un rango
    private List<LocalDate> getDatesInRange(LocalDate startDate, LocalDate endDate) {
        List<LocalDate> dates = new ArrayList<>();
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            dates.add(currentDate);
            currentDate = currentDate.plusDays(1);
        }
        return dates;
    }

    // Método auxiliar para dividir una lista en lotes
    private List<List<LocalDate>> createBatches(List<LocalDate> dates) {
        List<List<LocalDate>> batches = new ArrayList<>();
        for (int i = 0; i < dates.size(); i += batchSize) {
            int end = Math.min(i + batchSize, dates.size());
            batches.add(dates.subList(i, end));
        }
        return batches;
    }
}