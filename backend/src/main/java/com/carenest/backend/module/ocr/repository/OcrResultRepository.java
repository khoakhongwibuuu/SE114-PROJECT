package com.carenest.backend.module.ocr.repository;

import com.carenest.backend.module.ocr.entity.OcrResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OcrResultRepository extends JpaRepository<OcrResult, Long> {
}
