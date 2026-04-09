package com.eseict.gasmodule.data.postgres.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.OffsetDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "gas_log", schema = "event")
public class GasLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "gas_event_id")
    private Long gasEventId;

    @Column(name = "detector_cd", nullable = false)
    private String detectorCd;

    @Column(name = "building_id")
    private Long buildingId;

    @Column(name = "level")
    private Integer level;

    @Column(name = "level_status")
    private Integer levelStatus;

    @Column(name = "detected_at", nullable = false)
    private OffsetDateTime detectedAt;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "mssql_idx")
    private Integer mssqlIdx;
}
