package com.eseict.gasmodule.data.postgres.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "gas_sensor", schema = "fac")
public class GasSensor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sensor_id", unique = true, nullable = false)
    private String sensorId;

    @Column(name = "sensor_name")
    private String sensorName;

    @Column(name = "building_id")
    private Long buildingId;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;
}
