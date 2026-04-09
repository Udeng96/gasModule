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
@Table(name = "gas_event", schema = "event")
public class GasEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_type")
    private String eventType;

    @Column(name = "area")
    private String area;

    @Column(name = "room")
    private String room;

    @Column(name = "gas_type")
    private String gasType;

    @Column(name = "detector_cd")
    private String detectorCd;

    @Column(name = "occurred_at", nullable = false)
    private OffsetDateTime occurredAt;

    @Column(name = "cleared_at")
    private OffsetDateTime clearedAt;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    public boolean isActive() {
        return clearedAt == null;
    }
}
