package com.eseict.gasmodule.repository.postgres;

import com.eseict.gasmodule.data.postgres.domain.GasEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GasEventRepository extends JpaRepository<GasEvent, Long> {

    /**
     * 특정 센서의 활성(미해제) 이벤트 조회
     */
    Optional<GasEvent> findByDetectorCdAndClearedAtIsNull(String detectorCd);
}
