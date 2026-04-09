package com.eseict.gasmodule.repository.postgres;

import com.eseict.gasmodule.data.postgres.domain.GasLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface GasLogRepository extends JpaRepository<GasLog, Long> {

    /**
     * 특정 센서의 최신 로그 조회 (상태 전이 판단용)
     */
    Optional<GasLog> findTopByDetectorCdOrderByDetectedAtDesc(String detectorCd);

    /**
     * 특정 이벤트의 로그 목록
     */
    List<GasLog> findByGasEventId(Long gasEventId);

    /**
     * catch-up용: PostgreSQL에 저장된 마지막 MSSQL idx 조회
     */
    @Query("SELECT COALESCE(MAX(g.mssqlIdx), 0) FROM GasLog g")
    int findMaxMssqlIdx();
}
