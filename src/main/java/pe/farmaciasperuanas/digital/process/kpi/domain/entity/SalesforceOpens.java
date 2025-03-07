package pe.farmaciasperuanas.digital.process.kpi.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "salesforce_opens")
public class SalesforceOpens {
    @Id
    private String id;
    private Integer batchID;
    private String browser;
    private Long clientID;
    private String corporacion;
    private String device;
    private String emailAddress;
    private String emailClient;
    private LocalDateTime eventDate;
    private String eventType;
    private LocalDate fechaProceso;
    private Boolean isUnique;
    private Integer listID;
    private String operatingSystem;
    private Integer sendID;
    private Long subscriberID;
    private String subscriberKey;
    private String triggeredSendExternalKey;
}

