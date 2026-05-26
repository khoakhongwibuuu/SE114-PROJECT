package com.carenest.backend.features.growth.repository;

import com.carenest.backend.features.growth.entity.GrowthRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GrowthRecordRepository extends JpaRepository<GrowthRecord, Long> {

    List<GrowthRecord> findByHealthProfileIdOrderByRecordDateDesc(Long healthProfileId);

    List<GrowthRecord> findByHealthProfileIdOrderByRecordDateAsc(Long healthProfileId);
}
