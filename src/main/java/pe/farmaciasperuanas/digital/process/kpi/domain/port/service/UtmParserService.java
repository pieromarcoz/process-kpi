package pe.farmaciasperuanas.digital.process.kpi.domain.port.service;

import pe.farmaciasperuanas.digital.process.kpi.domain.entity.CampaignMetadata;
import reactor.core.publisher.Mono;

public interface UtmParserService {
    /**
     * Extrae metadatos de la campaña a partir de una URL con parámetros UTM
     * @param url URL completa con parámetros UTM
     * @return Metadatos extraídos de la campaña
     */
    Mono<CampaignMetadata> extractMetadataFromUrl(String url);

    /**
     * Extrae metadatos de la campaña a partir de un valor utm_campaign
     * @param utmCampaign Valor del parámetro utm_campaign
     * @return Metadatos extraídos de la campaña
     */
    Mono<CampaignMetadata> extractMetadataFromUtmCampaign(String utmCampaign);

    /**
     * Extrae el ID de la campaña de un sendID de Salesforce
     * @param sendId SendID de Salesforce
     * @return ID de la campaña asociada
     */
    Mono<String> extractCampaignIdFromSendId(Integer sendId);
}