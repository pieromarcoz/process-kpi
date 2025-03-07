package pe.farmaciasperuanas.digital.process.kpi.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "salesforce_push")
public class SalesforcePush {
    @Id
    private String id;
    private String androidMediaUrl;
    private String appName;
    private String contactKey;
    private String corporacion;
    private String dateTimeSend;
    private String deviceId;
    private String fechaProceso;
    private Integer format;
    private String iosMediaUrl;
    private String mediaAlt;
    private String messageContent;
    private Integer messageID;
    private String messageName;
    private Boolean messageOpened;
    private String openDate;
    private String platform;
    private String platformVersion;
    private String pushJobId;
    private String requestId;
    private String serviceResponse;
    private String status;
    private String systemToken;
    private String template;
}