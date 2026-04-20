package com.carenest.backend.module.growth.repository;

import com.carenest.backend.module.growth.entity.GrowthRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GrowthRecordRepository extends JpaRepository<GrowthRecord, Long> {
}
