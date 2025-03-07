package pe.farmaciasperuanas.digital.process.kpi.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "metrics")
public class Metrics {
    @Id
    private String id;
    private String providerId;
    private Integer totalActiveCampaigns;
    private Double totalInvestmentPeriod;
    private Double totalInvestmentForBrand;
    private Double ventaTotal;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private String createdUser;
}