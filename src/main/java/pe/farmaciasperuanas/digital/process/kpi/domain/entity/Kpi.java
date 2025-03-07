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
@Document(collection = "kpi")
public class Kpi {
    @Id
    private String id;
    private String campaignId;
    private String campaignSubId;
    private String providerId;
    private String kpiId;
    private String kpiDescription;
    private String type; // Cantidad, porcentaje
    private Double value;
    private String status; // A: activo / I: inactivo
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private String createdUser;
}