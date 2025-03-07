package pe.farmaciasperuanas.digital.process.kpi.infrastructure.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import pe.farmaciasperuanas.digital.process.kpi.domain.port.service.KpiService;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class SchedulingConfig {

    private final KpiService kpiService;

    /**
     * Ejecuta el procesamiento de KPIs cada 15 minutos
     */
    @Scheduled(fixedRate = 900000) // 15 minutos en milisegundos
    public void scheduleKpiProcessing() {
        log.info("Iniciando procesamiento programado de KPIs");

        kpiService.processAllPendingKpis()
                .subscribe(
                        null,
                        error -> log.error("Error en procesamiento programado de KPIs", error),
                        () -> log.info("Procesamiento programado de KPIs completado")
                );
    }
}