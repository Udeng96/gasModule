package com.eseict.gasmodule.repository.postgres;

import com.eseict.gasmodule.data.postgres.domain.GasSensor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GasSensorRepository extends JpaRepository<GasSensor, Long> {

    Optional<GasSensor> findBySensorId(String sensorId);
}
