package com.eseict.gasmodule.repository.mssql;

import com.eseict.gasmodule.data.mssql.domain.TblEventGas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface GasRepository extends JpaRepository<TblEventGas, Integer> {

    @Query("SELECT COALESCE(MAX(e.idx), 0) FROM TblEventGas e")
    int findMaxIdx();

//    @Query(
//            "SELECT e FROM TblEventGas e " +
//                    "WHERE e.date > :lastDate " +
//                    "   AND e.idx > :lastSeqn " +
//                    "ORDER BY e.date ASC, e.idx ASC"
//    )
//    List<TblEventGas> findNextEvents(@Param("lastDate") LocalDateTime lastDate,
//            @Param("lastSeqn") Integer lastSeqn);

    @Query("SELECT e FROM TblEventGas e " +
            "WHERE e.date > :lastDate AND e.idx > :lastSeqn " +
            "ORDER BY e.date ASC, e.idx ASC")
    List<TblEventGas> findNextEvents(
            @Param("lastDate") LocalDateTime lastDate,
            @Param("lastSeqn") Integer lastSeqn);
}
