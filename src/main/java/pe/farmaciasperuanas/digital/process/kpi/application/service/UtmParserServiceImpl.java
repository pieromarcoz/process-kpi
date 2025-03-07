package pe.farmaciasperuanas.digital.process.kpi.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.farmaciasperuanas.digital.process.kpi.domain.entity.CampaignMetadata;
import pe.farmaciasperuanas.digital.process.kpi.domain.port.service.UtmParserService;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
@RequiredArgsConstructor
public class UtmParserServiceImpl implements UtmParserService {

    private static final Pattern UTM_CAMPAIGN_PATTERN = Pattern.compile("utm_campaign=([^&?]+)");

    @Override
    public Mono<CampaignMetadata> extractMetadataFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return Mono.empty();
        }

        try {
            // Extraer el parámetro utm_campaign de la URL
            Matcher matcher = UTM_CAMPAIGN_PATTERN.matcher(url);
            if (matcher.find()) {
                String utmCampaign = matcher.group(1);
                return extractMetadataFromUtmCampaign(utmCampaign);
            }

            log.warn("No se encontró parámetro utm_campaign en la URL: {}", url);
            return Mono.empty();
        } catch (Exception e) {
            log.error("Error al extraer metadatos de la URL: {}", url, e);
            return Mono.empty();
        }
    }

    @Override
    public Mono<CampaignMetadata> extractMetadataFromUtmCampaign(String utmCampaign) {
        if (utmCampaign == null || utmCampaign.isEmpty()) {
            return Mono.empty();
        }

        try {
            // Ejemplo de formato:
            // 20250125_do_cindi_mifarma_estandar_compra_abierto_web_pautaregular_farma_neutrogena_011592_body

            String[] parts = utmCampaign.split("_");
            if (parts.length < 3) {
                log.warn("Formato de utm_campaign no válido: {}", utmCampaign);
                return Mono.empty();
            }

            // El formato (MC, MF, MB) suele estar al final
            String format = parts[parts.length - 1];

            // El campaignId suele estar al inicio (fecha YYYYMMDD)
            String campaignId = parts[0];

            // El campaignSubId es la combinación de campaignId y formato
            String campaignSubId = campaignId + format;

            // El medium suele ser "medio propios" para mailing
            String medium = "medio propios";

            // La plataforma suele ser "Salesforce" para mailing
            String platform = "Salesforce";

            // El providerId requeriría una consulta adicional a la base de datos
            // Por ahora, lo dejamos vacío
            String providerId = "";

            return Mono.just(CampaignMetadata.builder()
                    .campaignId(campaignId)
                    .campaignSubId(campaignSubId)
                    .format(format)
                    .medium(medium)
                    .platform(platform)
                    .providerId(providerId)
                    .build());

        } catch (Exception e) {
            log.error("Error al extraer metadatos de utm_campaign: {}", utmCampaign, e);
            return Mono.empty();
        }
    }

    @Override
    public Mono<String> extractCampaignIdFromSendId(Integer sendId) {
        if (sendId == null) {
            return Mono.empty();
        }

        // Esta implementación es provisional y requerirá una consulta a la base de datos
        // para obtener el campaignId asociado al sendId.
        // Por ahora, retornamos un valor dummy para testing.

        log.warn("Implementación provisional para extractCampaignIdFromSendId. SendId: {}", sendId);
        return Mono.just("202501" + (sendId % 100));
    }
}