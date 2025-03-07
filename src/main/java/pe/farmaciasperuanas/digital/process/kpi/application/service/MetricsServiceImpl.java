// src/main/java/pe/farmaciasperuanas/digital/process/kpi/application/service/MetricsServiceImpl.java
package pe.farmaciasperuanas.digital.process.kpi.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.farmaciasperuanas.digital.process.kpi.domain.entity.Kpi;
import pe.farmaciasperuanas.digital.process.kpi.domain.entity.Metrics;
import pe.farmaciasperuanas.digital.process.kpi.domain.port.repository.KpiRepository;
import pe.farmaciasperuanas.digital.process.kpi.domain.port.repository.MetricsRepository;
import pe.farmaciasperuanas.digital.process.kpi.domain.port.service.MetricsService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class MetricsServiceImpl implements MetricsService {

    private final KpiRepository kpiRepository;
    private final MetricsRepository metricsRepository;
    @Override
    public Flux<Metrics> getAllMetrics() {
        log.info("Obteniendo todas las métricas");
        return metricsRepository.findAll()
                .doOnComplete(() -> log.info("Recuperación de métricas completada"))
                .doOnError(e -> log.error("Error al recuperar métricas", e));
    }
    @Override
    public Mono<Void> calculateGeneralMetrics() {
        log.info("Calculando métricas generales");

        Map<String, Map<String, Object>> metricsMap = new HashMap<>();

        return kpiRepository.findAll()
                .filter(kpi -> kpi.getProviderId() != null && !kpi.getProviderId().isEmpty())
                .groupBy(kpi -> kpi.getProviderId())
                .flatMap(group -> {
                    String providerId = group.key();

                    // Inicializar métricas para este proveedor
                    metricsMap.putIfAbsent(providerId, new HashMap<>());
                    Map<String, Object> providerMetrics = metricsMap.get(providerId);

                    return group.collectList()
                            .map(kpis -> {
                                // Contar campañas activas (por campaignId único)
                                long activeCampaigns = kpis.stream()
                                        .map(kpi -> kpi.getCampaignId())
                                        .filter(id -> id != null && !id.isEmpty())
                                        .distinct()
                                        .count();

                                // Cálculo de inversión total (suma de investment de KPIs)
                                double totalInvestment = calculateTotalInvestment(kpis);

                                // Cálculo de inversión por marca (promedio por campaña)
                                double investmentPerBrand = activeCampaigns > 0 ?
                                        totalInvestment / activeCampaigns : 0;

                                // Cálculo de ventas totales (suma de KPIs de venta)
                                double totalSales = calculateTotalSales(kpis);

                                providerMetrics.put("totalActiveCampaigns", (int) activeCampaigns);
                                providerMetrics.put("totalInvestmentPeriod", totalInvestment);
                                providerMetrics.put("totalInvestmentForBrand", investmentPerBrand);
                                providerMetrics.put("ventaTotal", totalSales);

                                return providerId;
                            });
                })
                .then(Mono.defer(() -> {
                    // Guardar todas las métricas calculadas
                    return Flux.fromIterable(metricsMap.entrySet())
                            .flatMap(entry -> {
                                String providerId = entry.getKey();
                                Map<String, Object> metrics = entry.getValue();

                                Metrics metricsEntity = Metrics.builder()
                                        .providerId(providerId)
                                        .totalActiveCampaigns((Integer) metrics.get("totalActiveCampaigns"))
                                        .totalInvestmentPeriod((Double) metrics.get("totalInvestmentPeriod"))
                                        .totalInvestmentForBrand((Double) metrics.get("totalInvestmentForBrand"))
                                        .ventaTotal((Double) metrics.get("ventaTotal"))
                                        .build();

                                return metricsRepository.updateMetrics(metricsEntity);
                            })
                            .then();
                }))
                .doOnSuccess(v -> log.info("Cálculo de métricas generales completado"))
                .doOnError(e -> log.error("Error al calcular métricas generales", e));
    }

    @Override
    public Mono<Void> calculateProviderMetrics(String providerId) {
        log.info("Calculando métricas para el proveedor: {}", providerId);

        if (providerId == null || providerId.isEmpty()) {
            log.warn("ProviderId no válido");
            return Mono.empty();
        }

        return kpiRepository.findByProviderId(providerId)
                .collectList()
                .flatMap(kpis -> {
                    // Si no hay KPIs, retornamos métricas vacías
                    if (kpis.isEmpty()) {
                        log.warn("No se encontraron KPIs para el proveedor: {}", providerId);
                        Metrics emptyMetrics = Metrics.builder()
                                .providerId(providerId)
                                .totalActiveCampaigns(0)
                                .totalInvestmentPeriod(0.0)
                                .totalInvestmentForBrand(0.0)
                                .ventaTotal(0.0)
                                .build();
                        return metricsRepository.updateMetrics(emptyMetrics);
                    }

                    // Contar campañas activas (por campaignId único)
                    long activeCampaigns = kpis.stream()
                            .map(Kpi::getCampaignId)
                            .filter(id -> id != null && !id.isEmpty())
                            .distinct()
                            .count();

                    // Cálculo de inversión total (suma de investment de KPIs)
                    double totalInvestment = calculateTotalInvestment(kpis);

                    // Cálculo de inversión por marca (promedio por campaña)
                    double investmentPerBrand = activeCampaigns > 0 ?
                            totalInvestment / activeCampaigns : 0;

                    // Cálculo de ventas totales (suma de KPIs de venta)
                    double totalSales = calculateTotalSales(kpis);

                    Metrics metrics = Metrics.builder()
                            .providerId(providerId)
                            .totalActiveCampaigns((int) activeCampaigns)
                            .totalInvestmentPeriod(totalInvestment)
                            .totalInvestmentForBrand(investmentPerBrand)
                            .ventaTotal(totalSales)
                            .build();

                    return metricsRepository.updateMetrics(metrics);
                })
                .doOnSuccess(v -> log.info("Cálculo de métricas para el proveedor {} completado", providerId))
                .doOnError(e -> log.error("Error al calcular métricas para el proveedor {}", providerId, e))
                .then();
    }

    @Override
    public Mono<Void> calculateMetricsForPeriod(LocalDate startDate, LocalDate endDate) {
        log.info("Calculando métricas para el periodo: {} - {}", startDate, endDate);

        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            log.warn("Periodo de fechas no válido: {} - {}", startDate, endDate);
            return Mono.empty();
        }

        return kpiRepository.findByDateRange(startDate, endDate)
                .groupBy(Kpi::getProviderId)
                .flatMap(group -> {
                    String providerId = group.key();
                    if (providerId == null || providerId.isEmpty()) {
                        return Mono.empty();
                    }

                    return group.collectList()
                            .flatMap(kpis -> {
                                // Contar campañas activas (por campaignId único)
                                long activeCampaigns = kpis.stream()
                                        .map(Kpi::getCampaignId)
                                        .filter(id -> id != null && !id.isEmpty())
                                        .distinct()
                                        .count();

                                // Cálculo de inversión total (suma de investment de KPIs)
                                double totalInvestment = calculateTotalInvestment(kpis);

                                // Cálculo de inversión por marca (promedio por campaña)
                                double investmentPerBrand = activeCampaigns > 0 ?
                                        totalInvestment / activeCampaigns : 0;

                                // Cálculo de ventas totales (suma de KPIs de venta)
                                double totalSales = calculateTotalSales(kpis);

                                Metrics metrics = Metrics.builder()
                                        .providerId(providerId)
                                        .totalActiveCampaigns((int) activeCampaigns)
                                        .totalInvestmentPeriod(totalInvestment)
                                        .totalInvestmentForBrand(investmentPerBrand)
                                        .ventaTotal(totalSales)
                                        .build();

                                return metricsRepository.updateMetrics(metrics);
                            });
                })
                .then()
                .doOnSuccess(v -> log.info("Cálculo de métricas para el periodo {} - {} completado", startDate, endDate))
                .doOnError(e -> log.error("Error al calcular métricas para el periodo {} - {}", startDate, endDate, e));
    }

    // Método auxiliar para calcular la inversión total desde los KPIs
    private double calculateTotalInvestment(java.util.List<Kpi> kpis) {
        // Asumimos que hay KPIs específicos que almacenan la inversión
        // O podríamos obtenerlo directamente de la colección de campañas si existe

        // Buscamos KPIs con kpiId relacionado a inversión
        return kpis.stream()
                .filter(kpi -> "investment".equals(kpi.getKpiId()))
                .mapToDouble(Kpi::getValue)
                .sum();

        // Si no hay KPIs específicos de inversión, se podría implementar otra lógica
        // o consultar la colección de campañas para obtener esta información
    }

    // Método auxiliar para calcular las ventas totales desde los KPIs
    private double calculateTotalSales(java.util.List<Kpi> kpis) {
        // Buscamos KPIs con kpiId relacionado a ventas (MP-V, MCV, MFV, MBV, PW-V, etc.)
        return kpis.stream()
                .filter(kpi -> {
                    String kpiId = kpi.getKpiId();
                    return kpiId != null &&
                            (kpiId.equals("MP-V") || kpiId.equals("MCV") ||
                                    kpiId.equals("MFV") || kpiId.equals("MBV") ||
                                    kpiId.equals("PW-V") || kpiId.equals("PA-V"));
                })
                .mapToDouble(Kpi::getValue)
                .sum();
    }
    // src/main/java/pe/farmaciasperuanas/digital/process/kpi/application/service/MetricsServiceImpl.java
    @Override
    public Mono<Metrics> getProviderMetrics(String providerId) {
        log.info("Obteniendo métricas para el proveedor: {}", providerId);

        if (providerId == null || providerId.isEmpty()) {
            log.warn("ProviderId no válido");
            return Mono.empty();
        }

        return metricsRepository.findByProviderId(providerId)
                .doOnSuccess(metrics -> {
                    if (metrics == null) {
                        log.warn("No se encontraron métricas para el proveedor: {}", providerId);
                    } else {
                        log.info("Métricas recuperadas para el proveedor: {}", providerId);
                    }
                })
                .doOnError(e -> log.error("Error al obtener métricas para el proveedor: {}", providerId, e));
    }
}