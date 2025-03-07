package pe.farmaciasperuanas.digital.process.kpi.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignMetadata {
    private String campaignId;
    private String campaignSubId;
    private String format;
    private String providerId;
    private String medium;
    private String platform;
}