package pe.farmaciasperuanas.digital.process.kpi.infrastructure.inbound.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import pe.farmaciasperuanas.digital.process.kpi.domain.port.service.KpiService;
import reactor.core.publisher.Flux;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.Map;
/**
 * Controlador principal que expone el servicio a trav&eacute;s de HTTP/Rest para
 * las operaciones del recurso Kpi<br/>
 * <b>Class</b>: KpiController<br/>
 * <b>Copyright</b>: 2025 Farmacias Peruanas.<br/>
 * <b>Company</b>:Farmacias Peruanas.<br/>
 *
 * <u>Developed by</u>: <br/>
 * <ul>
 * <li>Piero Marcos</li>
 * </ul>
 * <u>Changes</u>:<br/>
 * <ul>
 * <li>Feb 28, 2025 Creaci&oacute;n de Clase.</li>
 * </ul>
 * @version 1.0
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/kpi")
public class KpiRest {
  private final ReactiveMongoTemplate reactiveMongoTemplate;
  private final KpiService kpiService;

  @GetMapping(value = {"/health"})
  public String health() {
    return "It's running";
  }

  @PostMapping("/process")
  public Mono<Void> processKpis(
          @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
          @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
    return kpiService.processKpis(startDate, endDate);
  }

  @PostMapping("/process/provider/{providerId}")
  public Mono<Void> processKpisByProvider(
          @PathVariable("providerId") String providerId,
          @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
          @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
    return kpiService.processKpisByProvider(providerId, startDate, endDate);
  }
}