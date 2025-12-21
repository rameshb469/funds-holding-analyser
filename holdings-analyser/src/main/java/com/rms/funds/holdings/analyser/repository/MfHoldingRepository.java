package com.rms.funds.holdings.analyser.repository;

import com.rms.funds.holdings.analyser.controller.dto.SectorIndustryStockChangeDTO;
import com.rms.funds.holdings.analyser.entity.MfHoldingEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MfHoldingRepository extends JpaRepository<MfHoldingEntity, Long> {

    List<MfHoldingEntity> findByStockId(Long stockId);

    @Query(value = """
    SELECT DISTINCT m.atDate FROM MfHoldingEntity m ORDER BY m.atDate DESC
    """)
    List<LocalDate> findLast12Dates(Pageable pageable);


    @Query("""
    SELECT h.stockId, h.mfId, h.atDate, h.marketValue, h.netAssetPct
    FROM MfHoldingEntity h
    JOIN h.stockInfoEntity s
    WHERE (h.atDate = :currentDate OR h.atDate = :prevDate) AND s.symbol <> 'CASH'
    """)
    List<Object[]> findHoldingsForDates(@Param("currentDate") LocalDate currentDate,
                                        @Param("prevDate") LocalDate prevDate);

    @Query("""
        SELECT new com.rms.funds.holdings.analyser.controller.dto.SectorIndustryStockChangeDTO(
            sec.id, sec.name,
            ind.id, ind.name,
            s.id, s.company, s.symbol,
            SUM(curr.marketValue),
            COALESCE(SUM(prev.marketValue), 0),
            CASE 
                WHEN COALESCE(SUM(prev.marketValue), 0) = 0 
                THEN 0 
                ELSE (SUM(curr.marketValue) - SUM(prev.marketValue)) / SUM(prev.marketValue) * 100 
            END
        )
        FROM MfHoldingEntity curr
        JOIN curr.stockInfoEntity s
        LEFT JOIN s.industry ind
        LEFT JOIN s.sector sec
        LEFT JOIN MfHoldingEntity prev
            ON prev.stockInfoEntity.id = s.id
           AND prev.atDate = :prevDate
        WHERE curr.atDate = :currentDate AND s.symbol <> 'CASH'
        GROUP BY sec.id, sec.name, ind.id, ind.name, s.id, s.company, s.symbol
    """)
    List<SectorIndustryStockChangeDTO> findSectorIndustryChanges(
            @Param("currentDate") LocalDate currentDate,
            @Param("prevDate") LocalDate prevDate);

    @Query("SELECT h FROM MfHoldingEntity h " +
                "JOIN FETCH h.stockInfoEntity s " +
                "WHERE h.atDate = :atDate AND s.symbol <> 'CASH' ")
    List<MfHoldingEntity> findByAtDate(@Param("atDate") LocalDate atDate);

    // Get holdings between two dates (for trend analysis)
    @Query("SELECT h FROM MfHoldingEntity h WHERE h.atDate BETWEEN :from AND :to")
    List<MfHoldingEntity> findByAtDateBetween(LocalDate from, LocalDate to);

    // Optional: Fetch by stockId and date range
    @Query("SELECT h FROM MfHoldingEntity h WHERE h.stockId = :stockId AND h.atDate BETWEEN :from AND :to")
    List<MfHoldingEntity> findByStockIdAndAtDateBetween(Long stockId, LocalDate from, LocalDate to);

    // Optional: Latest holding date
    @Query("SELECT MAX(h.atDate) FROM MfHoldingEntity h")
    LocalDate findLatestAtDate();

}
