package com.rms.funds.holdings.analyser.repository;

import com.rms.funds.holdings.analyser.entity.NseHolidayEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface NseHolidayRepository extends JpaRepository<NseHolidayEntity, Long> {

    List<NseHolidayEntity> findAllByOrderByHolidayDateAsc();

    boolean existsByHolidayDate(LocalDate holidayDate);
}
